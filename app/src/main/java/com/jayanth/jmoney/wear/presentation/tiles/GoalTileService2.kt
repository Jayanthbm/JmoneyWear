package com.jayanth.jmoney.wear.presentation.tiles

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture
import com.jayanth.jmoney.wear.presentation.tiles.tiles.GoalTile
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
class GoalTileService2 : TileService() {

    @Inject
    lateinit var dataFetcher: TileDataFetcher

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<Tile> {
        return CallbackToFutureAdapter.getFuture<Tile> { completer ->
            serviceScope.launch {
                try {
                    val configKey = "GoalTileService2"
                    val prefs = getSharedPreferences("goal_tile_prefs", Context.MODE_PRIVATE)
                    val goalId = prefs.getString(configKey, null)

                    val isLoggedIn = dataFetcher.isLoggedIn()
                    val goalProgressData = if (isLoggedIn && goalId != null) {
                        dataFetcher.fetchGoalProgress(goalId)
                    } else null

                    val tilesDevParams = requestParams.deviceConfiguration!!
                    val protoDeviceParams = androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters.Builder()
                        .setScreenWidthDp(tilesDevParams.screenWidthDp)
                        .setScreenHeightDp(tilesDevParams.screenHeightDp)
                        .setScreenShape(tilesDevParams.screenShape)
                        .setScreenDensity(tilesDevParams.screenDensity)
                        .build()

                    val layout = GoalTile.buildLayout(
                        this@GoalTileService2,
                        protoDeviceParams,
                        goalProgressData,
                        isLoggedIn,
                        configKey
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
        }
    }

    override fun onTileResourcesRequest(requestParams: ResourcesRequest): ListenableFuture<Resources> {
        return CallbackToFutureAdapter.getFuture { completer ->
            try {
                val resources = TileResourceBuilder.buildResources(this)
                completer.set(resources)
            } catch (e: Exception) {
                completer.setException(e)
            }
            "GoalTileService2ResourcesRequest"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
