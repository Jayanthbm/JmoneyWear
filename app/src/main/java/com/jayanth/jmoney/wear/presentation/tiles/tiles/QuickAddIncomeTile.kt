package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import com.jayanth.jmoney.wear.presentation.MainActivity

class QuickAddIncomeTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters
        ): LayoutElementBuilders.LayoutElement {
            val colors = Colors(0xFF4CAF50.toInt(), 0xFFFFFFFF.toInt(), 0, 0) // Green for income

            val clickable = ModifiersBuilders.Clickable.Builder()
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

            val plusButton = Button.Builder(context, clickable)
                .setTextContent("➕")
                .setButtonColors(ButtonColors.primaryButtonColors(colors))
                .build()

            return PrimaryLayout.Builder(deviceParameters)
                .setContent(plusButton)
                .setSecondaryLabelTextContent(
                    Text.Builder(context, "ADD INCOME")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                .build()
        }
    }
}
