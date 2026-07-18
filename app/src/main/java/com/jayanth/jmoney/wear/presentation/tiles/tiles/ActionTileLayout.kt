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
        // Centered title text (medium-small secondary text)
        val titleText = Text.Builder(context, title)
            .setTypography(Typography.TYPOGRAPHY_BODY1)
            .setColor(ColorBuilders.argb(0xFFC4C7C5.toInt())) // TextSecondary gray
            .build()

        // Custom capsule/pill button dimensions (160dp width x 60dp height) for larger padding/thickness
        val buttonWidth = DimensionBuilders.dp(160f)
        val buttonHeight = DimensionBuilders.dp(60f)

        // Pill shape background modifier with 30dp radius (half of 60dp height for perfect capsule)
        val background = ModifiersBuilders.Background.Builder()
            .setColor(ColorBuilders.argb(buttonColor))
            .setCorner(
                ModifiersBuilders.Corner.Builder()
                    .setRadius(DimensionBuilders.dp(30f))
                    .build()
            )
            .build()

        val buttonModifiers = ModifiersBuilders.Modifiers.Builder()
            .setClickable(clickable)
            .setBackground(background)
            .build()

        // Centered bold button label (using TITLE3 for premium sizing)
        val textElement = Text.Builder(context, buttonText)
            .setTypography(Typography.TYPOGRAPHY_TITLE3)
            .setColor(ColorBuilders.argb(0xFF121314.toInt())) // High contrast dark charcoal text
            .build()

        val pillButton = LayoutElementBuilders.Box.Builder()
            .setWidth(buttonWidth)
            .setHeight(buttonHeight)
            .setModifiers(buttonModifiers)
            .addContent(textElement)
            .build()

        // Custom Column layout to control vertical alignments without default PrimaryLayout app icon
        val column = LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(titleText)
            // Spacing between text and button matching Next Alarm widget
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(18f)).build())
            .addContent(pillButton)
            // Bottom spacer to nudge the elements upwards, reducing top spacing
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(45f)).build())
            .build()

        // Wrap in full size Box container with explicit dimensions to guarantee correct rendering
        return LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.dp(deviceParameters.screenWidthDp.toFloat()))
            .setHeight(DimensionBuilders.dp(deviceParameters.screenHeightDp.toFloat()))
            .addContent(column)
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
