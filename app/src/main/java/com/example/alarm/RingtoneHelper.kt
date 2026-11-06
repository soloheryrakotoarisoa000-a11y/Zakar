package com.example.alarm

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

data class RingtoneOption(
    val title: String,
    val uriString: String
)

object RingtoneHelper {

    private var currentPlayingRingtone: Ringtone? = null

    fun getDeviceRingtones(context: Context): List<RingtoneOption> {
        val list = mutableListOf<RingtoneOption>()

        // 1. Default Alarm
        val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (defaultAlarmUri != null) {
            val r = RingtoneManager.getRingtone(context, defaultAlarmUri)
            val title = r?.getTitle(context) ?: "Alarme par défaut"
            list.add(RingtoneOption(title = "🔔 $title (Par défaut)", uriString = defaultAlarmUri.toString()))
        }

        // 2. Query system ringtones and alarms
        try {
            val ringtoneManager = RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_NOTIFICATION or RingtoneManager.TYPE_RINGTONE)
            }
            val cursor = ringtoneManager.cursor
            val count = cursor.count

            for (i in 0 until count) {
                val ringtoneUri = ringtoneManager.getRingtoneUri(i)
                val ringtone = ringtoneManager.getRingtone(i)
                val title = ringtone?.getTitle(context) ?: "Sonnerie $i"

                if (ringtoneUri != null) {
                    val uriStr = ringtoneUri.toString()
                    // Avoid duplicate of default if already added
                    if (list.none { it.uriString == uriStr }) {
                        list.add(RingtoneOption(title = title, uriString = uriStr))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RingtoneHelper", "Error loading ringtones", e)
        }

        // Fallback default notification sound if list empty
        if (list.isEmpty()) {
            val defaultNotif = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (defaultNotif != null) {
                list.add(RingtoneOption(title = "Sonnerie Standard", uriString = defaultNotif.toString()))
            }
        }

        return list
    }

    fun getRingtoneTitle(context: Context, uriString: String?): String {
        if (uriString.isNullOrEmpty()) return "Alarme par défaut"
        return try {
            val uri = Uri.parse(uriString)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.getTitle(context) ?: "Sonnerie personnalisée"
        } catch (e: Exception) {
            "Sonnerie du téléphone"
        }
    }

    fun playPreview(context: Context, uriString: String) {
        stopPreview()
        try {
            val uri = Uri.parse(uriString)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            currentPlayingRingtone = ringtone
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("RingtoneHelper", "Error playing ringtone preview", e)
        }
    }

    fun stopPreview() {
        try {
            if (currentPlayingRingtone?.isPlaying == true) {
                currentPlayingRingtone?.stop()
            }
        } catch (e: Exception) {
            Log.e("RingtoneHelper", "Error stopping preview", e)
        } finally {
            currentPlayingRingtone = null
        }
    }
}
