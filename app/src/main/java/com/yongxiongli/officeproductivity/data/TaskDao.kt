package com.yongxiongli.officeproductivity.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE WHEN dueAtMillis IS NULL THEN 1 ELSE 0 END, dueAtMillis ASC, createdAtMillis DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("UPDATE tasks SET isCompleted = :completed, completedAtMillis = :completedAt WHERE id = :taskId")
    suspend fun setCompleted(taskId: Long, completed: Boolean, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: Long)
}
