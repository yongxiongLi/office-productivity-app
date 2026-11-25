package com.yongxiongli.officeproductivity.data

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TaskReminderScheduler(private val workManager: WorkManager) {
    fun schedule(taskId: Long, title: String, reminderAtMillis: Long) {
        enqueue(
            uniqueName = taskWorkName(taskId),
            reminderId = taskId,
            title = title,
            reminderAtMillis = reminderAtMillis,
            kind = TaskReminderWorker.KIND_TASK,
        )
    }

    fun scheduleMeeting(meetingId: Long, title: String, reminderAtMillis: Long) {
        enqueue(
            uniqueName = meetingWorkName(meetingId),
            reminderId = meetingId,
            title = title,
            reminderAtMillis = reminderAtMillis,
            kind = TaskReminderWorker.KIND_MEETING,
        )
    }

    private fun enqueue(uniqueName: String, reminderId: Long, title: String, reminderAtMillis: Long, kind: String) {
        val delayMillis = (reminderAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(TaskReminderWorker.KEY_REMINDER_ID, reminderId)
                    .putString(TaskReminderWorker.KEY_REMINDER_TITLE, title)
                    .putString(TaskReminderWorker.KEY_REMINDER_KIND, kind)
                    .build(),
            )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(taskId: Long) {
        workManager.cancelUniqueWork(taskWorkName(taskId))
    }

    fun cancelMeeting(meetingId: Long) {
        workManager.cancelUniqueWork(meetingWorkName(meetingId))
    }

    private fun taskWorkName(taskId: Long) = "task-reminder-$taskId"
    private fun meetingWorkName(meetingId: Long) = "meeting-reminder-$meetingId"
}
