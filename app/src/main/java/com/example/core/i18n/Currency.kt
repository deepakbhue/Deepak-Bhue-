package com.example.core.i18n

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

enum class CurrencyCode(
    val code: String,
    val symbol: String,
    val rateFromInr: Double,
    val label: String
) {
    INR("INR", "₹", 1.0, "INR (₹)"),
    USD("USD", "$", 0.012, "USD ($)"),
    EUR("EUR", "€", 0.011, "EUR (€)"),
    GBP("GBP", "£", 0.0095, "GBP (£)");

    companion object {
        fun fromCode(code: String): CurrencyCode =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: INR
    }
}

fun convertFromInr(amountInr: Double, currency: CurrencyCode): Double {
    return amountInr * currency.rateFromInr
}

fun formatPrice(amountInr: Double, currency: CurrencyCode): String {
    val converted = convertFromInr(amountInr, currency)
    return when (currency) {
        CurrencyCode.INR -> {
            val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
            nf.maximumFractionDigits = 0
            "${currency.symbol}${nf.format(converted.roundToInt())}"
        }
        CurrencyCode.USD, CurrencyCode.EUR, CurrencyCode.GBP -> {
            "${currency.symbol}${String.format(Locale.US, "%.2f", converted)}"
        }
    }
}

fun discountPercent(priceInr: Double, mrpInr: Double): Int {
    if (mrpInr <= priceInr || mrpInr <= 0.0) return 0
    return (((mrpInr - priceInr) / mrpInr) * 100.0).roundToInt()
}
