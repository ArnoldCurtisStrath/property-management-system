package com.example.propertymanagementsystem.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.propertymanagementsystem.SahihiApp

/**
 * Builds the morning brief that the agent sees even before opening the app.
 *
 * The heavy lifting (the totals, the arrears, the repairs) is in the ledger
 * repository, the same code the Today screen uses. The worker only asks for a
 * snapshot and hands it to the notification.
 */
class RentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as SahihiApp
            val today = app.container.ledger.snapshotToday()
            NotificationHelper.showDailyBrief(applicationContext, today)

            Result.success(
                workDataOf(
                    KEY_COLLECTED to today.collected,
                    KEY_EXPECTED to today.expected,
                    KEY_ARREARS to today.arrearsAmount,
                    KEY_OPEN_REPAIRS to today.openRepairs
                )
            )
        } catch (error: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_COLLECTED = "collected"
        const val KEY_EXPECTED = "expected"
        const val KEY_ARREARS = "arrears"
        const val KEY_OPEN_REPAIRS = "open_repairs"
    }
}
