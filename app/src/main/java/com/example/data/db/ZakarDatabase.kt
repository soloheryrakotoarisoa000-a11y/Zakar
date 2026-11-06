package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomCategory
import com.example.data.model.MinistryNote
import com.example.data.model.MinistryTask

@Database(
    entities = [MinistryTask::class, MinistryNote::class, CustomCategory::class],
    version = 1,
    exportSchema = false
)
abstract class ZakarDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ZakarDatabase? = null

        fun getDatabase(context: Context): ZakarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZakarDatabase::class.java,
                    "zakar_ministry_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
