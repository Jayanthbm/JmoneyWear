package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.ModifiersBuilders
import com.jayanth.jmoney.wear.presentation.tiles.utils.IncomeData
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

import com.jayanth.jmoney.wear.presentation.tiles.utils.TileTheme

class MonthlyIncomeTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters,
            data: IncomeData,
            isLoggedIn: Boolean
        ): LayoutElementBuilders.LayoutElement {

            if (!isLoggedIn) {
                return PrimaryLayout.Builder(deviceParameters)
                    .setContent(
                        Text.Builder(context, "Please log in on watch")
                            .setTypography(Typography.TYPOGRAPHY_BODY1)
                            .setColor(ColorBuilders.argb(TileTheme.TEXT))
                            .build()
                    )
                    .build()
            }

            // Drop .00 decimal places to save space and fit screen
            val amountStr = formatCurrency(data.amount)
            val displayAmount = if (amountStr.endsWith(".00")) {
                amountStr.substring(0, amountStr.length - 3)
            } else {
                amountStr
            }

            // Modern semi-transparent Success Green badge for the Month
            val badgeText = Text.Builder(context, data.month.uppercase())
                .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                .setColor(ColorBuilders.argb(TileTheme.SUCCESS))
                .build()

            val monthBadge = LayoutElementBuilders.Box.Builder()
                .setModifiers(
                    ModifiersBuilders.Modifiers.Builder()
                        .setBackground(
                            ModifiersBuilders.Background.Builder()
                                .setColor(ColorBuilders.argb(0x1F81C995.toInt())) // 12% opacity success green
                                .setCorner(
                                    ModifiersBuilders.Corner.Builder()
                                        .setRadius(DimensionBuilders.dp(8f))
                                        .build()
                                )
                                .build()
                        )
                        .setPadding(
                            ModifiersBuilders.Padding.Builder()
                                .setStart(DimensionBuilders.dp(8f))
                                .setEnd(DimensionBuilders.dp(8f))
                                .setTop(DimensionBuilders.dp(4f))
                                .setBottom(DimensionBuilders.dp(4f))
                                .build()
                        )
                        .build()
                )
                .addContent(badgeText)
                .build()

            // Large, clear amount text
            val amountText = Text.Builder(context, displayAmount)
                .setTypography(Typography.TYPOGRAPHY_TITLE2)
                .setColor(ColorBuilders.argb(TileTheme.TEXT))
                .build()

            // Secondary label at bottom
            val bottomLabel = Text.Builder(context, "Monthly Income")
                .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                .setColor(ColorBuilders.argb(TileTheme.TEXT_SECONDARY))
                .build()

            // Vertical column with precise M3 Expressive spacing
            val column = LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(monthBadge)
                .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(12f)).build())
                .addContent(amountText)
                .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(6f)).build())
                .addContent(bottomLabel)
                .build()

            // Root box container centered on screen
            return LayoutElementBuilders.Box.Builder()
                .setWidth(DimensionBuilders.dp(deviceParameters.screenWidthDp.toFloat()))
                .setHeight(DimensionBuilders.dp(deviceParameters.screenHeightDp.toFloat()))
                .addContent(column)
                .build()
        }
    }
}
