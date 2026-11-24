package com.yongxiongli.officeproductivity.data

import android.content.Context
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    context: Context,
) {
    private val reminderScheduler = TaskReminderScheduler(WorkManager.getInstance(context))

    val tasks: Flow<List<TaskEntity>> = taskDao.observeTasks()

    suspend fun addTask(title: String, dueAtMillis: Long?) {
        val taskId = taskDao.insert(
            TaskEntity(title = title.trim(), dueAtMillis = dueAtMillis),
        )
        if (dueAtMillis != null) {
            reminderScheduler.schedule(taskId, title.trim(), dueAtMillis)
        }
    }

    suspend fun setCompleted(task: TaskEntity, completed: Boolean) {
        taskDao.setCompleted(task.id, completed)
        if (completed) {
            reminderScheduler.cancel(task.id)
        } else {
            task.dueAtMillis?.let { dueAt ->
                reminderScheduler.schedule(task.id, task.title, dueAt)
            }
        }
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.delete(taskId)
        reminderScheduler.cancel(taskId)
    }
}
