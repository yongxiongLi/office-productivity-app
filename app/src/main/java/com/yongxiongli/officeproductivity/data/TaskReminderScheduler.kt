package com.yongxiongli.officeproductivity.data

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TaskReminderScheduler(private val workManager: WorkManager) {
    fun schedule(taskId: Long, title: String, dueAtMillis: Long) {
        val delayMillis = (dueAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(
                Data.Builder()
                    .putLong(TaskReminderWorker.KEY_TASK_ID, taskId)
                    .putString(TaskReminderWorker.KEY_TASK_TITLE, title)
                    .build(),
            )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            workName(taskId),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(taskId: Long) {
        workManager.cancelUniqueWork(workName(taskId))
    }

    private fun workName(taskId: Long) = "task-reminder-$taskId"
}
