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

class QuickTransactionTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters
        ): LayoutElementBuilders.LayoutElement {
            val colors = Colors(0xFFFFC107.toInt(), 0xFF000000.toInt(), 0, 0) // Amber/Yellow for quick add

            val clickable = ModifiersBuilders.Clickable.Builder()
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

            val quickButton = Button.Builder(context, clickable)
                .setTextContent("⚡")
                .setButtonColors(ButtonColors.primaryButtonColors(colors))
                .build()

            return PrimaryLayout.Builder(deviceParameters)
                .setContent(quickButton)
                .setSecondaryLabelTextContent(
                    Text.Builder(context, "QUICK ADD")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                .build()
        }
    }
}
