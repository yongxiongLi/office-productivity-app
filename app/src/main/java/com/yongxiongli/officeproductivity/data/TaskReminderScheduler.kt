package com.yongxiongli.officeproductivity.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class TaskReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(taskId: Long, title: String, reminderAtMillis: Long) {
        scheduleAlarm(
            reminderId = taskId,
            title = title,
            reminderAtMillis = reminderAtMillis,
            kind = TaskReminderReceiver.KIND_TASK,
        )
    }

    fun scheduleMeeting(meetingId: Long, title: String, reminderAtMillis: Long) {
        scheduleAlarm(
            reminderId = meetingId,
            title = title,
            reminderAtMillis = reminderAtMillis,
            kind = TaskReminderReceiver.KIND_MEETING,
        )
    }

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(
                Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }

    private fun scheduleAlarm(reminderId: Long, title: String, reminderAtMillis: Long, kind: String) {
        val intent = TaskReminderReceiver.createIntent(context, reminderId, title, kind)
        val requestCode = reminderRequestCode(reminderId, kind)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (reminderAtMillis <= System.currentTimeMillis()) {
            context.sendBroadcast(intent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                reminderAtMillis - 15 * 60 * 1000L,
                30 * 60 * 1000L,
                pendingIntent,
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderAtMillis,
                pendingIntent,
            )
        }
    }

    fun cancel(taskId: Long) {
        cancelAlarm(taskId, TaskReminderReceiver.KIND_TASK)
    }

    fun cancelMeeting(meetingId: Long) {
        cancelAlarm(meetingId, TaskReminderReceiver.KIND_MEETING)
    }

    private fun cancelAlarm(reminderId: Long, kind: String) {
        val intent = TaskReminderReceiver.createIntent(context, reminderId, "", kind)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderRequestCode(reminderId, kind),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun reminderRequestCode(reminderId: Long, kind: String): Int {
        val prefix = if (kind == TaskReminderReceiver.KIND_MEETING) 100_000_000 else 0
        return prefix + reminderId.hashCode()
    }
}
