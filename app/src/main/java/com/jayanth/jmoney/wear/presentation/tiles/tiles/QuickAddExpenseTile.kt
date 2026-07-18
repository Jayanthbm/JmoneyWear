package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import com.jayanth.jmoney.wear.presentation.MainActivity

import com.jayanth.jmoney.wear.presentation.tiles.utils.TileTheme

class QuickAddExpenseTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters
        ): LayoutElementBuilders.LayoutElement {
            val clickable = ModifiersBuilders.Clickable.Builder()
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

            return ActionTileLayout.build(
                context = context,
                deviceParameters = deviceParameters,
                title = "Log Expense",
                buttonText = "Add",
                buttonColor = TileTheme.DANGER,
                clickable = clickable
            )
        }
    }
}
