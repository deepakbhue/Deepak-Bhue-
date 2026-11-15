package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.i18n.formatPrice
import com.example.data.model.PaymentMethod
import com.example.ui.MarketplaceUiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductImage
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

@Composable
fun CartCheckoutScreen(
    uiState: MarketplaceUiState,
    onUpdateCartQuantity: (cartId: String, newQty: Int) -> Unit,
    onApplyPromo: (String) -> Unit,
    onSelectAddress: (String) -> Unit,
    onAddAddress: (fullName: String, phone: String, street: String, city: String, postalCode: String, country: String) -> Unit,
    onPlaceOrder: (PaymentMethod, onOrderCreated: (String) -> Unit) -> Unit,
    onNavigateToOrders: () -> Unit,
    onExploreShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = uiState.strings
    var promoInput by remember { mutableStateOf(uiState.promoCode) }
    var showAddAddressSheet by remember { mutableStateOf(false) }
    var showPaymentGatewaySheet by remember { mutableStateOf(false) }

    if (uiState.cartItems.isEmpty()) {
        EmptyStateView(
            icon = Icons.Filled.ShoppingCartCheckout,
            title = strings.cartEmptyTitle,
            subtitle = strings.cartEmptySubtitle,
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
                text = "${strings.cartTitle} (${uiState.cartCount})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Cart Items
        items(uiState.cartItems, key = { it.cartId }) { item ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImage(
                        imageKey = item.imageKey,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.selectedVariant} • ${item.vendorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatPrice(item.priceInr * item.quantity, uiState.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                IconButton(
                                    onClick = { onUpdateCartQuantity(item.cartId, item.quantity - 1) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { onUpdateCartQuantity(item.cartId, item.quantity + 1) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase quantity", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delivery Address
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(strings.deliveryAddress, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showAddAddressSheet = true }) {
                            Text(strings.addNewAddress)
                        }
                    }

                    uiState.addresses.forEach { addr ->
                        val isSelected = addr.id == uiState.selectedAddress?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectAddress(addr.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { onSelectAddress(addr.id) })
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${addr.fullName} • ${addr.phone}", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "${addr.street}, ${addr.city} ${addr.postalCode}, ${addr.country}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Promo Code
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promoInput,
                    onValueChange = { promoInput = it },
                    placeholder = { Text(strings.promoCodeHint) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("promo_code_input")
                )
                Button(
                    onClick = { onApplyPromo(promoInput) },
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("apply_promo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.applyPromo)
                }
            }
        }

        // Price Breakdown
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriceRow(strings.subtotal, formatPrice(uiState.cartSubtotalInr, uiState.currency))
                    if (uiState.promoDiscountInr > 0) {
                        PriceRow(
                            "${strings.discount} (${uiState.promoCode})",
                            "-${formatPrice(uiState.promoDiscountInr, uiState.currency)}",
                            highlightSuccess = true
                        )
                    }
                    PriceRow(strings.shipping, strings.free, highlightSuccess = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(strings.totalPayable, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = formatPrice(uiState.cartFinalInr, uiState.currency),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showPaymentGatewaySheet = true },
                        enabled = uiState.selectedAddress != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_to_pay_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.proceedToPay, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddAddressSheet) {
        AddAddressModalSheet(
            onSave = { name, phone, street, city, postal, country ->
                onAddAddress(name, phone, street, city, postal, country)
                showAddAddressSheet = false
            },
            onDismiss = { showAddAddressSheet = false }
        )
    }

    if (showPaymentGatewaySheet) {
        GlobalPaymentGatewaySheet(
            uiState = uiState,
            onAuthorizeAndPay = { method ->
                onPlaceOrder(method) {
                    showPaymentGatewaySheet = false
                    onNavigateToOrders()
                }
            },
            onDismiss = { if (!uiState.isProcessingPayment) showPaymentGatewaySheet = false }
        )
    }
}

@Composable
private fun PriceRow(label: String, value: String, highlightSuccess: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (highlightSuccess) SuccessGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlobalPaymentGatewaySheet(
    uiState: MarketplaceUiState,
    onAuthorizeAndPay: (PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = uiState.strings
    var selectedMethod by remember { mutableStateOf(PaymentMethod.GLOBAL_CARD) }
    var cardNumber by remember { mutableStateOf("4532 8891 2041 7742") }
    var expiry by remember { mutableStateOf("09/29") }
    var cvv by remember { mutableStateOf("842") }
    var cardHolder by remember { mutableStateOf(uiState.activeUser?.name ?: "Aarav Mehta") }
    var upiId by remember { mutableStateOf("aarav@okaxis") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val cardNetwork = remember(cardNumber) {
        val digits = cardNumber.filter { it.isDigit() }
        when {
            digits.startsWith("4") -> "VISA Global"
            digits.startsWith("5") -> "Mastercard"
            digits.startsWith("3") -> "Amex"
            else -> "Global Card"
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(strings.globalPaymentGateway, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Settlement Currency: ${uiState.currency.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(color = SuccessContainer, shape = RoundedCornerShape(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PCI-DSS", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (uiState.isProcessingPayment) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator()
                        Text(strings.processingPayment, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "3D-Secure / FX Verification (${formatPrice(uiState.cartFinalInr, uiState.currency)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(strings.selectPaymentMethod, style = MaterialTheme.typography.labelLarge)

                PaymentMethodOptionRow(
                    icon = Icons.Filled.CreditCard,
                    title = strings.payCard,
                    selected = selectedMethod == PaymentMethod.GLOBAL_CARD,
                    onClick = { selectedMethod = PaymentMethod.GLOBAL_CARD }
                )
                PaymentMethodOptionRow(
                    icon = Icons.Filled.QrCode2,
                    title = strings.payUpi,
                    selected = selectedMethod == PaymentMethod.UPI_INSTANT,
                    onClick = { selectedMethod = PaymentMethod.UPI_INSTANT }
                )
                PaymentMethodOptionRow(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = strings.payWallet,
                    selected = selectedMethod == PaymentMethod.DIGITAL_WALLET,
                    onClick = { selectedMethod = PaymentMethod.DIGITAL_WALLET }
                )
                PaymentMethodOptionRow(
                    icon = Icons.Filled.LocalAtm,
                    title = strings.payCod,
                    selected = selectedMethod == PaymentMethod.COD,
                    onClick = { selectedMethod = PaymentMethod.COD }
                )

                when (selectedMethod) {
                    PaymentMethod.GLOBAL_CARD -> {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it.take(19) },
                            label = { Text("${strings.cardNumberLabel} ($cardNetwork)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = expiry,
                                onValueChange = { expiry = it.take(5) },
                                label = { Text(strings.expiryLabel) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it.filter { ch -> ch.isDigit() }.take(4) },
                                label = { Text(strings.cvvLabel) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text(strings.cardHolderLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PaymentMethod.UPI_INSTANT -> {
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text(strings.upiIdLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PaymentMethod.DIGITAL_WALLET -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Global Wallet Express (Apple Pay / Google Pay / PayPal) ready for instant settlement.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    PaymentMethod.COD -> {
                        Text(
                            text = "Pay ${formatPrice(uiState.cartFinalInr, uiState.currency)} in cash or UPI upon doorstep delivery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        validationError = when (selectedMethod) {
                            PaymentMethod.GLOBAL_CARD -> {
                                val digits = cardNumber.filter { it.isDigit() }
                                when {
                                    digits.length < 12 -> "Enter a valid 12–16 digit card number."
                                    expiry.length < 4 -> "Enter valid expiry MM/YY."
                                    cvv.length < 3 -> "Enter valid 3–4 digit CVV."
                                    cardHolder.isBlank() -> "Enter cardholder name."
                                    else -> null
                                }
                            }
                            PaymentMethod.UPI_INSTANT -> {
                                if (!upiId.contains("@")) "Enter a valid UPI ID (e.g. user@bank)." else null
                            }
                            else -> null
                        }
                        if (validationError == null) {
                            onAuthorizeAndPay(selectedMethod)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("authorize_pay_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "${strings.authorizeAndPay} • ${formatPrice(uiState.cartFinalInr, uiState.currency)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAddressModalSheet(
    onSave: (String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postal by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("India") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Add Delivery Address", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street / House No.") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = postal, onValueChange = { postal = it }, label = { Text("Postal Code") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        if (fullName.isNotBlank() && street.isNotBlank() && city.isNotBlank()) {
                            onSave(fullName, phone.ifBlank { "+91 90000 00000" }, street, city, postal.ifBlank { "110001" }, country)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Address")
                }
            }
        }
    }
}
