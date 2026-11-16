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
import com.example.propertymanagementsystem.ui.main.MainActivity

/**
 * Shows the notification that reminds about the rent.
 *
 * On Android 8 and above a notification has to belong to a channel, so the
 * channel is made first. The channel is what the user sets the sound and the
 * vibration of the notification under.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "rent_reminders"
    private const val NOTIFICATION_ID = 1001

    // Makes the channel the notifications belong to.
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

    // Shows the reminder. The number of tenants that have not paid is put in
    // the message.
    fun showRentReminder(context: Context, unpaid: Int) {
        if (unpaid <= 0) return

        // From Android 13 the user has to allow notifications, so the
        // permission is checked before the notification is shown.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val allowed = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!allowed) return
        }

        createChannel(context)

        // Tapping the notification opens the app.
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text, unpaid))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
