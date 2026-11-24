package com.yongxiongli.officeproductivity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProductivityColorScheme = lightColorScheme(
    primary = Color(0xFF315C9B),
    secondary = Color(0xFF526B8A),
    tertiary = Color(0xFF6B5A8E),
)

@Composable
fun OfficeProductivityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ProductivityColorScheme,
        content = content,
    )
}
