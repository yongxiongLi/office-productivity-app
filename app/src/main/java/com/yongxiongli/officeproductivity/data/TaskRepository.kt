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

    suspend fun addTask(
        title: String,
        dueAtMillis: Long?,
        reminderAtMillis: Long?,
        priority: String,
        assignee: String,
        meetingId: Long? = null,
    ) {
        val cleanTitle = title.trim()
        val id = taskDao.insert(
            TaskEntity(
                title = cleanTitle,
                dueAtMillis = dueAtMillis,
                reminderAtMillis = reminderAtMillis,
                priority = priority,
                assignee = assignee.trim(),
                meetingId = meetingId,
            ),
        )
        reminderAtMillis?.let { reminderScheduler.schedule(id, cleanTitle, it) }
    }

    suspend fun setCompleted(task: TaskEntity, completed: Boolean) {
        taskDao.setCompleted(task.id, completed, if (completed) System.currentTimeMillis() else null)
        if (completed) reminderScheduler.cancel(task.id)
        else task.reminderAtMillis?.let { reminderScheduler.schedule(task.id, task.title, it) }
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.delete(taskId)
        reminderScheduler.cancel(taskId)
    }

    suspend fun addMeeting(
        title: String,
        meetingAtMillis: Long,
        meetingReminderAtMillis: Long?,
        participants: String,
        notes: String,
        actionTitle: String,
        actionAssignee: String,
        actionDueAtMillis: Long?,
        actionReminderAtMillis: Long?,
        actionPriority: String,
    ) {
        val cleanTitle = title.trim()
        val meetingId = meetingDao.insert(
            MeetingEntity(
                title = cleanTitle,
                meetingAtMillis = meetingAtMillis,
                participants = participants.trim(),
                notes = notes.trim(),
                reminderAtMillis = meetingReminderAtMillis,
            ),
        )
        meetingReminderAtMillis?.let { reminderScheduler.scheduleMeeting(meetingId, cleanTitle, it) }
        if (actionTitle.isNotBlank()) {
            addTask(actionTitle, actionDueAtMillis, actionReminderAtMillis, actionPriority, actionAssignee, meetingId)
        }
    }

    suspend fun deleteMeeting(meetingId: Long) {
        meetingDao.delete(meetingId)
        reminderScheduler.cancelMeeting(meetingId)
    }
}
