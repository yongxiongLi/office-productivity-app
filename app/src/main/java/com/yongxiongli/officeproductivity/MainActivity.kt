package com.yongxiongli.officeproductivity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.yongxiongli.officeproductivity.data.TaskDatabase
import com.yongxiongli.officeproductivity.data.TaskRepository
import com.yongxiongli.officeproductivity.ui.DashboardScreen
import com.yongxiongli.officeproductivity.ui.TaskViewModel
import com.yongxiongli.officeproductivity.ui.theme.OfficeProductivityTheme

class MainActivity : ComponentActivity() {
    private val requestNotificationsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = TaskDatabase.getInstance(applicationContext)
        val viewModel = ViewModelProvider(
            this,
            TaskViewModel.Factory(TaskRepository(database.taskDao(), database.meetingDao(), applicationContext)),
        )[TaskViewModel::class.java]
        setContent {
            OfficeProductivityTheme {
                DashboardScreen(viewModel, ::requestNotificationPermissionIfNeeded)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
