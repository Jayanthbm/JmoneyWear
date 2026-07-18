package com.jayanth.jmoney.wear.data.work

import android.content.Context
import androidx.wear.tiles.TileService
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService2
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService3
import com.jayanth.jmoney.wear.presentation.tiles.BudgetTileService4
import com.jayanth.jmoney.wear.presentation.tiles.GoalTileService
import com.jayanth.jmoney.wear.presentation.tiles.GoalTileService2
import com.jayanth.jmoney.wear.presentation.tiles.GoalTileService3
import com.jayanth.jmoney.wear.presentation.tiles.MonthlyExpenseTileService
import com.jayanth.jmoney.wear.presentation.tiles.MonthlyIncomeTileService
import com.jayanth.jmoney.wear.presentation.tiles.NetWorthTileService
import com.jayanth.jmoney.wear.presentation.tiles.RemainingForPeriodTileService
import com.jayanth.jmoney.wear.presentation.tiles.UpcomingPayDayTileService

/**
 * Requests a UI update for all data tiles (excludes action-only tiles which never change).
 * Call this after any sync (WorkManager or manual) completes successfully.
 */
object TileUpdateHelper {
    fun requestAllTileUpdates(context: Context) {
        val updater = TileService.getUpdater(context)
        updater.requestUpdate(RemainingForPeriodTileService::class.java)
        updater.requestUpdate(UpcomingPayDayTileService::class.java)
        updater.requestUpdate(MonthlyExpenseTileService::class.java)
        updater.requestUpdate(MonthlyIncomeTileService::class.java)
        updater.requestUpdate(NetWorthTileService::class.java)
        updater.requestUpdate(BudgetTileService::class.java)
        updater.requestUpdate(BudgetTileService2::class.java)
        updater.requestUpdate(BudgetTileService3::class.java)
        updater.requestUpdate(BudgetTileService4::class.java)
        updater.requestUpdate(GoalTileService::class.java)
        updater.requestUpdate(GoalTileService2::class.java)
        updater.requestUpdate(GoalTileService3::class.java)
        // Action tiles (Add Expense, Add Income, Quick Add, Quick Actions) excluded —
        // they display no data and never need to refresh.
    }
}
