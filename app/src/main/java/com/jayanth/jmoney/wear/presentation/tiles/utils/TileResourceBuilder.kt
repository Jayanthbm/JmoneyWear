package com.jayanth.jmoney.wear.presentation.tiles.utils

import android.content.Context
import androidx.wear.protolayout.ResourceBuilders

class TileResourceBuilder {
    companion object {
        const val RESOURCES_VERSION = "1"

        fun buildResources(
            context: Context
        ): ResourceBuilders.Resources {
            return ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        }
    }
}
