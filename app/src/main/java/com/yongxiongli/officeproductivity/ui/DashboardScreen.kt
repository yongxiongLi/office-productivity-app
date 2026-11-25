package com.yongxiongli.officeproductivity.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
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
import com.yongxiongli.officeproductivity.data.MeetingEntity
import com.yongxiongli.officeproductivity.data.TaskEntity
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

private enum class AppPage { HOME, TASKS, MEETINGS, REPORTS }
private enum class TaskFilter(val label: String) { TODAY("今天"), UPCOMING("即将到期"), COMPLETED("已完成"), ALL("全部任务") }
private val priorities = listOf("高", "中", "低")

@Composable
fun DashboardScreen(viewModel: TaskViewModel, onReminderPermissionNeeded: () -> Unit) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val meetings by viewModel.meetings.collectAsStateWithLifecycle(initialValue = emptyList())
    var page by remember { mutableStateOf(AppPage.HOME) }
    var filter by remember { mutableStateOf(TaskFilter.ALL) }
    var weeklyReport by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showMeetingDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            when (page) {
                AppPage.TASKS -> Button(onClick = { showTaskDialog = true }) { Text("新建任务 +") }
                AppPage.MEETINGS -> Button(onClick = { showMeetingDialog = true }) { Text("记录会议 +") }
                else -> Unit
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (page != AppPage.HOME) TextButton(onClick = { page = AppPage.HOME }) { Text("‹ 返回首页") }
            when (page) {
                AppPage.HOME -> HomeScreen(
                    onTasks = { filter = TaskFilter.ALL; page = AppPage.TASKS },
                    onMeetings = { page = AppPage.MEETINGS },
                    onReports = { weeklyReport = false; page = AppPage.REPORTS },
                )
                AppPage.TASKS -> TaskScreen(
                    tasks = tasks,
                    filter = filter,
                    onFilter = { filter = it },
                    onComplete = { task, completed -> viewModel.setCompleted(task, completed) },
                    onDelete = { viewModel.deleteTask(it) },
                )
                AppPage.MEETINGS -> MeetingsScreen(meetings) { viewModel.deleteMeeting(it) }
                AppPage.REPORTS -> ReportsScreen(tasks, weeklyReport) { weeklyReport = it }
            }
        }
    }

    if (showTaskDialog) TaskDialog(
        onDismiss = { showTaskDialog = false },
        onSave = { title, due, reminder, priority, assignee ->
            viewModel.addTask(title, due, reminder, priority, assignee)
            showTaskDialog = false
            if (reminder != null) onReminderPermissionNeeded()
        },
    )
    if (showMeetingDialog) MeetingDialog(
        onDismiss = { showMeetingDialog = false },
        onSave = { title, time, meetingReminder, participants, notes, actionTitle, owner, due, actionReminder, priority ->
            viewModel.addMeeting(title, time, meetingReminder, participants, notes, actionTitle, owner, due, actionReminder, priority)
            showMeetingDialog = false
            if (meetingReminder != null || (actionTitle.isNotBlank() && actionReminder != null)) onReminderPermissionNeeded()
        },
    )
}

@Composable
private fun HomeScreen(onTasks: () -> Unit, onMeetings: () -> Unit, onReports: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Flow办公", style = MaterialTheme.typography.headlineLarge)
        Text("请选择要使用的功能", style = MaterialTheme.typography.bodyLarge)
        HomeMenuItem("任务记录", "查看和管理任务、截止时间与提醒", onTasks)
        HomeMenuItem("会议记录", "记录会议内容，并关联行动待办", onMeetings)
        HomeMenuItem("日报 / 周报", "汇总每日或每周已完成的工作", onReports)
    }
}

@Composable
private fun HomeMenuItem(title: String, description: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TaskScreen(
    tasks: List<TaskEntity>,
    filter: TaskFilter,
    onFilter: (TaskFilter) -> Unit,
    onComplete: (TaskEntity, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Text("任务记录", style = MaterialTheme.typography.headlineMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TaskFilter.entries.forEach { item ->
            if (item == filter) Button(onClick = { onFilter(item) }) { Text(item.label) }
            else OutlinedButton(onClick = { onFilter(item) }) { Text(item.label) }
        }
    }
    val now = System.currentTimeMillis()
    val soon = now + 7L * 24 * 60 * 60 * 1000
    val visible = tasks.filter { task ->
        when (filter) {
            TaskFilter.TODAY -> !task.isCompleted && task.dueAtMillis?.let { isSameDay(it, now) } == true
            TaskFilter.UPCOMING -> !task.isCompleted && task.dueAtMillis?.let { it >= now && it <= soon } == true
            TaskFilter.COMPLETED -> task.isCompleted
            TaskFilter.ALL -> true
        }
    }
    if (visible.isEmpty()) Text("暂无${filter.label}任务")
    else LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(visible, key = { it.id }) { task -> TaskCard(task, { onComplete(task, it) }, { onDelete(task.id) }) }
    }
}

@Composable
private fun TaskCard(task: TaskEntity, onComplete: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onComplete)
            Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text("${priorityLabel(task.priority)}优先级${task.assignee.takeIf { it.isNotBlank() }?.let { " · 负责人：$it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                task.dueAtMillis?.let { Text("截止：${formatDateTime(it)}", style = MaterialTheme.typography.bodySmall) }
                task.reminderAtMillis?.let { Text("提醒：${formatDateTime(it)}", style = MaterialTheme.typography.bodySmall) }
            }
            TextButton(onClick = onDelete) { Text("删除") }
        }
    }
}

@Composable
private fun MeetingsScreen(meetings: List<MeetingEntity>, onDelete: (Long) -> Unit) {
    Text("会议记录", style = MaterialTheme.typography.headlineMedium)
    Text("记录讨论内容，并把行动项关联到任务。")
    if (meetings.isEmpty()) Text("还没有会议记录。点击“记录会议 +”开始添加。")
    else LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(meetings, key = { it.id }) { meeting ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(meeting.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { onDelete(meeting.id) }) { Text("删除") }
                    }
                    Text("会议时间：${formatDateTime(meeting.meetingAtMillis)}", style = MaterialTheme.typography.bodySmall)
                    meeting.reminderAtMillis?.let { Text("提醒时间：${formatDateTime(it)}", style = MaterialTheme.typography.bodySmall) }
                    if (meeting.participants.isNotBlank()) Text("参与人：${meeting.participants}")
                    if (meeting.notes.isNotBlank()) Text(meeting.notes)
                }
            }
        }
    }
}

@Composable
private fun ReportsScreen(tasks: List<TaskEntity>, weekly: Boolean, onWeeklyChange: (Boolean) -> Unit) {
    val startCalendar = Calendar.getInstance().apply {
        if (weekly) set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = startCalendar.timeInMillis
    val completed = tasks.filter { it.isCompleted && (it.completedAtMillis ?: 0L) >= start }
    Text("日报 / 周报", style = MaterialTheme.typography.headlineMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!weekly) Button(onClick = { onWeeklyChange(false) }) { Text("日报") }
        else OutlinedButton(onClick = { onWeeklyChange(false) }) { Text("日报") }
        if (weekly) Button(onClick = { onWeeklyChange(true) }) { Text("周报") }
        else OutlinedButton(onClick = { onWeeklyChange(true) }) { Text("周报") }
    }
    Text(if (weekly) "本周完成 ${completed.size} 项" else "今日完成 ${completed.size} 项", style = MaterialTheme.typography.titleLarge)
    if (completed.isEmpty()) Text("该时间段还没有已完成任务。")
    else LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(completed, key = { it.id }) { TaskCard(it, {}, {}) }
    }
    Text("未完成待办：${tasks.count { !it.isCompleted }}")
}

@Composable
private fun TaskDialog(onDismiss: () -> Unit, onSave: (String, Long?, Long?, String, String) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var due by remember { mutableLongStateOf(0L) }
    var reminder by remember { mutableLongStateOf(0L) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建任务") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(title, { title = it }, label = { Text("任务标题") }, singleLine = true) }
                item { OutlinedTextField(assignee, { assignee = it }, label = { Text("负责人（可选）") }, singleLine = true) }
                item { Text("优先级") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        priorities.forEach { label ->
                            val code = priorityCode(label)
                            if (priority == code) Button(onClick = { priority = code }) { Text(label) }
                            else OutlinedButton(onClick = { priority = code }) { Text(label) }
                        }
                    }
                }
                item { DateTimeButton(context, due.takeIf { it > 0L }, "设置截止时间") { due = it } }
                item { if (due > 0L) TextButton(onClick = { due = 0L }) { Text("清除截止时间") } }
                item { DateTimeButton(context, reminder.takeIf { it > 0L }, "设置提醒时间（可选）") { reminder = it } }
                item { if (reminder > 0L) TextButton(onClick = { reminder = 0L }) { Text("清除提醒时间") } }
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), due.takeIf { it > 0L }, reminder.takeIf { it > 0L }, priority, assignee.trim()) }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun MeetingDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long, Long?, String, String, String, String, Long?, Long?, String) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var meetingTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var meetingReminder by remember { mutableLongStateOf(0L) }
    var actionDue by remember { mutableLongStateOf(0L) }
    var actionReminder by remember { mutableLongStateOf(0L) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录会议") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(title, { title = it }, label = { Text("会议标题") }, singleLine = true) }
                item { DateTimeButton(context, meetingTime, "选择会议时间") { meetingTime = it } }
                item { DateTimeButton(context, meetingReminder.takeIf { it > 0L }, "设置会议提醒时间（可选）") { meetingReminder = it } }
                item { if (meetingReminder > 0L) TextButton(onClick = { meetingReminder = 0L }) { Text("清除会议提醒") } }
                item { OutlinedTextField(participants, { participants = it }, label = { Text("参与人") }, singleLine = true) }
                item { OutlinedTextField(notes, { notes = it }, label = { Text("会议内容") }, minLines = 3) }
                item { Text("行动项（可选，保存后会关联创建任务）", style = MaterialTheme.typography.titleSmall) }
                item { OutlinedTextField(action, { action = it }, label = { Text("待办内容") }, singleLine = true) }
                item { OutlinedTextField(owner, { owner = it }, label = { Text("负责人") }, singleLine = true) }
                item { DateTimeButton(context, actionDue.takeIf { it > 0L }, "设置行动项截止时间") { actionDue = it } }
                item { if (actionDue > 0L) TextButton(onClick = { actionDue = 0L }) { Text("清除行动项截止时间") } }
                item { DateTimeButton(context, actionReminder.takeIf { it > 0L }, "设置行动项提醒时间（可选）") { actionReminder = it } }
                item { if (actionReminder > 0L) TextButton(onClick = { actionReminder = 0L }) { Text("清除行动项提醒") } }
                item { Text("行动项优先级") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        priorities.forEach { label ->
                            val code = priorityCode(label)
                            if (priority == code) Button(onClick = { priority = code }) { Text(label) }
                            else OutlinedButton(onClick = { priority = code }) { Text(label) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                onSave(
                    title.trim(), meetingTime, meetingReminder.takeIf { it > 0L }, participants, notes,
                    action, owner, actionDue.takeIf { it > 0L }, actionReminder.takeIf { it > 0L }, priority,
                )
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun DateTimeButton(context: android.content.Context, timestamp: Long?, label: String, onSelected: (Long) -> Unit) {
    OutlinedButton(onClick = {
        val current = Calendar.getInstance().apply { if (timestamp != null) timeInMillis = timestamp }
        DatePickerDialog(context, { _, year, month, day ->
            val selected = Calendar.getInstance().apply {
                if (timestamp != null) timeInMillis = timestamp
                set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
            }
            TimePickerDialog(context, { _, hour, minute ->
                selected.set(Calendar.HOUR_OF_DAY, hour); selected.set(Calendar.MINUTE, minute)
                selected.set(Calendar.SECOND, 0); selected.set(Calendar.MILLISECOND, 0)
                onSelected(selected.timeInMillis)
            }, selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE), true).show()
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show()
    }) { Text(timestamp?.let { formatDateTime(it) } ?: label) }
}

private fun isSameDay(first: Long, second: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = first }
    val b = Calendar.getInstance().apply { timeInMillis = second }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun priorityCode(label: String) = when (label) { "高" -> "HIGH"; "低" -> "LOW"; else -> "MEDIUM" }
private fun priorityLabel(code: String) = when (code) { "HIGH" -> "高"; "LOW" -> "低"; else -> "中" }
private fun formatDateTime(timestamp: Long): String = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
