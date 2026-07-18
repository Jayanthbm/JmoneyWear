package com.jayanth.jmoney.wear.presentation.tiles.utils

import androidx.wear.protolayout.material.Colors

object TileTheme {
    const val PRIMARY = 0xFF9ECAFF.toInt()
    const val BACKGROUND = 0xFF121314.toInt()
    const val TEXT = 0xFFE2E2E2.toInt()
    const val TEXT_SECONDARY = 0xFFC4C7C5.toInt()
    const val CARD_BACKGROUND = 0xFF1E1F20.toInt()
    const val SUCCESS = 0xFF81C995.toInt()
    const val DANGER = 0xFFF2B8B5.toInt()
    const val ERROR = 0xFF8C1D18.toInt()

    val colors = Colors(
        PRIMARY,
        TEXT,
        BACKGROUND,
        TEXT_SECONDARY
    )
}
