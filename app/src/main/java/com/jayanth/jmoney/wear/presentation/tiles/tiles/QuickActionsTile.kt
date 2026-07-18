package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import com.jayanth.jmoney.wear.presentation.MainActivity

import com.jayanth.jmoney.wear.presentation.tiles.utils.TileTheme

class QuickActionsTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters
        ): LayoutElementBuilders.LayoutElement {
            val expenseClickable = ModifiersBuilders.Clickable.Builder()
                .setId("add_expense_clickable")
                .setOnClick(
                    ActionBuilders.LaunchAction.Builder()
                        .setAndroidActivity(
                            ActionBuilders.AndroidActivity.Builder()
                                .setPackageName(context.packageName)
                                .setClassName(MainActivity::class.java.name)
                                .addKeyToExtraMapping("action", ActionBuilders.stringExtra("add_expense"))
                                .build()
                        )
                        .build()
                )
                .build()

            val incomeClickable = ModifiersBuilders.Clickable.Builder()
                .setId("add_income_clickable")
                .setOnClick(
                    ActionBuilders.LaunchAction.Builder()
                        .setAndroidActivity(
                            ActionBuilders.AndroidActivity.Builder()
                                .setPackageName(context.packageName)
                                .setClassName(MainActivity::class.java.name)
                                .addKeyToExtraMapping("action", ActionBuilders.stringExtra("add_income"))
                                .build()
                        )
                        .build()
                )
                .build()

            val quickClickable = ModifiersBuilders.Clickable.Builder()
                .setId("quick_add_clickable")
                .setOnClick(
                    ActionBuilders.LaunchAction.Builder()
                        .setAndroidActivity(
                            ActionBuilders.AndroidActivity.Builder()
                                .setPackageName(context.packageName)
                                .setClassName(MainActivity::class.java.name)
                                .addKeyToExtraMapping("action", ActionBuilders.stringExtra("open_quick_transactions"))
                                .build()
                        )
                        .build()
                )
                .build()

            // Custom M3 Expressive squircle buttons matching Dashboard Screen layout colors and vector icons exactly
            val expenseBtn = buildIconButton(
                clickable = expenseClickable,
                resourceId = "ic_remove",
                backgroundColor = 0xFF8C1D18.toInt(), // ErrorContainer
                textColor = 0xFFF9DEDC.toInt()       // OnErrorContainer
            )

            val incomeBtn = buildIconButton(
                clickable = incomeClickable,
                resourceId = "ic_add",
                backgroundColor = 0xFF004977.toInt(), // PrimaryContainer
                textColor = 0xFFC2E7FF.toInt()       // OnPrimaryContainer
            )

            val quickBtn = buildIconButton(
                clickable = quickClickable,
                resourceId = "ic_bolt",
                backgroundColor = 0xFF2D2F31.toInt(), // SecondaryContainer (tonal dark surface)
                textColor = 0xFFE2E2E2.toInt()       // OnSecondaryContainer
            )

            val row = LayoutElementBuilders.Row.Builder()
                .addContent(expenseBtn)
                .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(12f)).build())
                .addContent(incomeBtn)
                .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(12f)).build())
                .addContent(quickBtn)
                .build()

            val titleText = Text.Builder(context, "QUICK ACTIONS")
                .setTypography(Typography.TYPOGRAPHY_BODY1)
                .setColor(ColorBuilders.argb(TileTheme.TEXT_SECONDARY))
                .build()

            // Vertical column with precise M3 Expressive spacing
            val column = LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(titleText)
                .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(20f)).build())
                .addContent(row)
                // Bottom spacer to nudge elements slightly upwards for optical vertical centering
                .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(20f)).build())
                .build()

            // Root box container centered on screen
            return LayoutElementBuilders.Box.Builder()
                .setWidth(DimensionBuilders.dp(deviceParameters.screenWidthDp.toFloat()))
                .setHeight(DimensionBuilders.dp(deviceParameters.screenHeightDp.toFloat()))
                .addContent(column)
                .build()
        }

        /** Helper to build a modern 52dp squircle (16dp corner radius) button. */
        private fun buildIconButton(
            clickable: ModifiersBuilders.Clickable,
            resourceId: String,
            backgroundColor: Int,
            textColor: Int
        ): LayoutElementBuilders.LayoutElement {
            val background = ModifiersBuilders.Background.Builder()
                .setColor(ColorBuilders.argb(backgroundColor))
                .setCorner(
                    ModifiersBuilders.Corner.Builder()
                        .setRadius(DimensionBuilders.dp(16f)) // 16dp squircle
                        .build()
                )
                .build()

            val modifiers = ModifiersBuilders.Modifiers.Builder()
                .setClickable(clickable)
                .setBackground(background)
                .build()

            val imageElement = LayoutElementBuilders.Image.Builder()
                .setResourceId(resourceId)
                .setWidth(DimensionBuilders.dp(24f))
                .setHeight(DimensionBuilders.dp(24f))
                .setColorFilter(
                    LayoutElementBuilders.ColorFilter.Builder()
                        .setTint(ColorBuilders.argb(textColor))
                        .build()
                )
                .build()

            return LayoutElementBuilders.Box.Builder()
                .setWidth(DimensionBuilders.dp(52f))
                .setHeight(DimensionBuilders.dp(52f))
                .setModifiers(modifiers)
                .addContent(imageElement)
                .build()
        }
    }
}
