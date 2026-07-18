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
                .addIdToImageMapping(
                    "ic_remove",
                    ResourceBuilders.ImageResource.Builder()
                        .setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(com.jayanth.jmoney.wear.R.drawable.ic_remove)
                                .build()
                        )
                        .build()
                )
                .addIdToImageMapping(
                    "ic_add",
                    ResourceBuilders.ImageResource.Builder()
                        .setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(com.jayanth.jmoney.wear.R.drawable.ic_add)
                                .build()
                        )
                        .build()
                )
                .addIdToImageMapping(
                    "ic_bolt",
                    ResourceBuilders.ImageResource.Builder()
                        .setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(com.jayanth.jmoney.wear.R.drawable.ic_bolt)
                                .build()
                        )
                        .build()
                )
                .build()
        }
    }
}
