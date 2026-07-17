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
import com.jayanth.jmoney.wear.presentation.tiles.utils.GoalProgressData
import com.jayanth.jmoney.wear.presentation.utils.formatCurrency

class GoalTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters,
            data: GoalProgressData?,
            isLoggedIn: Boolean,
            configKey: String
        ): LayoutElementBuilders.LayoutElement {

            val launchAction = ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(
                    ActionBuilders.AndroidActivity.Builder()
                        .setPackageName(context.packageName)
                        .setClassName("com.jayanth.jmoney.wear.presentation.tiles.configuration.GoalTileConfigActivity")
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
                val chipClickable = ModifiersBuilders.Clickable.Builder()
                    .setId("select_goal_$configKey")
                    .setOnClick(launchAction)
                    .build()

                val chipColors = ChipColors.primaryChipColors(
                    Colors(0xFF2196F3.toInt(), 0xFFFFFFFF.toInt(), 0xFF1A1A1A.toInt(), 0xFF888888.toInt())
                )

                val chip = Chip.Builder(context, chipClickable, deviceParameters)
                    .setPrimaryLabelContent("Select Goal")
                    .setChipColors(chipColors)
                    .setWidth(DimensionBuilders.wrap())
                    .build()

                return PrimaryLayout.Builder(deviceParameters)
                    .setPrimaryLabelTextContent(
                        Text.Builder(context, "Goal Tracker")
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .setColor(ColorBuilders.argb(0xFFAAAAAA.toInt()))
                            .build()
                    )
                    .setContent(chip)
                    .build()
            }

            val pct = data.percentage.coerceIn(0.0, 1.0)

            // Color Coding for goal progress (savings):
            // Under 25% -> Red (Just starting)
            // 25% to 50% -> Yellow (Building up)
            // 50% to 80% -> Blue (Halfway or more)
            // Over 80% -> Green (Almost achieved!)
            val indicatorColor = when {
                pct > 0.8 -> 0xFF4CAF50.toInt()  // green
                pct > 0.5 -> 0xFF2196F3.toInt()  // blue
                pct > 0.25 -> 0xFFFFC107.toInt() // yellow
                else -> 0xFFF44336.toInt()       // red
            }

            val progressIndicator = CircularProgressIndicator.Builder()
                .setProgress(pct.toFloat())
                .setCircularProgressIndicatorColors(
                    ProgressIndicatorColors(
                        ColorBuilders.argb(indicatorColor),
                        ColorBuilders.argb(0xFF444444.toInt())
                    )
                )
                .build()

            val savedStr = if (data.saved > 1_000_000.0) formatCurrency(1_000_000.0) + "..." else formatCurrency(data.saved)
            val targetStr = if (data.target > 1_000_000.0) formatCurrency(1_000_000.0) + "..." else formatCurrency(data.target)

            val tapClickable = ModifiersBuilders.Clickable.Builder()
                .setId("change_goal_$configKey")
                .setOnClick(launchAction)
                .build()

            val centerColumn = LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(
                    Text.Builder(context, savedStr)
                        .setTypography(Typography.TYPOGRAPHY_TITLE2)
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                .addContent(
                    Text.Builder(context, "of $targetStr")
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
            val actualPct = (data.percentage * 100).toInt()
            val pctLabel = "$actualPct% saved"

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
