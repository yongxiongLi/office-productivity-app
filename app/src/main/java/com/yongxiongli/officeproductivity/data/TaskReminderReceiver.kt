package com.yongxiongli.officeproductivity.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val kind = intent.getStringExtra(EXTRA_KIND) ?: KIND_TASK
        if (title.isBlank()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val alertIntent = Intent(context, ReminderAlertService::class.java)
            .setAction(ReminderAlertService.ACTION_START)
            .putExtra(ReminderAlertService.EXTRA_TITLE, title)
            .putExtra(ReminderAlertService.EXTRA_KIND, kind)

        try {
            ContextCompat.startForegroundService(context, alertIntent)
        } catch (_: Exception) {
            context.startService(alertIntent)
        }
    }

    companion object {
        const val KIND_TASK = "task"
        const val KIND_MEETING = "meeting"
        private const val EXTRA_REMINDER_ID = "reminder_id"
        private const val EXTRA_TITLE = "reminder_title"
        private const val EXTRA_KIND = "reminder_kind"

        fun createIntent(context: Context, reminderId: Long, title: String, kind: String): Intent {
            return Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_KIND, kind)
            }
        }
    }
}
