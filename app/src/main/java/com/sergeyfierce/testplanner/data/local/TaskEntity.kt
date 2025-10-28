package com.sergeyfierce.testplanner.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["parent_id"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "parent_id")
    val parentId: String?,
    val title: String,
    val description: String?,
    val date: String,
    val type: String,
    val start: String,
    val end: String?,
    @ColumnInfo(name = "is_important")
    val isImportant: Boolean,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
