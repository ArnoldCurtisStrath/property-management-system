package com.example.propertymanagementsystem.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Sets up the work that runs in the background.
 *
 * The daily brief is one piece of work that runs once a day. The name is
 * given to it so the same work is not added twice when the app is opened
 * again. The first run is held until 7am so the notification arrives with
 * the morning, not in the middle of the afternoon.
 */
object WorkScheduler {

    private const val WORK_NAME = "daily_brief"
    private const val ONE_DAY = 1L

    fun start(context: Context) {
        val request = PeriodicWorkRequest.Builder(
            RentReminderWorker::class.java,
            ONE_DAY,
            TimeUnit.DAYS
        )
            .setInitialDelay(millisUntilTomorrowMorning(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder().setRequiresBatteryNotLow(true).build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Used by the "Send today's brief now" button in the More screen.
     */
    fun runBriefNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<RentReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private fun millisUntilTomorrowMorning(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return (next.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
    }
}
