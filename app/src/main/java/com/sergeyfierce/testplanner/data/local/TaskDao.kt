package com.sergeyfierce.testplanner.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY start ASC")
    fun observeTasksForDate(date: String): Flow<List<TaskEntity>>

    @Query(
        "SELECT * FROM tasks WHERE date BETWEEN :start AND :end ORDER BY date ASC, start ASC"
    )
    fun observeTasksBetween(start: String, end: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date")
    suspend fun getTasksForDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE parent_id = :parentId")
    suspend fun getChildren(parentId: String): List<TaskEntity>

    @Transaction
    suspend fun deleteCascade(taskId: String) {
        val children = getChildren(taskId)
        children.forEach { deleteById(it.id) }
        deleteById(taskId)
    }
}
