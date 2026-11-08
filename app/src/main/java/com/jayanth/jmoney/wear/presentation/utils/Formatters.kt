package com.jayanth.jmoney.wear.presentation.utils

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val isInteger = amount % 1.0 == 0.0
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = if (isInteger) 0 else 2
        maximumFractionDigits = 2
        isGroupingUsed = false
    }
    val formatted = formatter.format(amount)
    val decimalIndex = formatted.indexOf('.')
    val integerPart = if (decimalIndex >= 0) formatted.substring(0, decimalIndex) else formatted
    val fractionPart = if (decimalIndex >= 0) formatted.substring(decimalIndex) else ""
    val sign = if (integerPart.startsWith("-")) "-" else ""
    val digits = integerPart.removePrefix("-")
    val groupedInteger = if (digits.length <= 3) {
        digits
    } else {
        val prefix = digits.dropLast(3)
        val firstGroupLength = if (prefix.length % 2 == 0) 2 else 1
        val groups = mutableListOf(prefix.take(firstGroupLength))
        var index = firstGroupLength
        while (index < prefix.length) {
            groups += prefix.substring(index, (index + 2).coerceAtMost(prefix.length))
            index += 2
        }
        (groups + digits.takeLast(3)).joinToString(",")
    }
    return "$sign₹$groupedInteger$fractionPart"
}

fun formatTransactionDate(dateStr: String): String {
    return try {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        when (dateStr) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val localDate = LocalDate.parse(dateStr)
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                localDate.format(formatter)
            }
        }
    } catch (e: Exception) {
        dateStr
    }
}

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp == 0L) return "Never synced"
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 0 -> "Just now"
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
