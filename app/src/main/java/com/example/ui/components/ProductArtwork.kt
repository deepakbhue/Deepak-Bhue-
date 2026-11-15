package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.i18n.CurrencyCode
import com.example.core.i18n.discountPercent
import com.example.core.i18n.formatPrice
import com.example.data.local.ProductEntity
import com.example.data.model.OrderTrackingStatus
import com.example.data.model.UserRole
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintOnContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SaffronOnContainer
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerracottaAccent
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaOnContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer

import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun ProductImage(
    imageKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val drawableId = when (imageKey) {
        "drawable:img_prod_electronics" -> R.drawable.img_prod_electronics
        "drawable:img_prod_fashion" -> R.drawable.img_prod_fashion
        "drawable:img_prod_home" -> R.drawable.img_prod_home
        "drawable:img_hero_mega_sale" -> R.drawable.img_hero_mega_sale
        else -> null
    }

    if (drawableId != null) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (imageKey.startsWith("content://") || imageKey.startsWith("file://") || imageKey.startsWith("http")) {
        AsyncImage(
            model = imageKey,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (imageKey.startsWith("art:")) {
        CategoryStudioArtwork(
            artKey = imageKey,
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.img_prod_fashion),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
private fun CategoryStudioArtwork(
    artKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val spec = when (artKey) {
        "art:food_biryani" -> ArtSpec(
            colors = listOf(Color(0xFF7C2D12), Color(0xFFEA580C), Color(0xFFF59E0B)),
            accent = Color(0xFFFFEDD5),
            icon = Icons.Filled.RiceBowl,
            tag = "HOT MEAL"
        )
        "art:food_pizza" -> ArtSpec(
            colors = listOf(Color(0xFF991B1B), Color(0xFFDC2626), Color(0xFFFB923C)),
            accent = Color(0xFFFEF2F2),
            icon = Icons.Filled.LocalPizza,
            tag = "WOOD-FIRED"
        )
        "art:food_thali" -> ArtSpec(
            colors = listOf(Color(0xFF14532D), Color(0xFF15803D), Color(0xFFF59E0B)),
            accent = Color(0xFFDCFCE7),
            icon = Icons.Filled.Restaurant,
            tag = "CHEF SPECIAL"
        )
        "art:food_dessert" -> ArtSpec(
            colors = listOf(Color(0xFF4A1D43), Color(0xFF9D174D), Color(0xFFF472B6)),
            accent = Color(0xFFFCE7F3),
            icon = Icons.Filled.Cake,
            tag = "BAKERY"
        )
        "art:grocery_fresh" -> ArtSpec(
            colors = listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF34D399)),
            accent = Color(0xFFD1FAE5),
            icon = Icons.Filled.Eco,
            tag = "FARM FRESH"
        )
        "art:grocery_staples" -> ArtSpec(
            colors = listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFFBBF24)),
            accent = Color(0xFFFEF3C7),
            icon = Icons.Filled.LocalGroceryStore,
            tag = "PANTRY"
        )
        "art:snacks_cafe" -> ArtSpec(
            colors = listOf(Color(0xFF3E2723), Color(0xFF6D4C41), Color(0xFFD7CCC8)),
            accent = Color(0xFFFFF8E1),
            icon = Icons.Filled.Coffee,
            tag = "ARTISAN CAFE"
        )
        "art:snacks_bakery" -> ArtSpec(
            colors = listOf(Color(0xFF713F12), Color(0xFFCA8A04), Color(0xFFFDE047)),
            accent = Color(0xFFFEF9C3),
            icon = Icons.Filled.BakeryDining,
            tag = "SNACKS"
        )
        "art:beauty_care" -> ArtSpec(
            colors = listOf(Color(0xFF831843), Color(0xFFDB2777), Color(0xFFFBCFE8)),
            accent = Color(0xFFFDF2F8),
            icon = Icons.Filled.Spa,
            tag = "SKINCARE"
        )
        "art:sports_gear" -> ArtSpec(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1D4ED8), Color(0xFF38BDF8)),
            accent = Color(0xFFE0F2FE),
            icon = Icons.Filled.FitnessCenter,
            tag = "FITNESS"
        )
        else -> ArtSpec(
            colors = listOf(Color(0xFF312E81), Color(0xFF4F46E5), Color(0xFFA5B4FC)),
            accent = Color(0xFFEEF2FF),
            icon = Icons.Filled.AutoStories,
            tag = "CURATED"
        )
    }

    Box(
        modifier = modifier.background(Brush.linearGradient(spec.colors)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = spec.accent.copy(alpha = 0.14f),
                radius = size.minDimension * 0.44f,
                center = Offset(size.width * 0.85f, size.height * 0.2f)
            )
            drawCircle(
                color = spec.accent.copy(alpha = 0.12f),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * 0.15f, size.height * 0.85f)
            )
            drawLine(
                color = spec.accent.copy(alpha = 0.18f),
                start = Offset(0f, size.height * 0.35f),
                end = Offset(size.width, size.height * 0.65f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(spec.accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = spec.icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Surface(
                color = Color.Black.copy(alpha = 0.28f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = spec.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private data class ArtSpec(
    val colors: List<Color>,
    val accent: Color,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun ProductGridCard(
    product: ProductEntity,
    currency: CurrencyCode,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onQuickAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discount = discountPercent(product.priceInr, product.mrpInr)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
            .clickable(onClick = onProductClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                ProductImage(
                    imageKey = product.imageKey,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left discount or flash pill
                if (discount > 0) {
                    Surface(
                        color = if (product.isFlashDeal) TerracottaAccent else EmeraldDeep,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-$discount%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Top-right wishlist button (48dp touch target)
                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .minimumInteractiveComponentSize()
                        .testTag("wishlist_toggle_${product.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
                            tint = if (isWishlisted) TerracottaAccent else EmeraldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = product.vendorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified vendor",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = SaffronContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = SaffronGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${product.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronOnContainer
                            )
                        }
                    }
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = formatPrice(product.priceInr, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (discount > 0) {
                            Text(
                                text = formatPrice(product.mrpInr, currency),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = onQuickAddToCart,
                        enabled = product.stockCount > 0,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("quick_add_cart_${product.id}"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddShoppingCart,
                            contentDescription = "Add ${product.title} to cart",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (role) {
        UserRole.BUYER -> Triple(MintContainer, MintOnContainer, "BUYER")
        UserRole.VENDOR -> Triple(SaffronContainer, SaffronOnContainer, "VENDOR")
        UserRole.ADMIN -> Triple(TerracottaContainer, TerracottaOnContainer, "ADMIN")
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OrderStatusPill(status: OrderTrackingStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        OrderTrackingStatus.DELIVERED -> SuccessContainer to SuccessGreen
        OrderTrackingStatus.CANCELLED -> DangerContainer to DangerRed
        OrderTrackingStatus.OUT_FOR_DELIVERY, OrderTrackingStatus.SHIPPED -> SaffronContainer to WarningAmber
        else -> MintContainer to MintOnContainer
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = status.name.replace('_', ' '),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(20.dp))
            actionButton()
        }
    }
}
