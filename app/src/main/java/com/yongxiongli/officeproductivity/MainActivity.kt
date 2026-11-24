package com.yongxiongli.officeproductivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yongxiongli.officeproductivity.ui.DashboardScreen
import com.yongxiongli.officeproductivity.ui.theme.OfficeProductivityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfficeProductivityTheme {
                DashboardScreen()
            }
        }
    }
}
