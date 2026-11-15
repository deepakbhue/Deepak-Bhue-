package com.example.data.model

enum class UserRole(val level: Int) {
    BUYER(1),
    VENDOR(2),
    ADMIN(3);

    fun canAccessVendorPanel(): Boolean = this == VENDOR || this == ADMIN
    fun canAccessAdminPanel(): Boolean = this == ADMIN

    companion object {
        fun fromString(value: String): UserRole =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: BUYER
    }
}

enum class AuthProvider(val label: String) {
    GOOGLE("Google"),
    META("Meta"),
    APPLE("Apple"),
    LOCAL("Email");

    companion object {
        fun fromString(value: String): AuthProvider =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: LOCAL
    }
}

enum class OrderTrackingStatus(val stepIndex: Int) {
    PLACED(0),
    CONFIRMED(1),
    PACKED(2),
    SHIPPED(3),
    OUT_FOR_DELIVERY(4),
    DELIVERED(5),
    CANCELLED(-1);

    fun nextStatus(): OrderTrackingStatus? = when (this) {
        PLACED -> CONFIRMED
        CONFIRMED -> PACKED
        PACKED -> SHIPPED
        SHIPPED -> OUT_FOR_DELIVERY
        OUT_FOR_DELIVERY -> DELIVERED
        DELIVERED, CANCELLED -> null
    }

    fun canCancel(): Boolean = this == PLACED || this == CONFIRMED || this == PACKED

    companion object {
        fun fromString(value: String): OrderTrackingStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PLACED

        val activeTimeline = listOf(
            PLACED,
            CONFIRMED,
            PACKED,
            SHIPPED,
            OUT_FOR_DELIVERY,
            DELIVERED
        )
    }
}

enum class PaymentMethod {
    GLOBAL_CARD,
    UPI_INSTANT,
    DIGITAL_WALLET,
    COD;

    companion object {
        fun fromString(value: String): PaymentMethod =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: GLOBAL_CARD
    }
}

enum class ProductCategory(val key: String, val displayName: String) {
    ALL("All", "All"),
    FOOD("Food", "Food & Meals"),
    GROCERY("Grocery", "Groceries"),
    SNACKS("Snacks", "Snacks & Cafe"),
    ELECTRONICS("Electronics", "Electronics"),
    FASHION("Fashion", "Fashion"),
    HOME("Home", "Home & Living"),
    BEAUTY("Beauty", "Beauty & Care"),
    ACCESSORIES("Accessories", "Accessories"),
    SPORTS("Sports", "Sports"),
    BOOKS("Books", "Books & Toys");

    fun isExpressFoodOrGrocery(): Boolean =
        this == FOOD || this == GROCERY || this == SNACKS

    companion object {
        fun fromKey(key: String): ProductCategory =
            entries.find { it.key.equals(key, ignoreCase = true) } ?: ALL
    }
}

data class OrderLineItem(
    val productId: String,
    val title: String,
    val vendorId: String,
    val vendorName: String,
    val quantity: Int,
    val unitPriceInr: Double,
    val variant: String,
    val imageKey: String
)

data class CartItemWithProduct(
    val cartId: String,
    val productId: String,
    val title: String,
    val vendorId: String,
    val vendorName: String,
    val priceInr: Double,
    val mrpInr: Double,
    val stockCount: Int,
    val quantity: Int,
    val selectedVariant: String,
    val imageKey: String
)
