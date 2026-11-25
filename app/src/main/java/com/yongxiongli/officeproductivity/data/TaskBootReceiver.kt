package com.yongxiongli.officeproductivity.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Existing exact alarms cannot be reconstructed here without coroutine/DAO access.
        // This receiver is reserved for an application-level reschedule implementation.
    }
}
