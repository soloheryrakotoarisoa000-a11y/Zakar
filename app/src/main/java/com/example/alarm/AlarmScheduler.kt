package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.MinistryTask
import com.example.receiver.AlarmReceiver

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleTaskAlarm(task: MinistryTask) {
        if (!task.hasAlarm || task.isCompleted) return

        // Calculate trigger time subtracting offset
        val triggerTime = task.dateTimeMillis - (task.alarmOffsetMinutes * 60 * 1000L)
        if (triggerTime <= System.currentTimeMillis()) {
            // Already past
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(AlarmReceiver.EXTRA_TASK_CATEGORY, task.categoryKey)
            putExtra(AlarmReceiver.EXTRA_TASK_DESC, task.description)
            putExtra(AlarmReceiver.EXTRA_REPEAT_TYPE, task.repeatType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { am ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (am.canScheduleExactAlarms()) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                Log.d("AlarmScheduler", "Alarm scheduled for task ${task.id} at $triggerTime")
            } catch (e: SecurityException) {
                Log.e("AlarmScheduler", "Exact alarm permission missing, fallback to inexact", e)
                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    fun cancelTaskAlarm(taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AlarmScheduler", "Alarm cancelled for task $taskId")
        }
    }

    fun triggerTestAlarm() {
        val triggerTime = System.currentTimeMillis() + 2000L // 2 seconds from now
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, -1L)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, "Test d'Alarme – MPAMPAHATSIARO")
            putExtra(AlarmReceiver.EXTRA_TASK_CATEGORY, "prayers")
            putExtra(AlarmReceiver.EXTRA_TASK_DESC, "Le système d'alarme et de rappel ZAKAR fonctionne parfaitement !")
            putExtra(AlarmReceiver.EXTRA_REPEAT_TYPE, "NONE")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }
}
