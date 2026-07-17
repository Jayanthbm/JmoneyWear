package com.jayanth.jmoney.wear.presentation.tiles.tiles

import android.content.Context
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout

/**
 * Reusable helper for building Wear OS action tiles with an alarm-widget-inspired design.
 *
 * Produces a layout with:
 * - Solid black background (default for PrimaryLayout)
 * - Centered gray title near the top (Material Typography TITLE2 ~20sp)
 * - Generous spacing between title and a large pill-shaped Chip action button
 * - Chip covers ~89% of tile width with proper brand colors
 * - Built-in Material press feedback via ChipColors
 *
 * Note: ProtoLayout does not expose custom button height APIs, so Chip's standard
 * Material height (~48dp) is used. On a Wear OS 480x480 display this provides a
 * comfortable, proportional tap target.
 */
object ActionTileLayout {

    /** Button width as fraction of tile width */
    private const val BUTTON_WIDTH_FRACTION = 0.89f

    /** Vertical spacer between title and button */
    private const val SPACER_HEIGHT_DP = 28f

    /**
     * Builds the full tile layout.
     *
     * @param title        Centered gray heading (e.g. "Expense", "Income", "Quick Add")
     * @param buttonText   Label inside the pill button (e.g. "Add", "Presets")
     * @param buttonColor  ARGB int for the pill button background (e.g. 0xFFFF453A)
     * @param clickable    Pre-built Clickable that defines the button's action
     */
    fun build(
        context: Context,
        deviceParameters: DeviceParameters,
        title: String,
        buttonText: String,
        buttonColor: Int,
        clickable: ModifiersBuilders.Clickable
    ): LayoutElementBuilders.LayoutElement {
        // Darker shade for pressed/activated state (built-in Material feedback via ChipColors)
        val darkColor = darkenColor(buttonColor, 0.7f)

        val chipColors = ChipColors.primaryChipColors(
            Colors(buttonColor, 0xFFFFFFFF.toInt(), darkColor, 0xFF888888.toInt())
        )

        // ~89% of total tile width → centered pill button
        val chipWidthDp = deviceParameters.screenWidthDp * BUTTON_WIDTH_FRACTION

        val chip = Chip.Builder(context, clickable, deviceParameters)
            .setPrimaryLabelContent(buttonText)
            .setChipColors(chipColors)
            .setWidth(DimensionBuilders.dp(chipWidthDp))
            .build()

        // 28dp spacer + chip for balanced vertical layout
        val contentColumn = LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(SPACER_HEIGHT_DP)).build())
            .addContent(chip)
            .build()

        return PrimaryLayout.Builder(deviceParameters)
            .setPrimaryLabelTextContent(
                Text.Builder(context, title)
                    .setTypography(Typography.TYPOGRAPHY_TITLE2)
                    .setColor(ColorBuilders.argb(0xFFB8B8B8.toInt()))
                    .build()
            )
            .setContent(contentColumn)
            .build()
    }

    /** Darkens an ARGB color by multiplying RGB channels by [factor] (0..1). */
    private fun darkenColor(color: Int, factor: Float): Int {
        val r = ((color shr 16) and 0xFF) * factor
        val g = ((color shr 8) and 0xFF) * factor
        val b = (color and 0xFF) * factor
        return (0xFF shl 24) or ((r.toInt() and 0xFF) shl 16) or ((g.toInt() and 0xFF) shl 8) or (b.toInt() and 0xFF)
    }
}
