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
import com.jayanth.jmoney.wear.presentation.tiles.tiles.RemainingForPeriodTile
import com.jayanth.jmoney.wear.presentation.tiles.utils.RemainingData
import com.jayanth.jmoney.wear.presentation.tiles.utils.TileDataFetcher
import com.jayanth.jmoney.wear.presentation.tiles.utils.TileResourceBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RemainingForPeriodTileService : TileService() {

    @Inject
    lateinit var dataFetcher: TileDataFetcher

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<Tile> {
        return CallbackToFutureAdapter.getFuture<Tile> { completer ->
            serviceScope.launch {
                try {
                    val isLoggedIn = dataFetcher.isLoggedIn()
                    val remainingData = if (isLoggedIn) {
                        dataFetcher.fetchRemainingForPeriod()
                    } else {
                        RemainingData(0.0, false)
                    }

                    // Manually map DeviceParameters to avoid package-private toProto() conversion issues
                    val tilesDevParams = requestParams.deviceConfiguration!!
                    val protoDeviceParams = androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters.Builder()
                        .setScreenWidthDp(tilesDevParams.screenWidthDp)
                        .setScreenHeightDp(tilesDevParams.screenHeightDp)
                        .setScreenShape(tilesDevParams.screenShape)
                        .setScreenDensity(tilesDevParams.screenDensity)
                        .build()

                    val layout = RemainingForPeriodTile.buildLayout(
                        this@RemainingForPeriodTileService,
                        protoDeviceParams,
                        remainingData,
                        isLoggedIn
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
            }
            "RemainingForPeriodTileRequest"
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
            "RemainingForPeriodTileResourcesRequest"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
