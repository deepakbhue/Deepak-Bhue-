package com.example.data.repository

import android.content.Context
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CurrencyCode
import com.example.core.i18n.formatPrice
import com.example.core.notifications.OrderNotificationHelper
import com.example.data.local.AddressEntity
import com.example.data.local.AppNotificationEntity
import com.example.data.local.BazaarDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemCodec
import com.example.data.local.ProductEntity
import com.example.data.local.SeedDataProvider
import com.example.data.local.UserEntity
import com.example.data.local.WishlistEntity
import com.example.data.model.AuthProvider
import com.example.data.model.CartItemWithProduct
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderTrackingStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.UserRole
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.random.Random

class MarketplaceRepository(
    private val context: Context,
    private val db: BazaarDatabase,
    private val prefsRepo: UserPreferencesRepository
) {
    val prefsFlow: Flow<UserPrefs> = prefsRepo.prefsFlow
    val allUsersFlow: Flow<List<UserEntity>> = db.userDao().observeAllUsers()
    val approvedProductsFlow: Flow<List<ProductEntity>> = db.productDao().observeApprovedProducts()
    val allProductsFlow: Flow<List<ProductEntity>> = db.productDao().observeAllProducts()
    val allOrdersFlow: Flow<List<OrderEntity>> = db.orderDao().observeAllOrders()

    suspend fun initializeSeedData() {
        SeedDataProvider.ensureSeeded(db)
    }

    fun observeUser(userId: String): Flow<UserEntity?> = db.userDao().observeUserById(userId)

    fun observeCartWithProducts(userId: String): Flow<List<CartItemWithProduct>> =
        combine(
            db.cartDao().observeCart(userId),
            db.productDao().observeApprovedProducts()
        ) { cartItems, products ->
            val productMap = products.associateBy { it.id }
            cartItems.mapNotNull { cart ->
                val prod = productMap[cart.productId] ?: return@mapNotNull null
                CartItemWithProduct(
                    cartId = cart.id,
                    productId = prod.id,
                    title = prod.title,
                    vendorId = prod.vendorId,
                    vendorName = prod.vendorName,
                    priceInr = prod.priceInr,
                    mrpInr = prod.mrpInr,
                    stockCount = prod.stockCount,
                    quantity = cart.quantity,
                    selectedVariant = cart.selectedVariant,
                    imageKey = prod.imageKey
                )
            }
        }

    fun observeWishlistIds(userId: String): Flow<Set<String>> =
        db.wishlistDao().observeWishlist(userId).combine(db.productDao().observeApprovedProducts()) { wish, prods ->
            val validIds = prods.map { it.id }.toSet()
            wish.map { it.productId }.filter { it in validIds }.toSet()
        }

    fun observeAddresses(userId: String): Flow<List<AddressEntity>> =
        db.addressDao().observeAddresses(userId)

    fun observeBuyerOrders(userId: String): Flow<List<OrderEntity>> =
        db.orderDao().observeBuyerOrders(userId)

    fun observeNotifications(userId: String): Flow<List<AppNotificationEntity>> =
        db.notificationDao().observeNotifications(userId)

    suspend fun setLanguage(language: AppLanguage) = prefsRepo.setLanguage(language)
    suspend fun setCurrency(currency: CurrencyCode) = prefsRepo.setCurrency(currency)
    suspend fun setNotificationsEnabled(enabled: Boolean) = prefsRepo.setNotificationsEnabled(enabled)
    suspend fun setThemeMode(mode: AppThemeMode) = prefsRepo.setThemeMode(mode)

    suspend fun switchToUser(userId: String) {
        prefsRepo.setActiveUserId(userId)
    }

    suspend fun signInWithSocialOrEmail(
        name: String,
        email: String,
        provider: AuthProvider,
        role: UserRole,
        shopName: String
    ): UserEntity {
        val existing = db.userDao().getUserByEmail(email.trim().lowercase())
        val user = if (existing != null) {
            existing.copy(
                name = name.ifBlank { existing.name },
                authProvider = provider.name
            )
        } else {
            UserEntity(
                id = "user_${UUID.randomUUID().toString().take(8)}",
                name = name.ifBlank { "Shopper" },
                email = email.trim().lowercase(),
                authProvider = provider.name,
                role = role.name,
                vendorShopName = if (role == UserRole.VENDOR) shopName.ifBlank { "$name Store" } else "",
                isVerifiedVendor = role != UserRole.BUYER
            )
        }
        db.userDao().upsertUser(user)
        if (db.addressDao().observeAddresses(user.id).first().isEmpty()) {
            db.addressDao().upsertAddress(
                AddressEntity(
                    id = "addr_${UUID.randomUUID().toString().take(8)}",
                    userId = user.id,
                    fullName = user.name,
                    phone = "+91 98100 12345",
                    street = "21 Global Avenue, Residency Road",
                    city = "Bengaluru",
                    postalCode = "560025",
                    country = "India",
                    isDefault = true
                )
            )
        }
        prefsRepo.setActiveUserId(user.id)
        return user
    }

    suspend fun addToCart(userId: String, product: ProductEntity, variant: String, quantityDelta: Int = 1) {
        val existing = db.cartDao().findCartItem(userId, product.id, variant)
        if (existing != null) {
            val nextQty = (existing.quantity + quantityDelta).coerceIn(1, product.stockCount.coerceAtLeast(1))
            db.cartDao().updateQuantity(existing.id, nextQty)
        } else {
            db.cartDao().upsertCartItem(
                CartItemEntity(
                    id = "cart_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    productId = product.id,
                    quantity = quantityDelta.coerceIn(1, product.stockCount.coerceAtLeast(1)),
                    selectedVariant = variant
                )
            )
        }
    }

    suspend fun updateCartQuantity(cartId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            db.cartDao().removeCartItem(cartId)
        } else {
            db.cartDao().updateQuantity(cartId, newQuantity)
        }
    }

    suspend fun toggleWishlist(userId: String, productId: String): Boolean {
        val existing = db.wishlistDao().findWishlistItem(userId, productId)
        return if (existing != null) {
            db.wishlistDao().removeFromWishlist(userId, productId)
            false
        } else {
            db.wishlistDao().addToWishlist(
                WishlistEntity(
                    id = "${userId}_$productId",
                    userId = userId,
                    productId = productId
                )
            )
            true
        }
    }

    suspend fun addAddress(
        userId: String,
        fullName: String,
        phone: String,
        street: String,
        city: String,
        postalCode: String,
        country: String
    ) {
        db.addressDao().clearDefaultAddresses(userId)
        db.addressDao().upsertAddress(
            AddressEntity(
                id = "addr_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                fullName = fullName,
                phone = phone,
                street = street,
                city = city,
                postalCode = postalCode,
                country = country,
                isDefault = true
            )
        )
    }

    suspend fun selectDefaultAddress(userId: String, addressId: String) {
        db.addressDao().clearDefaultAddresses(userId)
        db.addressDao().setDefaultAddress(addressId)
    }

    suspend fun placeOrder(
        buyer: UserEntity,
        cartItems: List<CartItemWithProduct>,
        address: AddressEntity,
        currency: CurrencyCode,
        discountInr: Double,
        paymentMethod: PaymentMethod
    ): OrderEntity {
        val subtotal = cartItems.sumOf { it.priceInr * it.quantity }
        val finalInr = (subtotal - discountInr).coerceAtLeast(0.0)
        val lineItems = cartItems.map {
            OrderLineItem(
                productId = it.productId,
                title = it.title,
                vendorId = it.vendorId,
                vendorName = it.vendorName,
                quantity = it.quantity,
                unitPriceInr = it.priceInr,
                variant = it.selectedVariant,
                imageKey = it.imageKey
            )
        }
        val orderId = "BH-${Random.nextInt(100000, 999999)}"
        val gatewayRef = "TXN-GLB-${Random.nextInt(100000, 999999)}"
        val trackingCode = "GLB-${Random.nextInt(10000000, 99999999)}"
        val isExpressFoodOrder = cartItems.all { item ->
            val prod = db.productDao().getProductById(item.productId)
            prod?.category?.lowercase() in listOf("food", "grocery", "snacks")
        }
        val courier = when {
            isExpressFoodOrder -> "BazaarFresh Express"
            address.country.lowercase() == "india" -> "BlueDart Express"
            else -> "DHL Global Express"
        }
        val eta = if (isExpressFoodOrder) "Express Delivery in 25–35 Mins" else "Estimated in 2–4 Business Days"

        val order = OrderEntity(
            id = orderId,
            buyerId = buyer.id,
            buyerName = buyer.name,
            shippingAddressSummary = "${address.fullName} • ${address.street}, ${address.city} ${address.postalCode}, ${address.country}",
            itemsEncoded = OrderItemCodec.encode(lineItems),
            vendorIdsCsv = lineItems.map { it.vendorId }.distinct().joinToString(","),
            totalInr = finalInr,
            discountInr = discountInr,
            currencyCode = currency.code,
            paidAmountFormatted = formatPrice(finalInr, currency),
            paymentMethod = paymentMethod.name,
            paymentGatewayRef = gatewayRef,
            paymentStatus = if (paymentMethod == PaymentMethod.COD) "COD_PENDING" else "PAID",
            trackingStatus = OrderTrackingStatus.PLACED.name,
            courierName = courier,
            trackingCode = trackingCode,
            estimatedDelivery = eta
        )
        db.orderDao().upsertOrder(order)

        // Deduct inventory
        cartItems.forEach { item ->
            val currentProd = db.productDao().getProductById(item.productId)
            if (currentProd != null) {
                db.productDao().updateStock(
                    currentProd.id,
                    (currentProd.stockCount - item.quantity).coerceAtLeast(0)
                )
            }
        }

        // Clear buyer cart
        db.cartDao().clearCart(buyer.id)

        // Create notification for buyer and notify vendors
        val notifTitle = "Order Confirmed • $orderId"
        val notifBody = "Paid ${order.paidAmountFormatted} via ${paymentMethod.name.replace('_', ' ')} ($gatewayRef). Courier: $courier."
        emitOrderNotification(buyer.id, notifTitle, notifBody, orderId)

        lineItems.map { it.vendorId }.distinct().forEach { vendorId ->
            if (vendorId != buyer.id) {
                db.notificationDao().insertNotification(
                    AppNotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        userId = vendorId,
                        title = "New Order Received • $orderId",
                        message = "${buyer.name} ordered ${lineItems.count { it.vendorId == vendorId }} item(s). Ready to confirm & pack.",
                        orderId = orderId
                    )
                )
            }
        }

        return order
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderTrackingStatus) {
        val order = db.orderDao().getOrderById(orderId) ?: return
        val updated = order.copy(
            trackingStatus = newStatus.name,
            updatedAt = System.currentTimeMillis(),
            estimatedDelivery = when (newStatus) {
                OrderTrackingStatus.DELIVERED -> "Delivered Today"
                OrderTrackingStatus.OUT_FOR_DELIVERY -> "Arriving Today by 8:00 PM"
                OrderTrackingStatus.CANCELLED -> "Order Cancelled"
                else -> order.estimatedDelivery
            }
        )
        db.orderDao().updateOrder(updated)

        val statusLabel = newStatus.name.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
        emitOrderNotification(
            userId = order.buyerId,
            title = "Order $orderId • $statusLabel",
            body = "Shipment (${order.trackingCode}) updated to $statusLabel via ${order.courierName}.",
            orderId = orderId
        )
    }

    private suspend fun emitOrderNotification(
        userId: String,
        title: String,
        body: String,
        orderId: String?
    ) {
        db.notificationDao().insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                title = title,
                message = body,
                orderId = orderId
            )
        )
        val prefs = prefsRepo.prefsFlow.first()
        if (prefs.notificationsEnabled) {
            OrderNotificationHelper.showOrderNotification(context, title, body)
        }
    }

    suspend fun markAllNotificationsRead(userId: String) {
        db.notificationDao().markAllRead(userId)
    }

    // Vendor operations (RBAC gated)
    suspend fun createOrUpdateVendorProduct(
        actor: UserEntity,
        existingId: String?,
        title: String,
        description: String,
        category: String,
        priceInr: Double,
        mrpInr: Double,
        stockCount: Int,
        imageKey: String,
        variantsCsv: String,
        isFlashDeal: Boolean
    ) {
        val role = UserRole.fromString(actor.role)
        require(role.canAccessVendorPanel()) { "Access denied: Vendor or Admin role required." }

        val product = ProductEntity(
            id = existingId ?: "prod_${UUID.randomUUID().toString().take(8)}",
            vendorId = actor.id,
            vendorName = actor.vendorShopName.ifBlank { actor.name },
            title = title.trim(),
            description = description.trim(),
            category = category,
            priceInr = priceInr,
            mrpInr = mrpInr.coerceAtLeast(priceInr),
            rating = 4.8f,
            reviewCount = 1,
            stockCount = stockCount.coerceAtLeast(0),
            imageKey = imageKey,
            variantsCsv = variantsCsv.ifBlank { "Standard" },
            isApproved = true,
            isFlashDeal = isFlashDeal
        )
        db.productDao().upsertProduct(product)
    }

    suspend fun updateProductStock(actor: UserEntity, productId: String, newStock: Int) {
        val role = UserRole.fromString(actor.role)
        require(role.canAccessVendorPanel()) { "Access denied." }
        db.productDao().updateStock(productId, newStock.coerceAtLeast(0))
    }

    suspend fun deleteVendorProduct(actor: UserEntity, productId: String) {
        val role = UserRole.fromString(actor.role)
        require(role.canAccessVendorPanel()) { "Access denied." }
        db.productDao().deleteProduct(productId)
    }

    // Admin operations (Strict ADMIN RBAC gated)
    suspend fun adminSetUserRole(actor: UserEntity, targetUserId: String, newRole: UserRole) {
        val actorRole = UserRole.fromString(actor.role)
        require(actorRole.canAccessAdminPanel()) { "Access denied: Admin role required." }
        db.userDao().updateUserRole(
            userId = targetUserId,
            newRole = newRole.name,
            verifiedVendor = newRole != UserRole.BUYER
        )
    }

    suspend fun adminSetProductApproval(actor: UserEntity, productId: String, approved: Boolean) {
        val actorRole = UserRole.fromString(actor.role)
        require(actorRole.canAccessAdminPanel()) { "Access denied: Admin role required." }
        db.productDao().setProductApproval(productId, approved)
    }
}
