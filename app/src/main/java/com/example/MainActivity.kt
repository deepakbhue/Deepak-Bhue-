package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.notifications.OrderNotificationHelper
import com.example.ui.MarketplaceViewModel
import com.example.ui.components.BazaarTopBar
import com.example.ui.components.NotificationsModalSheet
import com.example.ui.components.SocialAuthAndRoleSheet
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VendorPanelScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.BazaarHubTheme

object AppRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_WISHLIST = "wishlist"
    const val ROUTE_ORDERS = "orders"
    const val ROUTE_CART = "cart"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_PRODUCT_DETAIL = "product/{productId}"
    const val ROUTE_VENDOR_PANEL = "vendor_panel"
    const val ROUTE_ADMIN_PANEL = "admin_panel"

    fun productDetail(productId: String) = "product/$productId"
}

private data class TopLevelTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OrderNotificationHelper.ensureChannel(this)
        setContent {
            BazaarHubTheme {
                BazaarHubApp()
            }
        }
    }
}

@Composable
fun BazaarHubApp(viewModel: MarketplaceViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = uiState.strings
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showSocialAuthSheet by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val topLevelTabs = listOf(
        TopLevelTab(
            route = AppRoutes.ROUTE_HOME,
            label = strings.navShop,
            selectedIcon = Icons.Filled.Storefront,
            unselectedIcon = Icons.Outlined.Storefront
        ),
        TopLevelTab(
            route = AppRoutes.ROUTE_WISHLIST,
            label = strings.navWishlist,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
            badgeCount = uiState.wishlistIds.size
        ),
        TopLevelTab(
            route = AppRoutes.ROUTE_ORDERS,
            label = strings.navOrders,
            selectedIcon = Icons.Filled.LocalShipping,
            unselectedIcon = Icons.Outlined.LocalShipping,
            badgeCount = uiState.buyerOrders.count {
                it.trackingStatus != "DELIVERED" && it.trackingStatus != "CANCELLED"
            }
        ),
        TopLevelTab(
            route = AppRoutes.ROUTE_CART,
            label = strings.navCart,
            selectedIcon = Icons.Filled.ShoppingBag,
            unselectedIcon = Icons.Outlined.ShoppingBag,
            badgeCount = uiState.cartCount
        ),
        TopLevelTab(
            route = AppRoutes.ROUTE_PROFILE,
            label = strings.navProfile,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    val isTopLevelScreen = currentRoute in topLevelTabs.map { it.route }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isTopLevelScreen) {
                    BazaarTopBar(
                        strings = strings,
                        activeUser = uiState.activeUser,
                        activeRole = uiState.activeRole,
                        language = uiState.language,
                        currency = uiState.currency,
                        unreadNotifications = uiState.unreadNotificationCount,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        onCurrencySelected = { viewModel.setCurrency(it) },
                        onOpenNotifications = { showNotificationsSheet = true },
                        onOpenSocialAuth = { showSocialAuthSheet = true }
                    )
                }
            },
            bottomBar = {
                if (isTopLevelScreen && !useNavigationRail) {
                    NavigationBar {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (tab.badgeCount > 0) {
                                                Badge { Text("${tab.badgeCount}") }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelScreen && useNavigationRail) {
                    NavigationRail {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (tab.badgeCount > 0) {
                                                Badge { Text("${tab.badgeCount}") }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    }
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.ROUTE_HOME
                    ) {
                        composable(AppRoutes.ROUTE_HOME) {
                            HomeScreen(
                                uiState = uiState,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onProductClick = { product ->
                                    navController.navigate(AppRoutes.productDetail(product.id))
                                },
                                onToggleWishlist = { viewModel.toggleWishlist(it) },
                                onQuickAddToCart = { product ->
                                    val firstVariant = product.variantsCsv.split(",").firstOrNull()?.trim().orEmpty().ifBlank { "Standard" }
                                    viewModel.addToCart(product, firstVariant)
                                }
                            )
                        }

                        composable(AppRoutes.ROUTE_WISHLIST) {
                            WishlistScreen(
                                uiState = uiState,
                                onProductClick = { product ->
                                    navController.navigate(AppRoutes.productDetail(product.id))
                                },
                                onMoveToCart = { viewModel.moveWishlistItemToCart(it) },
                                onRemoveFromWishlist = { viewModel.toggleWishlist(it) },
                                onExploreShop = { navigateTopLevel(AppRoutes.ROUTE_HOME) }
                            )
                        }

                        composable(AppRoutes.ROUTE_ORDERS) {
                            OrdersScreen(
                                uiState = uiState,
                                onAdvanceTracking = { viewModel.advanceOrderTracking(it) },
                                onCancelOrder = { viewModel.cancelOrder(it) },
                                onExploreShop = { navigateTopLevel(AppRoutes.ROUTE_HOME) }
                            )
                        }

                        composable(AppRoutes.ROUTE_CART) {
                            CartCheckoutScreen(
                                uiState = uiState,
                                onUpdateCartQuantity = { cartId, qty -> viewModel.updateCartQuantity(cartId, qty) },
                                onApplyPromo = { viewModel.applyPromoCode(it) },
                                onSelectAddress = { viewModel.selectDefaultAddress(it) },
                                onAddAddress = { name, phone, street, city, postal, country ->
                                    viewModel.addAddress(name, phone, street, city, postal, country)
                                },
                                onPlaceOrder = { method, onSuccess ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !OrderNotificationHelper.hasNotificationPermission(context)
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    viewModel.processCheckoutAndPlaceOrder(method, onSuccess)
                                },
                                onNavigateToOrders = { navigateTopLevel(AppRoutes.ROUTE_ORDERS) },
                                onExploreShop = { navigateTopLevel(AppRoutes.ROUTE_HOME) }
                            )
                        }

                        composable(AppRoutes.ROUTE_PROFILE) {
                            ProfileScreen(
                                uiState = uiState,
                                onOpenSocialAuth = { showSocialAuthSheet = true },
                                onOpenVendorPanel = { navController.navigate(AppRoutes.ROUTE_VENDOR_PANEL) },
                                onOpenAdminPanel = { navController.navigate(AppRoutes.ROUTE_ADMIN_PANEL) },
                                onSelectLanguage = { viewModel.setLanguage(it) },
                                onSelectCurrency = { viewModel.setCurrency(it) },
                                onToggleNotifications = { viewModel.setNotificationsEnabled(it) }
                            )
                        }

                        composable(
                            route = AppRoutes.ROUTE_PRODUCT_DETAIL,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
                            val product = uiState.approvedProducts.firstOrNull { it.id == productId }
                            if (product != null) {
                                ProductDetailScreen(
                                    product = product,
                                    strings = strings,
                                    currency = uiState.currency,
                                    isWishlisted = product.id in uiState.wishlistIds,
                                    onBack = { navController.popBackStack() },
                                    onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                                    onAddToCart = { variant -> viewModel.addToCart(product, variant) },
                                    onBuyNow = { variant ->
                                        viewModel.addToCart(product, variant)
                                        navigateTopLevel(AppRoutes.ROUTE_CART)
                                    }
                                )
                            } else {
                                LaunchedEffect(productId) {
                                    navController.popBackStack()
                                }
                            }
                        }

                        composable(AppRoutes.ROUTE_VENDOR_PANEL) {
                            VendorPanelScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onSaveProduct = { existingId, title, desc, cat, price, mrp, stock, imageKey, variants, flash ->
                                    viewModel.saveVendorProduct(
                                        existingId, title, desc, cat, price, mrp, stock, imageKey, variants, flash
                                    )
                                },
                                onUpdateStock = { productId, stock -> viewModel.updateVendorStock(productId, stock) },
                                onDeleteProduct = { productId -> viewModel.deleteVendorProduct(productId) },
                                onAdvanceOrderStatus = { viewModel.advanceOrderTracking(it) }
                            )
                        }

                        composable(AppRoutes.ROUTE_ADMIN_PANEL) {
                            AdminPanelScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onChangeUserRole = { userId, role -> viewModel.adminChangeUserRole(userId, role) },
                                onToggleProductApproval = { prodId, approved -> viewModel.adminToggleProductApproval(prodId, approved) },
                                onAdvanceOrderStatus = { viewModel.advanceOrderTracking(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationsModalSheet(
            strings = strings,
            notifications = uiState.notifications,
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showSocialAuthSheet) {
        SocialAuthAndRoleSheet(
            strings = strings,
            activeUser = uiState.activeUser,
            allUsers = uiState.allUsers,
            onSwitchUser = { viewModel.switchUser(it) },
            onSocialSignIn = { name, email, provider, role, shopName ->
                viewModel.signInWithSocial(name, email, provider, role, shopName)
            },
            onDismiss = { showSocialAuthSheet = false }
        )
    }
}
