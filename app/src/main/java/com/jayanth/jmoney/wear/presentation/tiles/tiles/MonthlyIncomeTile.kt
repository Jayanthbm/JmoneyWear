package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import com.jayanth.jmoney.wear.presentation.tiles.utils.IncomeData
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

class MonthlyIncomeTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters,
            data: IncomeData,
            isLoggedIn: Boolean
        ): LayoutElementBuilders.LayoutElement {
            val colors = Colors(0xFF4CAF50.toInt(), 0xFFFFFFFF.toInt(), 0, 0) // Green for income

            val mainLayout = if (!isLoggedIn) {
                PrimaryLayout.Builder(deviceParameters)
                    .setContent(
                        Text.Builder(context, "Please log in on watch")
                            .setTypography(Typography.TYPOGRAPHY_BODY1)
                            .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .build()
            } else {
                val amountStr = if (data.amount > 1000000.0) {
                    formatCurrency(1000000.0) + "..."
                } else {
                    formatCurrency(data.amount)
                }
                val periodLabel = "INCOME - ${data.month}"

                PrimaryLayout.Builder(deviceParameters)
                    .setPrimaryLabelTextContent(
                        Text.Builder(context, periodLabel)
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(colors.primary))
                            .build()
                    )
                    .setContent(
                        Text.Builder(context, amountStr)
                            .setTypography(Typography.TYPOGRAPHY_TITLE2)
                            .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .setSecondaryLabelTextContent(
                        Text.Builder(context, "Monthly Period")
                            .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                            .setColor(ColorBuilders.argb(0xFF888888.toInt()))
                            .build()
                    )
                    .build()
            }

            return mainLayout
        }
    }
}
