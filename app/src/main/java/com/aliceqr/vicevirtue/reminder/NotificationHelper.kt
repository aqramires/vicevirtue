package com.aliceqr.vicevirtue.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aliceqr.vicevirtue.MainActivity
import com.aliceqr.vicevirtue.R
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "reminder_channel_v2"
        private const val CHANNEL_NAME = "Daily Reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for daily habit reminders"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(trackableId: Long, trackableName: String, trackableType: com.aliceqr.vicevirtue.domain.model.TrackableType) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("deep_link_trackable_id", trackableId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            trackableId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isVice = trackableType == com.aliceqr.vicevirtue.domain.model.TrackableType.VICE
        val iconRes = if (isVice) R.drawable.ic_widget_skull else R.drawable.ic_widget_shield
        val textRes = if (isVice) R.string.vice_notification_text else R.string.virtue_notification_text
        
        val title = context.getString(R.string.notification_title)
        val text = context.getString(textRes, trackableName)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setColor(if (isVice) 0xFFD32F2F.toInt() else 0xFF1976D2.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(trackableId.toInt(), notification)
    }
}
