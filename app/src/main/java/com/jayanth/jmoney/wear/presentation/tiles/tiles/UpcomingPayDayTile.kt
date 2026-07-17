package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UpcomingPayDayTile {
    companion object {
        fun buildLayout(
            context: Context,
            deviceParameters: DeviceParameters
        ): LayoutElementBuilders.LayoutElement {
            val today = LocalDate.now()
            val daysInMonth = today.lengthOfMonth()
            val currentDay = today.dayOfMonth
            val remainingDays = daysInMonth - currentDay + 1
            val nextPayDateInstant = today.plusMonths(1).withDayOfMonth(1)
            val formattedNextMonth = nextPayDateInstant.format(DateTimeFormatter.ofPattern("MMM 01"))

            val daysText = if (remainingDays == 1) "1 day" else "$remainingDays days"

            // Primary theme color
            val colors = Colors(0xFF3F51B5.toInt(), 0xFFFFFFFF.toInt(), 0, 0)

            return PrimaryLayout.Builder(deviceParameters)
                .setPrimaryLabelTextContent(
                    Text.Builder(context, "TO PAY DAY")
                        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                        .setColor(ColorBuilders.argb(0xFF888888.toInt()))
                        .build()
                )
                .setContent(
                    Text.Builder(context, daysText)
                        .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build()
                )
                .setSecondaryLabelTextContent(
                    Text.Builder(context, formattedNextMonth)
                        .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                        .setColor(ColorBuilders.argb(colors.primary))
                        .build()
                )
                .build()
        }
    }
}
