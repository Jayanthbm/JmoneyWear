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

class QuickActionsTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters
        ): LayoutElementBuilders.LayoutElement {
            val expenseColors = Colors(0xFFF44336.toInt(), 0xFFFFFFFF.toInt(), 0, 0)
            val incomeColors = Colors(0xFF4CAF50.toInt(), 0xFFFFFFFF.toInt(), 0, 0)
            // Fixed: deeper amber with white icon for better contrast on watch display
            val quickColors = Colors(0xFFFF9800.toInt(), 0xFFFFFFFF.toInt(), 0, 0)

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

            val expenseBtn = Button.Builder(context, expenseClickable)
                .setTextContent("➖")
                .setButtonColors(ButtonColors.primaryButtonColors(expenseColors))
                .build()

            val incomeBtn = Button.Builder(context, incomeClickable)
                .setTextContent("➕")
                .setButtonColors(ButtonColors.primaryButtonColors(incomeColors))
                .build()

            val quickBtn = Button.Builder(context, quickClickable)
                .setTextContent("⚡")
                .setButtonColors(ButtonColors.primaryButtonColors(quickColors))
                .build()

            val row = LayoutElementBuilders.Row.Builder()
                .addContent(expenseBtn)
                .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(8f)).build())
                .addContent(quickBtn)
                .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(8f)).build())
                .addContent(incomeBtn)
                .build()

            return PrimaryLayout.Builder(deviceParameters)
                .setPrimaryLabelTextContent(
                    Text.Builder(context, "QUICK ACTIONS")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(ColorBuilders.argb(0xFF888888.toInt()))
                        .build()
                )
                .setContent(row)
                .build()
        }
    }
}
