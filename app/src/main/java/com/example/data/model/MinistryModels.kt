package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ministry_tasks")
data class MinistryTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryKey: String = "general",
    val dateTimeMillis: Long,
    val hasAlarm: Boolean = true,
    val alarmOffsetMinutes: Int = 0, // 0 = at the time, 15 = 15 min before, 60 = 1 hr before, 1440 = 1 day before
    val repeatType: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val contactPerson: String? = null,
    val location: String? = null
)

@Entity(tableName = "ministry_notes")
data class MinistryNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val categoryKey: String = "sermons",
    val isFavorite: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val convertedToTaskId: Long? = null
)

@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val name: String,
    val iconName: String = "folder"
)

enum class TaskRepeat(val value: String) {
    NONE("NONE"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY")
}

data class ConflictResult(
    val hasConflict: Boolean,
    val conflictingTask: MinistryTask? = null,
    val suggestedSlotMillis: Long? = null
)
