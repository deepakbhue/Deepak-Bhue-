package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val authProvider: String,
    val role: String,
    val vendorShopName: String = "",
    val isVerifiedVendor: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val vendorName: String,
    val title: String,
    val description: String,
    val category: String,
    val priceInr: Double,
    val mrpInr: Double,
    val rating: Float,
    val reviewCount: Int,
    val stockCount: Int,
    val imageKey: String,
    val variantsCsv: String,
    val isApproved: Boolean = true,
    val isFlashDeal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val quantity: Int,
    val selectedVariant: String
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val buyerId: String,
    val buyerName: String,
    val shippingAddressSummary: String,
    val itemsEncoded: String,
    val vendorIdsCsv: String,
    val totalInr: Double,
    val discountInr: Double,
    val currencyCode: String,
    val paidAmountFormatted: String,
    val paymentMethod: String,
    val paymentGatewayRef: String,
    val paymentStatus: String,
    val trackingStatus: String,
    val courierName: String,
    val trackingCode: String,
    val estimatedDelivery: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val orderId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
