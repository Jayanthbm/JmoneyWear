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
import com.jayanth.jmoney.wear.presentation.tiles.utils.TileTheme
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
                            .setColor(ColorBuilders.argb(TileTheme.TEXT))
                            .build()
                    )
                    .build()
            }

            if (data == null) {
                val chipClickable = ModifiersBuilders.Clickable.Builder()
                    .setId("select_goal_$configKey")
                    .setOnClick(launchAction)
                    .build()

                return ActionTileLayout.build(
                    context = context,
                    deviceParameters = deviceParameters,
                    title = "Goal Tracker",
                    buttonText = "Select Goal",
                    buttonColor = TileTheme.PRIMARY,
                    clickable = chipClickable
                )
            }

            val pct = data.percentage.coerceIn(0.0, 1.0)

            // Color Coding for goal progress (savings):
            // Under 25% -> Red (Just starting)
            // 25% to 50% -> Tonal Orange/Yellow
            // 50% to 80% -> Blue
            // Over 80% -> Green (Almost achieved!)
            val indicatorColor = when {
                pct > 0.8  -> TileTheme.SUCCESS  // green
                pct > 0.5  -> TileTheme.PRIMARY  // light blue
                pct > 0.25 -> 0xFFFFD54F.toInt() // orange-yellow tint
                else       -> TileTheme.DANGER   // red
            }

            val progressIndicator = CircularProgressIndicator.Builder()
                .setProgress(pct.toFloat())
                .setCircularProgressIndicatorColors(
                    ProgressIndicatorColors(
                        ColorBuilders.argb(indicatorColor),
                        ColorBuilders.argb(TileTheme.CARD_BACKGROUND)
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
                        .setColor(ColorBuilders.argb(TileTheme.TEXT))
                        .build()
                )
                .addContent(
                    Text.Builder(context, "of $targetStr")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                        .setColor(ColorBuilders.argb(TileTheme.TEXT_SECONDARY))
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
                        .setColor(ColorBuilders.argb(TileTheme.TEXT))
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
