package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.OrderLineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        WishlistEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        AppNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BazaarDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: BazaarDatabase? = null

        fun getInstance(context: Context): BazaarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BazaarDatabase::class.java,
                    "bazaarhub_marketplace.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object OrderItemCodec {
    private const val FIELD_SEP = "::"
    private const val ITEM_SEP = ";;"

    fun encode(items: List<OrderLineItem>): String {
        return items.joinToString(ITEM_SEP) { item ->
            listOf(
                item.productId.sanitize(),
                item.title.sanitize(),
                item.vendorId.sanitize(),
                item.vendorName.sanitize(),
                item.quantity.toString(),
                item.unitPriceInr.toString(),
                item.variant.sanitize(),
                item.imageKey.sanitize()
            ).joinToString(FIELD_SEP)
        }
    }

    fun decode(encoded: String): List<OrderLineItem> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(ITEM_SEP).mapNotNull { chunk ->
            val parts = chunk.split(FIELD_SEP)
            if (parts.size < 8) return@mapNotNull null
            OrderLineItem(
                productId = parts[0],
                title = parts[1],
                vendorId = parts[2],
                vendorName = parts[3],
                quantity = parts[4].toIntOrNull() ?: 1,
                unitPriceInr = parts[5].toDoubleOrNull() ?: 0.0,
                variant = parts[6],
                imageKey = parts[7]
            )
        }
    }

    private fun String.sanitize(): String =
        replace(FIELD_SEP, " ").replace(ITEM_SEP, " ")
}

object SeedDataProvider {
    const val DEFAULT_BUYER_ID = "user_buyer_1"
    const val DEFAULT_VENDOR_ID = "user_vendor_1"
    const val DEFAULT_ADMIN_ID = "user_admin_1"

    suspend fun ensureSeeded(db: BazaarDatabase) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        val users = listOf(
            UserEntity(
                id = DEFAULT_BUYER_ID,
                name = "Aarav Mehta",
                email = "aarav.mehta@bazaarhub.demo",
                authProvider = "GOOGLE",
                role = "BUYER",
                vendorShopName = "",
                isVerifiedVendor = false,
                createdAt = now - 86_400_000L * 10
            ),
            UserEntity(
                id = DEFAULT_VENDOR_ID,
                name = "Priya Sharma",
                email = "priya@luxecraft.store",
                authProvider = "GOOGLE",
                role = "VENDOR",
                vendorShopName = "LuxeCraft Global Studio",
                isVerifiedVendor = true,
                createdAt = now - 86_400_000L * 9
            ),
            UserEntity(
                id = DEFAULT_ADMIN_ID,
                name = "Vikram Rao",
                email = "vikram.ops@bazaarhub.admin",
                authProvider = "GOOGLE",
                role = "ADMIN",
                vendorShopName = "Bazar Hub HQ",
                isVerifiedVendor = true,
                createdAt = now - 86_400_000L * 12
            ),
            UserEntity(
                id = "user_vendor_2",
                name = "Elena Vance",
                email = "elena@nordicaudio.co",
                authProvider = "APPLE",
                role = "VENDOR",
                vendorShopName = "Nordic Sound Lab",
                isVerifiedVendor = true,
                createdAt = now - 86_400_000L * 6
            ),
            UserEntity(
                id = "user_vendor_3",
                name = "Chef Kabir",
                email = "kabir@spiceroute.food",
                authProvider = "GOOGLE",
                role = "VENDOR",
                vendorShopName = "SpiceRoute Kitchen & Mart",
                isVerifiedVendor = true,
                createdAt = now - 86_400_000L * 5
            )
        )
        db.userDao().insertUsersIfMissing(users)

        val products = listOf(
            ProductEntity(
                id = "prod_1",
                vendorId = "user_vendor_2",
                vendorName = "Nordic Sound Lab",
                title = "AeroPulse Pro ANC Wireless Headphones",
                description = "Studio-grade hybrid active noise cancellation with 48-hour battery life, multipoint Bluetooth 5.4, and memory-foam acoustic cushions for global travelers.",
                category = "Electronics",
                priceInr = 6499.0,
                mrpInr = 11999.0,
                rating = 4.8f,
                reviewCount = 1248,
                stockCount = 34,
                imageKey = "drawable:img_prod_electronics",
                variantsCsv = "Obsidian Black,Titanium Silver,Sage Mist",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 500_000L
            ),
            ProductEntity(
                id = "prod_2",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Emerald Linen Tailored Blazer & Tote Set",
                description = "Breathable European flax linen blazer paired with a handcrafted woven vegan-leather crossbody bag. Designed for versatile day-to-evening elegance.",
                category = "Fashion",
                priceInr = 3299.0,
                mrpInr = 5999.0,
                rating = 4.7f,
                reviewCount = 864,
                stockCount = 22,
                imageKey = "drawable:img_prod_fashion",
                variantsCsv = "S,M,L,XL",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 450_000L
            ),
            ProductEntity(
                id = "prod_3",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Artisan Terracotta Vase & Brass Lamp Duo",
                description = "Hand-thrown matte terracotta vessel paired with a brushed solid-brass bedside ambient lamp and organic cotton throw aesthetic.",
                category = "Home",
                priceInr = 2499.0,
                mrpInr = 4499.0,
                rating = 4.9f,
                reviewCount = 532,
                stockCount = 18,
                imageKey = "drawable:img_prod_home",
                variantsCsv = "Warm Terracotta,Sandstone Ivory",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 400_000L
            ),
            ProductEntity(
                id = "prod_4",
                vendorId = "user_vendor_2",
                vendorName = "Nordic Sound Lab",
                title = "ChronoTi AMOLED Titanium Smartwatch",
                description = "Sapphire glass display, dual-band GPS, 14-day battery, NFC tap-to-pay readiness, and continuous wellness telemetry.",
                category = "Electronics",
                priceInr = 8999.0,
                mrpInr = 14999.0,
                rating = 4.6f,
                reviewCount = 915,
                stockCount = 15,
                imageKey = "drawable:img_prod_electronics",
                variantsCsv = "42mm Brushed Steel,46mm Matte Titanium",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 350_000L
            ),
            ProductEntity(
                id = "prod_5",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Heritage Woven Leather Crossbody Bag",
                description = "Artisanal braided flap satchel with antique brass buckle, RFID-shielded card pocket, and adjustable shoulder strap.",
                category = "Accessories",
                priceInr = 1899.0,
                mrpInr = 3499.0,
                rating = 4.8f,
                reviewCount = 1402,
                stockCount = 41,
                imageKey = "drawable:img_prod_fashion",
                variantsCsv = "Cognac Tan,Espresso Brown,Olive",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 300_000L
            ),
            ProductEntity(
                id = "prod_6",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Nordic Cotton Waffle Throw & Cushion Kit",
                description = "450 GSM OEKO-TEX certified organic cotton throw blanket with textured tassel edges for cozy living rooms.",
                category = "Home",
                priceInr = 1299.0,
                mrpInr = 2299.0,
                rating = 4.5f,
                reviewCount = 388,
                stockCount = 55,
                imageKey = "drawable:img_prod_home",
                variantsCsv = "Oatmeal Cream,Forest Sage",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 250_000L
            ),
            ProductEntity(
                id = "prod_7",
                vendorId = "user_vendor_2",
                vendorName = "Nordic Sound Lab",
                title = "Luxe Gold-Rim True Wireless Earbuds",
                description = "Spatial audio drivers, crystal-clear 6-mic ENC calling, wireless charging case, and IPX5 splash resistance.",
                category = "Electronics",
                priceInr = 2799.0,
                mrpInr = 4999.0,
                rating = 4.7f,
                reviewCount = 2104,
                stockCount = 62,
                imageKey = "drawable:img_hero_mega_sale",
                variantsCsv = "Emerald & Gold,Obsidian & Gold",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 200_000L
            ),
            ProductEntity(
                id = "prod_8",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Polarized Tortoiseshell Resort Sunglasses",
                description = "UV400 polarized TAC lenses in lightweight Italian acetate frames, complete with velvet travel pouch.",
                category = "Accessories",
                priceInr = 999.0,
                mrpInr = 1999.0,
                rating = 4.6f,
                reviewCount = 670,
                stockCount = 75,
                imageKey = "drawable:img_prod_fashion",
                variantsCsv = "Amber Tortoise,Jet Black",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 150_000L
            ),
            // Food & Meals
            ProductEntity(
                id = "prod_9",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Royal Hyderabadi Dum Biryani Bowl",
                description = "Slow-cooked aged saffron basmati rice layered with aromatic spices, caramelized onions, mint raita, and salan. Delivered hot in 25 mins.",
                category = "Food",
                priceInr = 349.0,
                mrpInr = 499.0,
                rating = 4.9f,
                reviewCount = 3420,
                stockCount = 120,
                imageKey = "art:food_biryani",
                variantsCsv = "Veg Paneer,Classic Chicken,Mutton Feast",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 140_000L
            ),
            ProductEntity(
                id = "prod_10",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Wood-Fired Neapolitan Margherita Pizza",
                description = "72-hour fermented sourdough crust topped with San Marzano tomato sauce, fresh buffalo mozzarella, and sweet basil.",
                category = "Food",
                priceInr = 449.0,
                mrpInr = 649.0,
                rating = 4.8f,
                reviewCount = 1890,
                stockCount = 85,
                imageKey = "art:food_pizza",
                variantsCsv = "Medium 10\",Large 12\",Cheese Burst",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 135_000L
            ),
            ProductEntity(
                id = "prod_11",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Maharaja Ghar-Jaisa Deluxe Veg Thali",
                description = "Complete wholesome meal: Paneer Butter Masala, Dal Makhani, Jeera Pulao, 3 Butter Naan, Fresh Salad, Pickle & Gulab Jamun.",
                category = "Food",
                priceInr = 299.0,
                mrpInr = 420.0,
                rating = 4.8f,
                reviewCount = 2640,
                stockCount = 150,
                imageKey = "art:food_thali",
                variantsCsv = "Regular Thali,Jain Thali,Extra Sweet",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 130_000L
            ),
            ProductEntity(
                id = "prod_12",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Belgian Dark Chocolate Molten Lava Cake",
                description = "Warm 70% dark chocolate ganache center baked fresh to order, served with Madagascar vanilla bean glaze.",
                category = "Food",
                priceInr = 189.0,
                mrpInr = 260.0,
                rating = 4.9f,
                reviewCount = 1530,
                stockCount = 95,
                imageKey = "art:food_dessert",
                variantsCsv = "Single Serve,Pack of 2,Party Box of 4",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 125_000L
            ),
            // Groceries & Fresh
            ProductEntity(
                id = "prod_13",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Organic Farm Fresh Fruit & Avocado Basket",
                description = "Hand-picked daily harvest: Hass Avocados, Honeycrisp Apples, Nagpur Oranges, and Seedless Green Grapes.",
                category = "Grocery",
                priceInr = 599.0,
                mrpInr = 850.0,
                rating = 4.7f,
                reviewCount = 740,
                stockCount = 60,
                imageKey = "art:grocery_fresh",
                variantsCsv = "2kg Family Pack,4kg Value Crate",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 120_000L
            ),
            ProductEntity(
                id = "prod_14",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Royal Aged Basmati Rice & Cold-Pressed Oil Combo",
                description = "5kg extra-long-grain Himalayan Basmati Rice paired with 1L cold-pressed extra virgin olive & mustard kitchen pack.",
                category = "Grocery",
                priceInr = 899.0,
                mrpInr = 1299.0,
                rating = 4.8f,
                reviewCount = 1120,
                stockCount = 110,
                imageKey = "art:grocery_staples",
                variantsCsv = "5kg + 1L Combo,10kg Pantry Saver",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 115_000L
            ),
            // Snacks & Cafe
            ProductEntity(
                id = "prod_15",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Chikmagalur Arabica Coffee & Masala Chai Duo",
                description = "Single-origin medium roast Arabica coffee beans paired with Assam spiced cardamom masala chai blend.",
                category = "Snacks",
                priceInr = 499.0,
                mrpInr = 749.0,
                rating = 4.9f,
                reviewCount = 980,
                stockCount = 80,
                imageKey = "art:snacks_cafe",
                variantsCsv = "Whole Bean + Loose Tea,Ground + Tea Bags",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 110_000L
            ),
            ProductEntity(
                id = "prod_16",
                vendorId = "user_vendor_3",
                vendorName = "SpiceRoute Kitchen & Mart",
                title = "Kashmiri Roasted Almond, Cashew & Makhana Tin",
                description = "Crunchy Himalayan pink-salt roasted dry fruits and peri-peri foxnuts (makhana) in airtight freshness tins.",
                category = "Snacks",
                priceInr = 549.0,
                mrpInr = 899.0,
                rating = 4.7f,
                reviewCount = 615,
                stockCount = 70,
                imageKey = "art:snacks_bakery",
                variantsCsv = "400g Assorted,800g Festive Box",
                isApproved = true,
                isFlashDeal = true,
                createdAt = now - 105_000L
            ),
            // Beauty & Care
            ProductEntity(
                id = "prod_17",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Vitamin C Glow Serum & Botanical Cleanser",
                description = "Dermatologist-tested 15% Vitamin C + Hyaluronic Acid radiance serum paired with gentle green-tea face wash.",
                category = "Beauty",
                priceInr = 799.0,
                mrpInr = 1399.0,
                rating = 4.8f,
                reviewCount = 1650,
                stockCount = 90,
                imageKey = "art:beauty_care",
                variantsCsv = "30ml Serum,Serum + Cleanser Kit",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 100_000L
            ),
            // Sports & Fitness
            ProductEntity(
                id = "prod_18",
                vendorId = DEFAULT_VENDOR_ID,
                vendorName = "LuxeCraft Global Studio",
                title = "Pro-Grip Natural Cork Yoga Mat & Resistance Bands",
                description = "6mm non-slip organic cork and natural rubber exercise mat with carry strap and 3 fabric booty/strength bands.",
                category = "Sports",
                priceInr = 1699.0,
                mrpInr = 2999.0,
                rating = 4.7f,
                reviewCount = 490,
                stockCount = 45,
                imageKey = "art:sports_gear",
                variantsCsv = "Natural Cork,Midnight Slate",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 95_000L
            ),
            // Books & Toys
            ProductEntity(
                id = "prod_19",
                vendorId = "user_vendor_2",
                vendorName = "Nordic Sound Lab",
                title = "STEM Robotics & Solar Science Explorer Kit",
                description = "Build 12 working solar-powered robots and circuits. Screen-free educational gift set for ages 8+ with illustrated manual.",
                category = "Books",
                priceInr = 1199.0,
                mrpInr = 1999.0,
                rating = 4.9f,
                reviewCount = 830,
                stockCount = 50,
                imageKey = "art:books_kids",
                variantsCsv = "12-in-1 Robotics,Deluxe Coding Edition",
                isApproved = true,
                isFlashDeal = false,
                createdAt = now - 90_000L
            )
        )
        db.productDao().insertProductsIfMissing(products)

        val addresses = listOf(
            AddressEntity(
                id = "addr_1",
                userId = DEFAULT_BUYER_ID,
                fullName = "Aarav Mehta",
                phone = "+91 98201 44510",
                street = "402 Emerald Heights, Linking Road, Bandra West",
                city = "Mumbai",
                postalCode = "400050",
                country = "India",
                isDefault = true
            ),
            AddressEntity(
                id = "addr_2",
                userId = DEFAULT_BUYER_ID,
                fullName = "Aarav Mehta (Global Office)",
                phone = "+1 415 890 3312",
                street = "548 Market St, Suite 420",
                city = "San Francisco, CA",
                postalCode = "94104",
                country = "United States",
                isDefault = false
            )
        )
        db.addressDao().insertAddressesIfMissing(addresses)

        val sampleItems = listOf(
            OrderLineItem(
                productId = "prod_1",
                title = "AeroPulse Pro ANC Wireless Headphones",
                vendorId = "user_vendor_2",
                vendorName = "Nordic Sound Lab",
                quantity = 1,
                unitPriceInr = 6499.0,
                variant = "Obsidian Black",
                imageKey = "drawable:img_prod_electronics"
            )
        )
        val sampleOrder = OrderEntity(
            id = "BH-849201",
            buyerId = DEFAULT_BUYER_ID,
            buyerName = "Aarav Mehta",
            shippingAddressSummary = "402 Emerald Heights, Bandra West, Mumbai 400050, India",
            itemsEncoded = OrderItemCodec.encode(sampleItems),
            vendorIdsCsv = "user_vendor_2",
            totalInr = 6499.0,
            discountInr = 0.0,
            currencyCode = "INR",
            paidAmountFormatted = "₹6,499",
            paymentMethod = "GLOBAL_CARD",
            paymentGatewayRef = "TXN-GLB-904812",
            paymentStatus = "PAID",
            trackingStatus = "SHIPPED",
            courierName = "DHL Global Express",
            trackingCode = "DHL-IN-77492018",
            estimatedDelivery = "Arriving Tomorrow by 6:00 PM",
            createdAt = now - 3_600_000L * 18,
            updatedAt = now - 3_600_000L * 2
        )
        db.orderDao().insertOrdersIfMissing(listOf(sampleOrder))

        val initialNotifications = listOf(
            AppNotificationEntity(
                id = "notif_seed_1",
                userId = DEFAULT_BUYER_ID,
                title = "Shipment in Transit • BH-849201",
                message = "AeroPulse Pro ANC Wireless Headphones cleared Mumbai Sortation Hub via DHL Global Express.",
                orderId = "BH-849201",
                isRead = false,
                createdAt = now - 3_600_000L * 2
            ),
            AppNotificationEntity(
                id = "notif_seed_2",
                userId = DEFAULT_BUYER_ID,
                title = "Global Mega Sale Live!",
                message = "Use promo code WELCOME20 at checkout for 20% off across all verified vendors.",
                orderId = null,
                isRead = false,
                createdAt = now - 3_600_000L * 5
            )
        )
        db.notificationDao().insertNotificationsIfMissing(initialNotifications)
    }
}
