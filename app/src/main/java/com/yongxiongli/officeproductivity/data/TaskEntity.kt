package com.yongxiongli.officeproductivity.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueAtMillis: Long? = null,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val priority: String = "MEDIUM",
    val assignee: String = "",
    val meetingId: Long? = null,
    val completedAtMillis: Long? = null,
)
