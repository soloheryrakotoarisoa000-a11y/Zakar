package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

class ZakarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // High Priority Ministry Alarm Channel
            val alarmChannelId = CHANNEL_ID_ALARMS
            val alarmChannelName = "ZAKAR Rappels & Alarmes"
            val alarmChannelDesc = "Notifications d'alarmes et de rappels du ministère ZAKAR"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(alarmChannelId, alarmChannelName, importance).apply {
                description = alarmChannelDesc
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // General Ministry Notices Channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "ZAKAR Notifications Générales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications d'organisation et synthèses du ministère"
            }

            notificationManager?.createNotificationChannel(alarmChannel)
            notificationManager?.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_ALARMS = "zakar_ministry_alarms"
        const val CHANNEL_ID_GENERAL = "zakar_ministry_general"

        lateinit var instance: ZakarApp
            private set
    }
}
