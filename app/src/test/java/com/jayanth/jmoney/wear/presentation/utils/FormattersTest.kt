package com.jayanth.jmoney.wear.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun formatCurrencyUsesIndianDigitGrouping() {
        assertEquals("₹1,23,456", formatCurrency(123456.0))
    }

    @Test
    fun formatCurrencyKeepsTwoFractionDigitsWhenNeeded() {
        assertEquals("₹1,23,456.78", formatCurrency(123456.78))
    }

    @Test
    fun zeroSyncTimestampIsPresentedAsNeverSynced() {
        assertEquals("Never synced", formatRelativeTime(0L))
    }

    @Test
    fun invalidTransactionDateIsReturnedUnchanged() {
        assertEquals("not-a-date", formatTransactionDate("not-a-date"))
    }
}
