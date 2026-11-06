package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ZakarApp
import com.example.alarm.AlarmScheduler
import com.example.data.db.ZakarDatabase
import com.example.data.model.MinistryTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Rappel ZAKAR"
        val desc = intent.getStringExtra(EXTRA_TASK_DESC) ?: ""
        val category = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "general"
        val repeatType = intent.getStringExtra(EXTRA_REPEAT_TYPE) ?: "NONE"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        when (action) {
            ACTION_ALARM_TRIGGER -> {
                showNotification(context, taskId, title, desc, category)

                // If repeating, schedule next occurrence
                if (taskId > 0 && repeatType != "NONE") {
                    handleRecurrence(context, taskId, repeatType)
                }
            }
            ACTION_SNOOZE -> {
                // Dismiss active notification
                notificationManager?.cancel(taskId.toInt())

                // Reschedule in 10 minutes
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L)
                val dummyTask = MinistryTask(
                    id = taskId,
                    title = title,
                    description = desc,
                    categoryKey = category,
                    dateTimeMillis = snoozeTime,
                    hasAlarm = true,
                    repeatType = "NONE"
                )
                AlarmScheduler(context).scheduleTaskAlarm(dummyTask)
            }
            ACTION_COMPLETE -> {
                // Dismiss notification
                notificationManager?.cancel(taskId.toInt())

                // Mark task as completed in DB
                if (taskId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = ZakarDatabase.getDatabase(context)
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null) {
                            db.taskDao().updateTask(
                                task.copy(
                                    isCompleted = true,
                                    completedAtMillis = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showNotification(
        context: Context,
        taskId: Long,
        title: String,
        desc: String,
        category: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // Tap opens MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "TASKS")
            putExtra("TASK_ID", taskId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze intent
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_DESC, desc)
            putExtra(EXTRA_TASK_CATEGORY, category)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 100000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Complete intent
        val completeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 200000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ZakarApp.CHANNEL_ID_ALARMS)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("זָכַר : $title")
            .setContentText(desc.ifEmpty { "Rappel de votre ministère pastoral" })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (desc.isNotEmpty()) "$desc\n(Catégorie : $category)" else "Rappel de votre responsabilité pastorale.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Reporter 10m", snoozePendingIntent)
            .addAction(0, "Marquer comme fait", completePendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    private fun handleRecurrence(context: Context, taskId: Long, repeatType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = ZakarDatabase.getDatabase(context)
            val task = db.taskDao().getTaskById(taskId) ?: return@launch

            val cal = Calendar.getInstance().apply {
                timeInMillis = task.dateTimeMillis
            }
            when (repeatType) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
            }

            val updatedTask = task.copy(dateTimeMillis = cal.timeInMillis)
            db.taskDao().updateTask(updatedTask)
            AlarmScheduler(context).scheduleTaskAlarm(updatedTask)
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.example.zakar.ACTION_ALARM_TRIGGER"
        const val ACTION_SNOOZE = "com.example.zakar.ACTION_SNOOZE"
        const val ACTION_COMPLETE = "com.example.zakar.ACTION_COMPLETE"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
        const val EXTRA_TASK_CATEGORY = "extra_task_category"
        const val EXTRA_REPEAT_TYPE = "extra_repeat_type"
    }
}
