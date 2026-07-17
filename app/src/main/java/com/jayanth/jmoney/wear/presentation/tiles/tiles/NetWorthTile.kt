package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

class NetWorthTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters,
            netWorth: Double,
            isLoggedIn: Boolean
        ): LayoutElementBuilders.LayoutElement {
            val colors = Colors(0xFF2196F3.toInt(), 0xFFFFFFFF.toInt(), 0, 0) // Blue for net worth

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
                // If net worth is negative, keep the minus sign but format the absolute amount.
                val isNegative = netWorth < 0
                val absVal = Math.abs(netWorth)
                val amountStr = if (absVal > 1000000.0) {
                    (if (isNegative) "-" else "") + formatCurrency(1000000.0) + "..."
                } else {
                    (if (isNegative) "-" else "") + formatCurrency(absVal)
                }

                PrimaryLayout.Builder(deviceParameters)
                    .setPrimaryLabelTextContent(
                        Text.Builder(context, "NET WORTH")
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
                        Text.Builder(context, "Total Balance")
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
