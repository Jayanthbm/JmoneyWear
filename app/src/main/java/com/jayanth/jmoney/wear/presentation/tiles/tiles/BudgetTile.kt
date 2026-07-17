package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import com.jayanth.jmoney.wear.presentation.tiles.utils.BudgetSpendingData
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

class BudgetTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters,
            data: BudgetSpendingData?,
            isLoggedIn: Boolean,
            configKey: String
        ): LayoutElementBuilders.LayoutElement {

            val launchAction = ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setPackageName(context.packageName)
                        .setClassName("com.jayanth.jmoney.wear.presentation.tiles.configuration.BudgetTileConfigActivity")
                        .addKeyToExtraMapping("config_key", ActionBuilders.stringExtra(configKey))
                        .build()
                )
                .build()

            if (!isLoggedIn) {
                return PrimaryLayout.Builder(deviceParameters)
                    .setContent(
                        Text.Builder(context, "Please log in on watch")
                            .setTypography(Typography.TYPOGRAPHY_BODY1)
                            .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .build()
            }

            if (data == null) {
                // Chip button — like alarm widget "Select Budget"
                val chipClickable = ModifiersBuilders.Clickable.Builder()
                    .setId("select_budget_$configKey")
                    .setOnClick(launchAction)
                    .build()

                val chipColors = ChipColors.primaryChipColors(
                    Colors(0xFF2196F3.toInt(), 0xFFFFFFFF.toInt(), 0xFF1A1A1A.toInt(), 0xFF888888.toInt())
                )

                val chip = Chip.Builder(context, chipClickable, deviceParameters)
                    .setPrimaryLabelContent("Select Budget")
                    .setChipColors(chipColors)
                    .setWidth(DimensionBuilders.wrap())
                    .build()

                return PrimaryLayout.Builder(deviceParameters)
                    .setPrimaryLabelTextContent(
                        Text.Builder(context, "Budget Tracker")
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(0xFFAAAAAA.toInt()))
                            .build()
                    )
                    .setContent(chip)
                    .build()
            }

            // ── Configured: EdgeContentLayout ───────────────────────────────
            val pct = data.percentage.coerceIn(0.0, 1.0)

            // Color: green < 50%, blue 50-80%, red > 80% or overspent
            val indicatorColor = when {
                data.isOverspent || pct > 0.8 -> 0xFFF44336.toInt()  // red
                pct > 0.5                     -> 0xFF2196F3.toInt()  // blue
                else                          -> 0xFF4CAF50.toInt()  // green
            }

            // Grey track background + colored progress
            val progressIndicator = CircularProgressIndicator.Builder()
                .setProgress(pct.toFloat())
                .setCircularProgressIndicatorColors(
                    ProgressIndicatorColors(
                        ColorBuilders.argb(indicatorColor),
                        ColorBuilders.argb(0xFF444444.toInt()) // grey track
                    )
                )
                .build()

            // Center: ₹spent on first line, "of ₹limit" on second line
            val spentStr = if (data.spent > 1_000_000.0) formatCurrency(1_000_000.0) + "..." else formatCurrency(data.spent)
            val limitStr = if (data.limit > 1_000_000.0) formatCurrency(1_000_000.0) + "..." else formatCurrency(data.limit)

            val tapClickable = ModifiersBuilders.Clickable.Builder()
                .setId("change_budget_$configKey")
                .setOnClick(launchAction)
                .build()

            val centerColumn = LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(
                    Text.Builder(context, spentStr)
                        .setTypography(Typography.TYPOGRAPHY_TITLE2)
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                .addContent(
                    Text.Builder(context, "of $limitStr")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                        .setColor(ColorBuilders.argb(0xFF888888.toInt()))
                        .build()
                )
                .setModifiers(
                    ModifiersBuilders.Modifiers.Builder()
                        .setClickable(tapClickable)
                        .build()
                )
                .build()

            val truncatedName = if (data.name.length > 15) data.name.take(12) + "..." else data.name
            val actualPct = (data.percentage * 100).toInt()  // not clamped — shows 109%, 200% etc.
            val pctLabel = "$actualPct% used"

            return EdgeContentLayout.Builder(deviceParameters)
                .setEdgeContent(progressIndicator)
                .setPrimaryLabelTextContent(
                    Text.Builder(context, truncatedName)
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                .setContent(centerColumn)
                .setSecondaryLabelTextContent(
                    Text.Builder(context, pctLabel)
                        .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                        .setColor(ColorBuilders.argb(indicatorColor))
                        .build()
                )
                .build()
        }
    }
}
