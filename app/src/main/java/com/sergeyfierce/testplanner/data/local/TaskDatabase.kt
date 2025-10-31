package com.sergeyfierce.testplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        fun build(context: Context): TaskDatabase = Room.databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            "tasks.db"
        ).fallbackToDestructiveMigration().build()
    }
}
