package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import com.jayanth.jmoney.wear.presentation.tiles.utils.RemainingData
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

class RemainingForPeriodTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters,
            data: RemainingData,
            isLoggedIn: Boolean
        ): LayoutElementBuilders.LayoutElement {
            val colors = if (data.isOverspent) {
                // Red for overspent
                Colors(0xFFF44336.toInt(), 0xFFFFFFFF.toInt(), 0, 0)
            } else {
                // Green for remaining
                Colors(0xFF4CAF50.toInt(), 0xFFFFFFFF.toInt(), 0, 0)
            }

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
                val label = if (data.isOverspent) "OVERSPENT" else "REMAINING"
                val amountStr = formatCurrency(data.amount)

                PrimaryLayout.Builder(deviceParameters)
                    .setPrimaryLabelTextContent(
                        Text.Builder(context, label)
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(colors.primary))
                            .build()
                    )
                    .setContent(
                        Text.Builder(context, amountStr)
                            .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
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
