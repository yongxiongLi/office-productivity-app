package com.yongxiongli.officeproductivity.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.yongxiongli.officeproductivity.MainActivity
import com.yongxiongli.officeproductivity.R

class TaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Fall back to the old keys so reminders already queued by an earlier app version still work.
        val reminderId = inputData.getLong(KEY_REMINDER_ID, inputData.getLong(KEY_TASK_ID, -1L))
        val title = inputData.getString(KEY_REMINDER_TITLE) ?: inputData.getString(KEY_TASK_TITLE).orEmpty()
        val kind = inputData.getString(KEY_REMINDER_KIND) ?: KIND_TASK
        if (reminderId < 0L || title.isBlank()) return Result.failure()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        val alertIntent = Intent(applicationContext, ReminderAlertService::class.java)
            .setAction(ReminderAlertService.ACTION_START)
            .putExtra(ReminderAlertService.EXTRA_TITLE, title)
            .putExtra(ReminderAlertService.EXTRA_KIND, kind)
        return try {
            startForegroundService(applicationContext, alertIntent)
            Result.success()
        } catch (_: RuntimeException) {
            // Some OEM/Android background restrictions can reject a service start. Keep a normal
            // one-shot notification as a fallback so the reminder is not lost completely.
            showOneShotNotification(reminderId, title, kind)
            Result.success()
        }
    }

    private fun showOneShotNotification(reminderId: Long, title: String, kind: String) {
        createNotificationChannel()
        val notificationId = if (kind == KIND_MEETING) "meeting-$reminderId".hashCode() else reminderId.hashCode()
        val openAppIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notificationTitle = if (kind == KIND_MEETING) "会议提醒" else applicationContext.getString(R.string.task_reminder_title)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.task_reminder_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_REMINDER_TITLE = "reminder_title"
        const val KEY_REMINDER_KIND = "reminder_kind"
        const val KIND_TASK = "task"
        const val KIND_MEETING = "meeting"
        private const val CHANNEL_ID = "task_reminders"
    }
}
