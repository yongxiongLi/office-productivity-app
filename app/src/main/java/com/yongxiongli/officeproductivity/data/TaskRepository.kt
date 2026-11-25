package com.yongxiongli.officeproductivity.data

import android.content.Context
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val meetingDao: MeetingDao,
    context: Context,
) {
    private val reminderScheduler = TaskReminderScheduler(WorkManager.getInstance(context))

    val tasks: Flow<List<TaskEntity>> = taskDao.observeTasks()
    val meetings: Flow<List<MeetingEntity>> = meetingDao.observeMeetings()

    suspend fun addTask(title: String, dueAtMillis: Long?, priority: String, assignee: String, meetingId: Long? = null) {
        val cleanTitle = title.trim()
        val id = taskDao.insert(TaskEntity(
            title = cleanTitle,
            dueAtMillis = dueAtMillis,
            priority = priority,
            assignee = assignee.trim(),
            meetingId = meetingId,
        ))
        dueAtMillis?.let { reminderScheduler.schedule(id, cleanTitle, it) }
    }

    suspend fun setCompleted(task: TaskEntity, completed: Boolean) {
        taskDao.setCompleted(task.id, completed, if (completed) System.currentTimeMillis() else null)
        if (completed) reminderScheduler.cancel(task.id)
        else task.dueAtMillis?.let { reminderScheduler.schedule(task.id, task.title, it) }
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.delete(taskId)
        reminderScheduler.cancel(taskId)
    }

    suspend fun addMeeting(
        title: String,
        meetingAtMillis: Long,
        participants: String,
        notes: String,
        actionTitle: String,
        actionAssignee: String,
        actionDueAtMillis: Long?,
        actionPriority: String,
    ) {
        val meetingId = meetingDao.insert(MeetingEntity(
            title = title.trim(),
            meetingAtMillis = meetingAtMillis,
            participants = participants.trim(),
            notes = notes.trim(),
        ))
        if (actionTitle.isNotBlank()) {
            addTask(actionTitle, actionDueAtMillis, actionPriority, actionAssignee, meetingId)
        }
    }

    suspend fun deleteMeeting(meetingId: Long) = meetingDao.delete(meetingId)
}
