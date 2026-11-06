package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomCategory
import com.example.data.model.MinistryNote
import com.example.data.model.MinistryTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM ministry_tasks ORDER BY dateTimeMillis ASC")
    fun getAllTasks(): Flow<List<MinistryTask>>

    @Query("SELECT * FROM ministry_tasks WHERE isCompleted = 0 ORDER BY dateTimeMillis ASC")
    fun getActiveTasks(): Flow<List<MinistryTask>>

    @Query("SELECT * FROM ministry_tasks WHERE isCompleted = 1 ORDER BY completedAtMillis DESC")
    fun getCompletedTasks(): Flow<List<MinistryTask>>

    @Query("SELECT * FROM ministry_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): MinistryTask?

    @Query("SELECT * FROM ministry_tasks WHERE dateTimeMillis >= :startMillis AND dateTimeMillis <= :endMillis ORDER BY dateTimeMillis ASC")
    suspend fun getTasksBetween(startMillis: Long, endMillis: Long): List<MinistryTask>

    @Query("SELECT * FROM ministry_tasks WHERE isCompleted = 0 AND hasAlarm = 1 AND dateTimeMillis >= :nowMillis ORDER BY dateTimeMillis ASC")
    suspend fun getUpcomingAlarmTasks(nowMillis: Long): List<MinistryTask>

    @Query("SELECT * FROM ministry_tasks WHERE isCompleted = 0 AND hasAlarm = 1 ORDER BY dateTimeMillis ASC")
    fun getAlarmTasksFlow(): Flow<List<MinistryTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: MinistryTask): Long

    @Update
    suspend fun updateTask(task: MinistryTask)

    @Delete
    suspend fun deleteTask(task: MinistryTask)

    @Query("DELETE FROM ministry_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM ministry_tasks")
    suspend fun clearAll()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM ministry_notes ORDER BY updatedAtMillis DESC")
    fun getAllNotes(): Flow<List<MinistryNote>>

    @Query("SELECT * FROM ministry_notes WHERE isFavorite = 1 ORDER BY updatedAtMillis DESC")
    fun getFavoriteNotes(): Flow<List<MinistryNote>>

    @Query("SELECT * FROM ministry_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): MinistryNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: MinistryNote): Long

    @Update
    suspend fun updateNote(note: MinistryNote)

    @Delete
    suspend fun deleteNote(note: MinistryNote)

    @Query("DELETE FROM ministry_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM ministry_notes")
    suspend fun clearAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CustomCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory): Long

    @Delete
    suspend fun deleteCategory(category: CustomCategory)

    @Query("DELETE FROM custom_categories")
    suspend fun clearAll()
}
