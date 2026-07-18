package com.jayanth.jmoney.wear.presentation.tiles

import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture
import com.jayanth.jmoney.wear.presentation.tiles.tiles.UpcomingPayDayTile
import com.jayanth.jmoney.wear.presentation.tiles.utils.TileResourceBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpcomingPayDayTileService : TileService() {

    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<Tile> {
        return CallbackToFutureAdapter.getFuture<Tile> { completer ->
            try {
                // Convert wear.tiles DeviceParameters to protolayout DeviceParameters
                val tilesDevParams = requestParams.deviceConfiguration!!
                val protoDeviceParams = androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters.Builder()
                    .setScreenWidthDp(tilesDevParams.screenWidthDp)
                    .setScreenHeightDp(tilesDevParams.screenHeightDp)
                    .setScreenShape(tilesDevParams.screenShape)
                    .setScreenDensity(tilesDevParams.screenDensity)
                    .build()

                val layout = UpcomingPayDayTile.buildLayout(
                    this@UpcomingPayDayTileService,
                    protoDeviceParams
                )

                val tile = Tile.Builder()
                    .setResourcesVersion(TileResourceBuilder.RESOURCES_VERSION)
                    .setTileTimeline(
                        TimelineBuilders.Timeline.Builder()
                            .addTimelineEntry(
                                TimelineBuilders.TimelineEntry.Builder()
                                    .setLayout(
                                        LayoutElementBuilders.Layout.Builder()
                                            .setRoot(layout)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()

                completer.set(tile)
            } catch (e: Exception) {
                completer.setException(e)
            }
            "UpcomingPayDayTileRequest"
        }
    }

    override fun onTileResourcesRequest(requestParams: ResourcesRequest): ListenableFuture<Resources> {
        return CallbackToFutureAdapter.getFuture<Resources> { completer ->
            try {
                val resources = TileResourceBuilder.buildResources(this)
                completer.set(resources)
            } catch (e: Exception) {
                completer.setException(e)
            }
            "UpcomingPayDayTileResourcesRequest"
        }
    }
}
