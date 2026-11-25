package com.yongxiongli.officeproductivity.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yongxiongli.officeproductivity.MainActivity
import com.yongxiongli.officeproductivity.R

class ReminderAlertService : Service() {
    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlert()
            return START_NOT_STICKY
        }

        val title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { getString(R.string.task_reminder_title) }
        val kind = intent?.getStringExtra(EXTRA_KIND).orEmpty()
        val notificationTitle = if (kind == TaskReminderWorker.KIND_MEETING) "会议提醒" else "任务提醒"

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(notificationTitle, title))
        startLoopingSound()
        return START_STICKY
    }

    private fun buildNotification(title: String, text: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            NOTIFICATION_ID + 1,
            Intent(this, ReminderAlertService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "停止提醒", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "持续提醒", NotificationManager.IMPORTANCE_LOW).apply {
                description = "提醒响铃期间显示的常驻通知"
                setSound(null, null)
                enableVibration(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startLoopingSound() {
        if (player?.isPlaying == true) return
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: return
        runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                isLooping = true
                setDataSource(this@ReminderAlertService, uri)
                setOnErrorListener { mediaPlayer, _, _ ->
                    mediaPlayer.release()
                    if (player === mediaPlayer) player = null
                    true
                }
                prepare()
                start()
                player = this
            }
        }
    }

    private fun stopAlert() {
        player?.runCatching { stop() }
        player?.release()
        player = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else @Suppress("DEPRECATION") stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        player?.runCatching { stop() }
        player?.release()
        player = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.yongxiongli.officeproductivity.action.START_REMINDER_ALERT"
        const val ACTION_STOP = "com.yongxiongli.officeproductivity.action.STOP_REMINDER_ALERT"
        const val EXTRA_TITLE = "reminder_alert_title"
        const val EXTRA_KIND = "reminder_alert_kind"
        private const val CHANNEL_ID = "ongoing_reminder_alert"
        private const val NOTIFICATION_ID = 9201
    }
}
