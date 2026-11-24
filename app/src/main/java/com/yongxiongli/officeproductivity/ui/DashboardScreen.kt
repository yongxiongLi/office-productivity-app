package com.yongxiongli.officeproductivity.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yongxiongli.officeproductivity.data.TaskEntity
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    onReminderPermissionNeeded: () -> Unit,
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Button(onClick = { showAddTaskDialog = true }) {
                Text("新建任务")
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("任务与提醒", style = MaterialTheme.typography.headlineMedium)
            Text("任务保存在本机；设置截止时间后会安排提醒。")

            if (tasks.isEmpty()) {
                Text(
                    text = "还没有任务。点击“新建任务”添加第一条。",
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onCompletedChange = { completed ->
                                viewModel.setCompleted(task, completed)
                            },
                            onDelete = { viewModel.deleteTask(task.id) },
                        )
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onSave = { title, dueAt ->
                viewModel.addTask(title, dueAt)
                showAddTaskDialog = false
                if (dueAt != null) onReminderPermissionNeeded()
            },
        )
    }
}

@Composable
private fun TaskRow(
    task: TaskEntity,
    onCompletedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onCompletedChange)
            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                task.dueAtMillis?.let { dueAt ->
                    Text(
                        text = "截止：${formatDateTime(dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            TextButton(onClick = onDelete) { Text("删除") }
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long?) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var dueAtMillis by remember { mutableLongStateOf(0L) }
    val dueAt = dueAtMillis.takeIf { it > 0L }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建任务") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务内容") },
                    singleLine = true,
                )
                OutlinedButton(
                    onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selected = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        selected.set(Calendar.HOUR_OF_DAY, hour)
                                        selected.set(Calendar.MINUTE, minute)
                                        dueAtMillis = selected.timeInMillis
                                    },
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    true,
                                ).show()
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                ) {
                    Text(dueAt?.let { "提醒时间：${formatDateTime(it)}" } ?: "设置截止时间和提醒")
                }
                if (dueAt != null) {
                    TextButton(onClick = { dueAtMillis = 0L }) { Text("清除提醒时间") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), dueAt) },
                enabled = title.isNotBlank(),
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

private fun formatDateTime(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
