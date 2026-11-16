package com.example.propertymanagementsystem.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.util.Dates

/**
 * Checks which tenants have not paid the rent for the current month.
 *
 * The work is done here and not on a screen because it has to happen even
 * when the app is not open. WorkManager is used because it makes sure the
 * work is done even when the phone is switched off at the time. The work is
 * then done as soon as the phone is switched on again.
 *
 * CoroutineWorker is used so the reading of the database can be waited for
 * without stopping the work.
 */
class RentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val month = Dates.currentMonth()

            val tenants = database.tenantDao().getActiveOnce()

            // The tenants that have no payment for this month.
            val unpaid = mutableListOf<String>()
            for (tenant in tenants) {
                val payments = database.paymentDao().countPaidOnce(tenant.id, month)
                if (payments == 0) {
                    unpaid.add(tenant.name)
                }
            }

            // The reminder is shown when there is somebody who has not paid.
            NotificationHelper.showRentReminder(applicationContext, unpaid.size)

            Result.success(workDataOf(KEY_UNPAID to unpaid.size))
        } catch (error: Exception) {
            // Something went wrong, for example the database was busy. The
            // work is asked to try again later.
            Result.retry()
        }
    }

    companion object {
        // The number of tenants that have not paid is given back to the app.
        const val KEY_UNPAID = "unpaid_count"
    }
}
