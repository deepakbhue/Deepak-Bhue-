package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppStrings
import com.example.core.i18n.formatPrice
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemCodec
import com.example.data.model.OrderTrackingStatus
import com.example.ui.MarketplaceUiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrderStatusPill
import com.example.ui.components.ProductImage
import com.example.ui.theme.SuccessGreen

@Composable
fun OrdersScreen(
    uiState: MarketplaceUiState,
    onAdvanceTracking: (OrderEntity) -> Unit,
    onCancelOrder: (OrderEntity) -> Unit,
    onExploreShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = uiState.strings

    if (uiState.buyerOrders.isEmpty()) {
        EmptyStateView(
            icon = Icons.Filled.LocalShipping,
            title = strings.ordersEmptyTitle,
            subtitle = strings.ordersEmptySubtitle,
            modifier = modifier.fillMaxSize(),
            actionButton = {
                Button(onClick = onExploreShop) {
                    Text(strings.heroCta)
                }
            }
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = strings.ordersTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.buyerOrders, key = { it.id }) { order ->
            OrderTrackingCard(
                order = order,
                strings = strings,
                uiState = uiState,
                initiallyExpanded = order.id == uiState.lastCompletedOrderId ||
                    uiState.buyerOrders.firstOrNull()?.id == order.id,
                onAdvanceTracking = { onAdvanceTracking(order) },
                onCancelOrder = { onCancelOrder(order) }
            )
        }
    }
}

@Composable
private fun OrderTrackingCard(
    order: OrderEntity,
    strings: AppStrings,
    uiState: MarketplaceUiState,
    initiallyExpanded: Boolean,
    onAdvanceTracking: () -> Unit,
    onCancelOrder: () -> Unit
) {
    var expanded by remember(order.id) { mutableStateOf(initiallyExpanded) }
    val status = OrderTrackingStatus.fromString(order.trackingStatus)
    val items = remember(order.itemsEncoded) { OrderItemCodec.decode(order.itemsEncoded) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.estimatedDelivery,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrderStatusPill(status = status)
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Toggle order details"
                    )
                }
            }

            // Items preview
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProductImage(
                        imageKey = item.imageKey,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                        Text(
                            text = "${item.variant} × ${item.quantity} • ${item.vendorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatPrice(item.unitPriceInr * item.quantity, uiState.currency),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider()

                    // Gateway & Courier Metadata
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            MetaRow(strings.courierLabel, "${order.courierName} (${order.trackingCode})")
                            MetaRow(strings.paymentRefLabel, "${order.paymentGatewayRef} • ${order.paymentStatus}")
                            MetaRow("Paid", "${order.paidAmountFormatted} (${order.currencyCode})")
                            MetaRow("Address", order.shippingAddressSummary)
                        }
                    }

                    Text(
                        text = strings.liveTracking,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (status == OrderTrackingStatus.CANCELLED) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = strings.statusCancelled,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            OrderTrackingStatus.activeTimeline.forEachIndexed { idx, step ->
                                val reached = status.stepIndex >= step.stepIndex
                                val isCurrent = status == step
                                val label = when (step) {
                                    OrderTrackingStatus.PLACED -> strings.statusPlaced
                                    OrderTrackingStatus.CONFIRMED -> strings.statusConfirmed
                                    OrderTrackingStatus.PACKED -> strings.statusPacked
                                    OrderTrackingStatus.SHIPPED -> strings.statusShipped
                                    OrderTrackingStatus.OUT_FOR_DELIVERY -> strings.statusOutForDelivery
                                    OrderTrackingStatus.DELIVERED -> strings.statusDelivered
                                    OrderTrackingStatus.CANCELLED -> strings.statusCancelled
                                }
                                Row(verticalAlignment = Alignment.Top) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (reached) SuccessGreen else MaterialTheme.colorScheme.surfaceContainerHigh
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (reached) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        if (idx < OrderTrackingStatus.activeTimeline.lastIndex) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(20.dp)
                                                    .background(
                                                        if (status.stepIndex > step.stepIndex) SuccessGreen
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                                    )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (status.nextStatus() != null) {
                            Button(
                                onClick = onAdvanceTracking,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_tracking_${order.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.advanceTrackingSim, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        if (status.canCancel()) {
                            OutlinedButton(
                                onClick = onCancelOrder,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("cancel_order_${order.id}")
                            ) {
                                Text(strings.cancelOrder, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}
