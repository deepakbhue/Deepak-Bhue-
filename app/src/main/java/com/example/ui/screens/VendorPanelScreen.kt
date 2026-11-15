package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.i18n.formatPrice
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.model.OrderTrackingStatus
import com.example.data.model.ProductCategory
import com.example.ui.MarketplaceUiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrderStatusPill
import com.example.ui.components.ProductImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPanelScreen(
    uiState: MarketplaceUiState,
    onBack: () -> Unit,
    onSaveProduct: (
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
    ) -> Unit,
    onUpdateStock: (productId: String, newStock: Int) -> Unit,
    onDeleteProduct: (productId: String) -> Unit,
    onAdvanceOrderStatus: (OrderEntity) -> Unit
) {
    val strings = uiState.strings
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProductEditor by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.vendorPanelTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("vendor_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.activeRole.canAccessVendorPanel()) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductEditor = true
                    },
                    modifier = Modifier.testTag("vendor_add_product_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addProduct)
                }
            }
        }
    ) { innerPadding ->
        if (!uiState.activeRole.canAccessVendorPanel()) {
            EmptyStateView(
                icon = Icons.Filled.Lock,
                title = strings.accessDeniedTitle,
                subtitle = strings.accessDeniedMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // KPI Row
            val vendorSales = uiState.vendorOrders.sumOf { it.totalInr }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Active SKUs",
                    value = "${uiState.vendorProducts.size}",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Orders",
                    value = "${uiState.vendorOrders.size}",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Revenue",
                    value = formatPrice(vendorSales, uiState.currency),
                    modifier = Modifier.weight(1.3f)
                )
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${strings.myCatalog} (${uiState.vendorProducts.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${strings.vendorOrders} (${uiState.vendorOrders.size})") }
                )
            }

            if (selectedTab == 0) {
                if (uiState.vendorProducts.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Filled.Storefront,
                        title = "No products listed",
                        subtitle = "Tap + to publish your first product to BazaarHub."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.vendorProducts, key = { it.id }) { prod ->
                            VendorProductRow(
                                product = prod,
                                priceText = formatPrice(prod.priceInr, uiState.currency),
                                onIncrementStock = { onUpdateStock(prod.id, prod.stockCount + 5) },
                                onEdit = {
                                    editingProduct = prod
                                    showProductEditor = true
                                },
                                onDelete = { onDeleteProduct(prod.id) }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.vendorOrders, key = { it.id }) { order ->
                        val status = OrderTrackingStatus.fromString(order.trackingStatus)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${order.id} • ${order.buyerName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(order.paidAmountFormatted, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                    OrderStatusPill(status)
                                }
                                Text(order.shippingAddressSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                status.nextStatus()?.let { next ->
                                    Button(
                                        onClick = { onAdvanceOrderStatus(order) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Advance to ${next.name.replace('_', ' ')}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProductEditor) {
        AddVendorProductSheet(
            existingProduct = editingProduct,
            onSave = { title, desc, cat, price, mrp, stock, imageKey, variants, flash ->
                onSaveProduct(editingProduct?.id, title, desc, cat, price, mrp, stock, imageKey, variants, flash)
                showProductEditor = false
                editingProduct = null
            },
            onDismiss = {
                showProductEditor = false
                editingProduct = null
            }
        )
    }
}

@Composable
private fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun VendorProductRow(
    product: ProductEntity,
    priceText: String,
    onIncrementStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                imageKey = product.imageKey,
                contentDescription = product.title,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("$priceText • Stock: ${product.stockCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (product.isApproved) "Approved & Live" else "Suspended by Admin",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (product.isApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onIncrementStock, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("+5 Stock", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Edit", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Delete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVendorProductSheet(
    existingProduct: ProductEntity?,
    onSave: (String, String, String, Double, Double, Int, String, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(existingProduct?.id) { mutableStateOf(existingProduct?.title ?: "") }
    var description by remember(existingProduct?.id) { mutableStateOf(existingProduct?.description ?: "") }
    var selectedCategory by remember(existingProduct?.id) {
        mutableStateOf(
            existingProduct?.let { ProductCategory.fromKey(it.category) }?.takeIf { it != ProductCategory.ALL }
                ?: ProductCategory.FASHION
        )
    }
    var priceInr by remember(existingProduct?.id) { mutableStateOf(existingProduct?.priceInr?.toInt()?.toString() ?: "1499") }
    var mrpInr by remember(existingProduct?.id) { mutableStateOf(existingProduct?.mrpInr?.toInt()?.toString() ?: "2499") }
    var stock by remember(existingProduct?.id) { mutableStateOf(existingProduct?.stockCount?.toString() ?: "25") }
    var variants by remember(existingProduct?.id) { mutableStateOf(existingProduct?.variantsCsv ?: "S,M,L,XL") }
    var isFlashDeal by remember(existingProduct?.id) { mutableStateOf(existingProduct?.isFlashDeal ?: false) }
    var selectedImageKey by remember(existingProduct?.id) { mutableStateOf(existingProduct?.imageKey ?: "drawable:img_prod_fashion") }

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageKey = uri.toString()
            }
        }
    )

    val presetArtwork = listOf(
        "Food Meal" to "art:food_biryani",
        "Pizza" to "art:food_pizza",
        "Grocery" to "art:grocery_fresh",
        "Snacks" to "art:snacks_cafe",
        "Fashion" to "drawable:img_prod_fashion",
        "Electronics" to "drawable:img_prod_electronics",
        "Home" to "drawable:img_prod_home",
        "Beauty" to "art:beauty_care",
        "Sports" to "art:sports_gear",
        "Books" to "art:books_kids"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (existingProduct == null) "Publish New Product" else "Edit Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductImage(
                        imageKey = selectedImageKey,
                        contentDescription = "Selected product image",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick From Gallery")
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(presetArtwork) { (label, key) ->
                                FilterChip(
                                    selected = selectedImageKey == key,
                                    onClick = { selectedImageKey = key },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProductCategory.entries.filter { it != ProductCategory.ALL }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName) }
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceInr,
                        onValueChange = { priceInr = it },
                        label = { Text("Price (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mrpInr,
                        onValueChange = { mrpInr = it },
                        label = { Text("MRP (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = variants,
                    onValueChange = { variants = it },
                    label = { Text("Variants (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Feature in Flash Deals", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isFlashDeal, onCheckedChange = { isFlashDeal = it })
                }
            }
            item {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                title,
                                description.ifBlank { "Verified global vendor catalog item." },
                                selectedCategory.key,
                                priceInr.toDoubleOrNull() ?: 999.0,
                                mrpInr.toDoubleOrNull() ?: 1499.0,
                                stock.toIntOrNull() ?: 10,
                                selectedImageKey,
                                variants,
                                isFlashDeal
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Publish Product", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
