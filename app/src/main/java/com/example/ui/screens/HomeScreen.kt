package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.i18n.formatPrice
import com.example.data.local.ProductEntity
import com.example.data.model.ProductCategory
import com.example.ui.MarketplaceUiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductGridCard
import com.example.ui.components.ProductImage
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.TerracottaAccent

@Composable
fun HomeScreen(
    uiState: MarketplaceUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (ProductCategory) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onQuickAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = uiState.strings

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        val productRows = uiState.filteredProducts.chunked(columns)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search products"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("home_search_input")
                )
            }

            // Hero Banner (shown when search is empty)
            if (uiState.searchQuery.isBlank()) {
                item {
                    HeroSaleBanner(
                        badge = strings.heroBadge,
                        title = strings.heroTitle,
                        subtitle = strings.heroSubtitle,
                        cta = strings.heroCta,
                        onCtaClick = { onSelectCategory(ProductCategory.ELECTRONICS) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Category Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ProductCategory.entries) { category ->
                        val selected = category == uiState.selectedCategory
                        val label = if (category == ProductCategory.ALL) {
                            strings.allCategories
                        } else {
                            category.displayName
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("category_chip_${category.key}")
                        )
                    }
                }
            }

            // Flash Deals horizontal rail
            if (uiState.searchQuery.isBlank() &&
                uiState.selectedCategory == ProductCategory.ALL &&
                uiState.flashDeals.isNotEmpty()
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = TerracottaAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.flashDeals,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.flashDeals, key = { "flash_${it.id}" }) { deal ->
                                FlashDealCompactCard(
                                    product = deal,
                                    priceFormatted = formatPrice(deal.priceInr, uiState.currency),
                                    onClick = { onProductClick(deal) }
                                )
                            }
                        }
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = strings.trendingNow,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Product Grid or Empty State
            if (productRows.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Filled.SearchOff,
                        title = "No matching products",
                        subtitle = "Try another keyword or clear your category filter."
                    )
                }
            } else {
                items(productRows) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { product ->
                            ProductGridCard(
                                product = product,
                                currency = uiState.currency,
                                isWishlisted = product.id in uiState.wishlistIds,
                                onProductClick = { onProductClick(product) },
                                onToggleWishlist = { onToggleWishlist(product.id) },
                                onQuickAddToCart = { onQuickAddToCart(product) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSaleBanner(
    badge: String,
    title: String,
    subtitle: String,
    cta: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(188.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onCtaClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_mega_sale),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            EmeraldDeep.copy(alpha = 0.94f),
                            EmeraldDeep.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = SaffronGold,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Button(
                onClick = onCtaClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = EmeraldDeep
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = cta,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FlashDealCompactCard(
    product: ProductEntity,
    priceFormatted: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .testTag("flash_deal_${product.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ProductImage(
                imageKey = product.imageKey,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = priceFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaAccent
                )
            }
        }
    }
}
