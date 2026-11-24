package com.example.propertymanagementsystem.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.ui.main.MainActivity
import com.example.propertymanagementsystem.util.Money

/**
 * The daily brief notification: the month's run, the arrears and the repairs,
 * in one glance before the agent leaves the house.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "sahihi_brief"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = context.getString(R.string.notification_channel_description)

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showDailyBrief(context: Context, today: TodayState) {
        if (!today.hasData) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val allowed = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!allowed) return
        }

        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = buildString {
            append(Money.format(today.collected))
            append(" of ")
            append(Money.format(today.expected))
            append(" collected. ")
            append(today.arrearsCount)
            append(" tenant")
            if (today.arrearsCount != 1) append("s")
            append(" owe ")
            append(Money.format(today.arrearsAmount))
            append(". ")
            append(today.openRepairs)
            append(" repair")
            if (today.openRepairs != 1) append("s")
            append(" open.")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sahihi · today's brief")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
