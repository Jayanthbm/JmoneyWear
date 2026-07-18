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
import com.jayanth.jmoney.wear.presentation.tiles.utils.TileTheme
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

                val displayAmount = if (amountStr.endsWith(".00")) {
                    amountStr.substring(0, amountStr.length - 3)
                } else {
                    amountStr
                }

                // Modern semi-transparent primary blue badge
                val badgeText = Text.Builder(context, "OVERALL")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(TileTheme.PRIMARY))
                    .build()

                val overallBadge = LayoutElementBuilders.Box.Builder()
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(ColorBuilders.argb(0x1F9ECAFF.toInt())) // 12% opacity primary blue
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
                val bottomLabel = Text.Builder(context, "Net Worth")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(TileTheme.TEXT_SECONDARY))
                    .build()

                // Vertical column with precise M3 Expressive spacing
                val column = LayoutElementBuilders.Column.Builder()
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                    .addContent(overallBadge)
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

            return mainLayout
        }
    }
}
