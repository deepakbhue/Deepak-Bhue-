package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.AppStrings
import com.example.core.i18n.CurrencyCode
import com.example.core.i18n.localizedStrings
import com.example.data.local.AddressEntity
import com.example.data.local.AppNotificationEntity
import com.example.data.local.BazaarDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemCodec
import com.example.data.local.ProductEntity
import com.example.data.local.SeedDataProvider
import com.example.data.local.UserEntity
import com.example.data.model.AuthProvider
import com.example.data.model.CartItemWithProduct
import com.example.data.model.OrderTrackingStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductCategory
import com.example.data.model.UserRole
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MarketplaceUiState(
    val isLoading: Boolean = true,
    val activeUser: UserEntity? = null,
    val activeRole: UserRole = UserRole.BUYER,
    val allUsers: List<UserEntity> = emptyList(),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val strings: AppStrings = localizedStrings(AppLanguage.ENGLISH),
    val currency: CurrencyCode = CurrencyCode.INR,
    val notificationsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val approvedProducts: List<ProductEntity> = emptyList(),
    val allProductsForAdmin: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val cartItems: List<CartItemWithProduct> = emptyList(),
    val wishlistIds: Set<String> = emptySet(),
    val addresses: List<AddressEntity> = emptyList(),
    val buyerOrders: List<OrderEntity> = emptyList(),
    val allPlatformOrders: List<OrderEntity> = emptyList(),
    val notifications: List<AppNotificationEntity> = emptyList(),
    val promoCode: String = "",
    val promoDiscountPercent: Int = 0,
    val isProcessingPayment: Boolean = false,
    val lastCompletedOrderId: String? = null
) {
    val unreadNotificationCount: Int get() = notifications.count { !it.isRead }
    val cartCount: Int get() = cartItems.sumOf { it.quantity }
    val cartSubtotalInr: Double get() = cartItems.sumOf { it.priceInr * it.quantity }
    val promoDiscountInr: Double get() = cartSubtotalInr * (promoDiscountPercent / 100.0)
    val cartFinalInr: Double get() = (cartSubtotalInr - promoDiscountInr).coerceAtLeast(0.0)

    val filteredProducts: List<ProductEntity>
        get() = approvedProducts.filter { prod ->
            val matchesCat = selectedCategory == ProductCategory.ALL ||
                prod.category.equals(selectedCategory.key, ignoreCase = true)
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                prod.title.contains(q, ignoreCase = true) ||
                prod.category.contains(q, ignoreCase = true) ||
                prod.vendorName.contains(q, ignoreCase = true)
            matchesCat && matchesQuery
        }

    val flashDeals: List<ProductEntity>
        get() = approvedProducts.filter { it.isFlashDeal }

    val wishlistProducts: List<ProductEntity>
        get() = approvedProducts.filter { it.id in wishlistIds }

    val vendorProducts: List<ProductEntity>
        get() {
            val uid = activeUser?.id ?: return emptyList()
            return if (activeRole == UserRole.ADMIN) allProductsForAdmin
            else allProductsForAdmin.filter { it.vendorId == uid }
        }

    val vendorOrders: List<OrderEntity>
        get() {
            val uid = activeUser?.id ?: return emptyList()
            return if (activeRole == UserRole.ADMIN) allPlatformOrders
            else allPlatformOrders.filter { order ->
                order.vendorIdsCsv.split(",").contains(uid)
            }
        }

    val selectedAddress: AddressEntity?
        get() = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
}

private data class FilterAndPromo(
    val searchQuery: String,
    val category: ProductCategory,
    val promoCode: String,
    val promoPercent: Int,
    val isProcessingPayment: Boolean,
    val lastOrderId: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BazaarDatabase.getInstance(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val repository = MarketplaceRepository(application, db, prefsRepo)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    private val _promoCode = MutableStateFlow("")
    private val _promoPercent = MutableStateFlow(0)
    private val _isProcessingPayment = MutableStateFlow(false)
    private val _lastOrderId = MutableStateFlow<String?>(null)

    private val _snackbarEvents = MutableSharedFlow<String>()
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedData()
        }
    }

    private val userScopedFlows = repository.prefsFlow.flatMapLatest { prefs ->
        val uid = prefs.activeUserId.ifBlank { SeedDataProvider.DEFAULT_BUYER_ID }
        combine(
            repository.observeUser(uid),
            repository.observeCartWithProducts(uid),
            repository.observeWishlistIds(uid),
            repository.observeAddresses(uid),
            repository.observeBuyerOrders(uid),
            repository.observeNotifications(uid)
        ) { arr ->
            @Suppress("UNCHECKED_CAST")
            UserScopedBundle(
                prefs = prefs,
                user = arr[0] as UserEntity?,
                cart = arr[1] as List<CartItemWithProduct>,
                wishlist = arr[2] as Set<String>,
                addresses = arr[3] as List<AddressEntity>,
                buyerOrders = arr[4] as List<OrderEntity>,
                notifications = arr[5] as List<AppNotificationEntity>
            )
        }
    }

    private val filterAndPromoFlow = combine(
        _searchQuery,
        _selectedCategory,
        _promoCode,
        _promoPercent,
        _isProcessingPayment,
        _lastOrderId
    ) { arr ->
        FilterAndPromo(
            searchQuery = arr[0] as String,
            category = arr[1] as ProductCategory,
            promoCode = arr[2] as String,
            promoPercent = arr[3] as Int,
            isProcessingPayment = arr[4] as Boolean,
            lastOrderId = arr[5] as String?
        )
    }

    val uiState: StateFlow<MarketplaceUiState> = combine(
        userScopedFlows,
        repository.allUsersFlow,
        repository.approvedProductsFlow,
        repository.allProductsFlow,
        repository.allOrdersFlow,
        filterAndPromoFlow
    ) { arr ->
        val scoped = arr[0] as UserScopedBundle
        @Suppress("UNCHECKED_CAST")
        val allUsers = arr[1] as List<UserEntity>
        @Suppress("UNCHECKED_CAST")
        val approved = arr[2] as List<ProductEntity>
        @Suppress("UNCHECKED_CAST")
        val allProds = arr[3] as List<ProductEntity>
        @Suppress("UNCHECKED_CAST")
        val allOrders = arr[4] as List<OrderEntity>
        val filters = arr[5] as FilterAndPromo

        val role = UserRole.fromString(scoped.user?.role ?: "BUYER")
        MarketplaceUiState(
            isLoading = false,
            activeUser = scoped.user,
            activeRole = role,
            allUsers = allUsers,
            language = scoped.prefs.language,
            strings = localizedStrings(scoped.prefs.language),
            currency = scoped.prefs.currency,
            notificationsEnabled = scoped.prefs.notificationsEnabled,
            themeMode = scoped.prefs.themeMode,
            approvedProducts = approved,
            allProductsForAdmin = allProds,
            searchQuery = filters.searchQuery,
            selectedCategory = filters.category,
            cartItems = scoped.cart,
            wishlistIds = scoped.wishlist,
            addresses = scoped.addresses,
            buyerOrders = scoped.buyerOrders,
            allPlatformOrders = allOrders,
            notifications = scoped.notifications,
            promoCode = filters.promoCode,
            promoDiscountPercent = filters.promoPercent,
            isProcessingPayment = filters.isProcessingPayment,
            lastCompletedOrderId = filters.lastOrderId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MarketplaceUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setCurrency(currency: CurrencyCode) {
        viewModelScope.launch { repository.setCurrency(currency) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchToUser(userId)
            _snackbarEvents.emit("Switched profile")
        }
    }

    fun signInWithSocial(
        name: String,
        email: String,
        provider: AuthProvider,
        role: UserRole,
        shopName: String
    ) {
        viewModelScope.launch {
            val user = repository.signInWithSocialOrEmail(name, email, provider, role, shopName)
            _snackbarEvents.emit("Signed in as ${user.name} (${user.role})")
        }
    }

    fun addToCart(product: ProductEntity, variant: String, quantity: Int = 1) {
        val uid = uiState.value.activeUser?.id ?: return
        viewModelScope.launch {
            repository.addToCart(uid, product, variant, quantity)
            _snackbarEvents.emit("${product.title} added to bag")
        }
    }

    fun updateCartQuantity(cartId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartId, newQty)
        }
    }

    fun toggleWishlist(productId: String) {
        val uid = uiState.value.activeUser?.id ?: return
        viewModelScope.launch {
            val added = repository.toggleWishlist(uid, productId)
            _snackbarEvents.emit(if (added) "Saved to Wishlist" else "Removed from Wishlist")
        }
    }

    fun moveWishlistItemToCart(product: ProductEntity) {
        val uid = uiState.value.activeUser?.id ?: return
        val firstVariant = product.variantsCsv.split(",").firstOrNull()?.trim().orEmpty().ifBlank { "Standard" }
        viewModelScope.launch {
            repository.addToCart(uid, product, firstVariant, 1)
            repository.toggleWishlist(uid, product.id)
            _snackbarEvents.emit("Moved to Bag")
        }
    }

    fun applyPromoCode(codeInput: String) {
        val code = codeInput.trim().uppercase()
        viewModelScope.launch {
            when (code) {
                "WELCOME20" -> {
                    _promoCode.value = code
                    _promoPercent.value = 20
                    _snackbarEvents.emit("WELCOME20 applied: 20% off!")
                }
                "GLOBAL10" -> {
                    _promoCode.value = code
                    _promoPercent.value = 10
                    _snackbarEvents.emit("GLOBAL10 applied: 10% off!")
                }
                else -> {
                    _promoCode.value = ""
                    _promoPercent.value = 0
                    _snackbarEvents.emit("Invalid code. Try WELCOME20 or GLOBAL10")
                }
            }
        }
    }

    fun addAddress(
        fullName: String,
        phone: String,
        street: String,
        city: String,
        postalCode: String,
        country: String
    ) {
        val uid = uiState.value.activeUser?.id ?: return
        viewModelScope.launch {
            repository.addAddress(uid, fullName, phone, street, city, postalCode, country)
            _snackbarEvents.emit("Delivery address saved")
        }
    }

    fun selectDefaultAddress(addressId: String) {
        val uid = uiState.value.activeUser?.id ?: return
        viewModelScope.launch {
            repository.selectDefaultAddress(uid, addressId)
        }
    }

    fun processCheckoutAndPlaceOrder(
        paymentMethod: PaymentMethod,
        onSuccess: (String) -> Unit
    ) {
        val state = uiState.value
        val buyer = state.activeUser ?: return
        val address = state.selectedAddress
        if (state.cartItems.isEmpty() || address == null) {
            viewModelScope.launch {
                _snackbarEvents.emit("Please add items and select a delivery address.")
            }
            return
        }

        viewModelScope.launch {
            _isProcessingPayment.value = true
            delay(1300L) // Global payment gateway authorization verification
            val order = repository.placeOrder(
                buyer = buyer,
                cartItems = state.cartItems,
                address = address,
                currency = state.currency,
                discountInr = state.promoDiscountInr,
                paymentMethod = paymentMethod
            )
            _promoCode.value = ""
            _promoPercent.value = 0
            _isProcessingPayment.value = false
            _lastOrderId.value = order.id
            onSuccess(order.id)
        }
    }

    fun clearLastCompletedOrderId() {
        _lastOrderId.value = null
    }

    fun advanceOrderTracking(order: OrderEntity) {
        val current = OrderTrackingStatus.fromString(order.trackingStatus)
        val next = current.nextStatus() ?: return
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, next)
            _snackbarEvents.emit("Order ${order.id} → ${next.name.replace('_', ' ')}")
        }
    }

    fun cancelOrder(order: OrderEntity) {
        val current = OrderTrackingStatus.fromString(order.trackingStatus)
        if (!current.canCancel()) return
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, OrderTrackingStatus.CANCELLED)
            _snackbarEvents.emit("Order ${order.id} cancelled")
        }
    }

    fun markAllNotificationsRead() {
        val uid = uiState.value.activeUser?.id ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(uid)
        }
    }

    fun saveVendorProduct(
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
        val actor = uiState.value.activeUser ?: return
        viewModelScope.launch {
            try {
                repository.createOrUpdateVendorProduct(
                    actor = actor,
                    existingId = existingId,
                    title = title,
                    description = description,
                    category = category,
                    priceInr = priceInr,
                    mrpInr = mrpInr,
                    stockCount = stockCount,
                    imageKey = imageKey,
                    variantsCsv = variantsCsv,
                    isFlashDeal = isFlashDeal
                )
                _snackbarEvents.emit("Product published to marketplace")
            } catch (e: Exception) {
                _snackbarEvents.emit(e.message ?: "Unable to save product")
            }
        }
    }

    fun updateVendorStock(productId: String, newStock: Int) {
        val actor = uiState.value.activeUser ?: return
        viewModelScope.launch {
            try {
                repository.updateProductStock(actor, productId, newStock)
            } catch (e: Exception) {
                _snackbarEvents.emit(e.message ?: "Permission denied")
            }
        }
    }

    fun deleteVendorProduct(productId: String) {
        val actor = uiState.value.activeUser ?: return
        viewModelScope.launch {
            try {
                repository.deleteVendorProduct(actor, productId)
                _snackbarEvents.emit("Product deleted")
            } catch (e: Exception) {
                _snackbarEvents.emit(e.message ?: "Permission denied")
            }
        }
    }

    fun adminChangeUserRole(targetUserId: String, newRole: UserRole) {
        val actor = uiState.value.activeUser ?: return
        viewModelScope.launch {
            try {
                repository.adminSetUserRole(actor, targetUserId, newRole)
                _snackbarEvents.emit("Access level updated to ${newRole.name}")
            } catch (e: Exception) {
                _snackbarEvents.emit(e.message ?: "Admin permission required")
            }
        }
    }

    fun adminToggleProductApproval(productId: String, approved: Boolean) {
        val actor = uiState.value.activeUser ?: return
        viewModelScope.launch {
            try {
                repository.adminSetProductApproval(actor, productId, approved)
                _snackbarEvents.emit(if (approved) "Listing approved" else "Listing suspended")
            } catch (e: Exception) {
                _snackbarEvents.emit(e.message ?: "Admin permission required")
            }
        }
    }

    fun decodeOrderItems(order: OrderEntity) = OrderItemCodec.decode(order.itemsEncoded)
}

private data class UserScopedBundle(
    val prefs: com.example.data.preferences.UserPrefs,
    val user: UserEntity?,
    val cart: List<CartItemWithProduct>,
    val wishlist: Set<String>,
    val addresses: List<AddressEntity>,
    val buyerOrders: List<OrderEntity>,
    val notifications: List<AppNotificationEntity>
)
