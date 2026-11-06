package com.example.data.repository

import android.content.Context
import com.example.alarm.AlarmScheduler
import com.example.data.db.ZakarDatabase
import com.example.data.model.ConflictResult
import com.example.data.model.CustomCategory
import com.example.data.model.MinistryNote
import com.example.data.model.MinistryTask
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class ZakarRepository(
    private val context: Context,
    private val database: ZakarDatabase = ZakarDatabase.getDatabase(context),
    private val alarmScheduler: AlarmScheduler = AlarmScheduler(context)
) {
    val allTasks: Flow<List<MinistryTask>> = database.taskDao().getAllTasks()
    val activeTasks: Flow<List<MinistryTask>> = database.taskDao().getActiveTasks()
    val completedTasks: Flow<List<MinistryTask>> = database.taskDao().getCompletedTasks()
    val alarmsFlow: Flow<List<MinistryTask>> = database.taskDao().getAlarmTasksFlow()

    val allNotes: Flow<List<MinistryNote>> = database.noteDao().getAllNotes()
    val favoriteNotes: Flow<List<MinistryNote>> = database.noteDao().getFavoriteNotes()

    val customCategories: Flow<List<CustomCategory>> = database.categoryDao().getAllCategories()

    suspend fun saveTask(task: MinistryTask): Long {
        val id = database.taskDao().insertTask(task)
        val savedTask = if (task.id == 0L) task.copy(id = id) else task
        if (savedTask.hasAlarm && !savedTask.isCompleted) {
            alarmScheduler.scheduleTaskAlarm(savedTask)
        } else {
            alarmScheduler.cancelTaskAlarm(savedTask.id)
        }
        return id
    }

    suspend fun updateTask(task: MinistryTask) {
        database.taskDao().updateTask(task)
        if (task.hasAlarm && !task.isCompleted) {
            alarmScheduler.scheduleTaskAlarm(task)
        } else {
            alarmScheduler.cancelTaskAlarm(task.id)
        }
    }

    suspend fun deleteTask(task: MinistryTask) {
        alarmScheduler.cancelTaskAlarm(task.id)
        database.taskDao().deleteTask(task)
    }

    suspend fun toggleTaskCompleted(task: MinistryTask) {
        val isNowCompleted = !task.isCompleted
        val updated = task.copy(
            isCompleted = isNowCompleted,
            completedAtMillis = if (isNowCompleted) System.currentTimeMillis() else null
        )
        database.taskDao().updateTask(updated)
        if (isNowCompleted) {
            alarmScheduler.cancelTaskAlarm(task.id)
        } else if (updated.hasAlarm) {
            alarmScheduler.scheduleTaskAlarm(updated)
        }
    }

    suspend fun postponeTask(task: MinistryTask, hours: Int) {
        val newTime = task.dateTimeMillis + (hours * 3600 * 1000L)
        val updated = task.copy(dateTimeMillis = newTime)
        updateTask(updated)
    }

    suspend fun checkConflict(targetTimeMillis: Long, excludeTaskId: Long = 0L): ConflictResult {
        val window = 45 * 60 * 1000L // 45 minutes conflict window
        val start = targetTimeMillis - window
        val end = targetTimeMillis + window
        val conflicting = database.taskDao().getTasksBetween(start, end)
            .firstOrNull { it.id != excludeTaskId && !it.isCompleted }

        return if (conflicting != null) {
            // Suggest slot 1 hour after the conflicting task
            val suggested = conflicting.dateTimeMillis + (60 * 60 * 1000L)
            ConflictResult(hasConflict = true, conflictingTask = conflicting, suggestedSlotMillis = suggested)
        } else {
            ConflictResult(hasConflict = false)
        }
    }

    // Notes
    suspend fun saveNote(note: MinistryNote): Long {
        return database.noteDao().insertNote(note)
    }

    suspend fun updateNote(note: MinistryNote) {
        database.noteDao().updateNote(note)
    }

    suspend fun deleteNote(note: MinistryNote) {
        database.noteDao().deleteNote(note)
    }

    suspend fun toggleNoteFavorite(note: MinistryNote) {
        database.noteDao().updateNote(note.copy(isFavorite = !note.isFavorite))
    }

    suspend fun convertNoteToTask(
        note: MinistryNote,
        dateTimeMillis: Long,
        hasAlarm: Boolean = true
    ): Long {
        val task = MinistryTask(
            title = note.title,
            description = note.content,
            categoryKey = note.categoryKey,
            dateTimeMillis = dateTimeMillis,
            hasAlarm = hasAlarm
        )
        val taskId = saveTask(task)
        database.noteDao().updateNote(note.copy(convertedToTaskId = taskId))
        return taskId
    }

    // Custom Categories
    suspend fun addCategory(category: CustomCategory) {
        database.categoryDao().insertCategory(category)
    }

    // Backup & Restore
    suspend fun exportDataJson(): String {
        val root = JSONObject()
        val tasksArray = JSONArray()
        val notesArray = JSONArray()

        val allT = database.taskDao().getTasksBetween(0, Long.MAX_VALUE)
        for (t in allT) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("description", t.description)
                put("categoryKey", t.categoryKey)
                put("dateTimeMillis", t.dateTimeMillis)
                put("hasAlarm", t.hasAlarm)
                put("alarmOffsetMinutes", t.alarmOffsetMinutes)
                put("repeatType", t.repeatType)
                put("isCompleted", t.isCompleted)
                put("completedAtMillis", t.completedAtMillis ?: JSONObject.NULL)
                put("createdAtMillis", t.createdAtMillis)
            }
            tasksArray.put(obj)
        }

        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("tasks", tasksArray)
        root.put("notes", notesArray)

        return root.toString(2)
    }

    suspend fun importDataJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val task = MinistryTask(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        categoryKey = obj.optString("categoryKey", "general"),
                        dateTimeMillis = obj.getLong("dateTimeMillis"),
                        hasAlarm = obj.optBoolean("hasAlarm", true),
                        alarmOffsetMinutes = obj.optInt("alarmOffsetMinutes", 0),
                        repeatType = obj.optString("repeatType", "NONE"),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                    saveTask(task)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
