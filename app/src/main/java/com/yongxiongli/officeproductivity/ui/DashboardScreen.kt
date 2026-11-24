package com.yongxiongli.officeproductivity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yongxiongli.officeproductivity.R

@Composable
fun DashboardScreen() {
    Scaffold { contentPadding ->
        DashboardContent(contentPadding)
    }
}

@Composable
private fun DashboardContent(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyLarge,
        )
        FeatureCard(
            title = stringResource(R.string.feature_tasks_title),
            description = stringResource(R.string.feature_tasks_description),
        )
        FeatureCard(
            title = stringResource(R.string.feature_meetings_title),
            description = stringResource(R.string.feature_meetings_description),
        )
        FeatureCard(
            title = stringResource(R.string.feature_reports_title),
            description = stringResource(R.string.feature_reports_description),
        )
    }
}

@Composable
private fun FeatureCard(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
