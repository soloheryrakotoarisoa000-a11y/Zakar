package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.AlarmScheduler
import com.example.data.db.ZakarDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("BootReceiver", "Received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = ZakarDatabase.getDatabase(context)
                    val now = System.currentTimeMillis()
                    val tasksToReschedule = db.taskDao().getUpcomingAlarmTasks(now)
                    val scheduler = AlarmScheduler(context)

                    for (task in tasksToReschedule) {
                        scheduler.scheduleTaskAlarm(task)
                    }
                    Log.d("BootReceiver", "Rescheduled ${tasksToReschedule.size} alarms after boot/timezone change")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
