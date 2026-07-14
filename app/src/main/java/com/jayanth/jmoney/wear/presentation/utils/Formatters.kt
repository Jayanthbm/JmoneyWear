package com.jayanth.jmoney.wear.presentation.utils

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val isInteger = amount % 1.0 == 0.0
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = if (isInteger) 0 else 2
        maximumFractionDigits = 2
    }
    return "₹${formatter.format(amount)}"
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
