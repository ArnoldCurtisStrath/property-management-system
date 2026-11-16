package com.example.propertymanagementsystem.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Sets up the work that runs in the background.
 *
 * The rent check is put in as one piece of work that runs once a day. The
 * name is given to it so the same work is not added twice when the app is
 * opened again.
 */
object WorkScheduler {

    private const val WORK_NAME = "rent_reminder"

    // The work is only done when the phone is not low on battery, so the
    // check does not use up the battery for nothing.
    private const val ONE_DAY = 1L

    fun start(context: Context) {
        val request = PeriodicWorkRequest.Builder(
            RentReminderWorker::class.java,
            ONE_DAY,
            TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder().setRequiresBatteryNotLow(true).build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
