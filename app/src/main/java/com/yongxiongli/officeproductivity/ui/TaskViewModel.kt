package com.yongxiongli.officeproductivity.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yongxiongli.officeproductivity.data.MeetingEntity
import com.yongxiongli.officeproductivity.data.TaskEntity
import com.yongxiongli.officeproductivity.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: Flow<List<TaskEntity>> = repository.tasks
    val meetings: Flow<List<MeetingEntity>> = repository.meetings

    fun addTask(title: String, dueAtMillis: Long?, reminderAtMillis: Long?, priority: String, assignee: String) {
        viewModelScope.launch { repository.addTask(title, dueAtMillis, reminderAtMillis, priority, assignee) }
    }

    fun addMeeting(
        title: String,
        meetingAtMillis: Long,
        meetingReminderAtMillis: Long?,
        participants: String,
        notes: String,
        actionTitle: String,
        actionAssignee: String,
        actionDueAtMillis: Long?,
        actionReminderAtMillis: Long?,
        actionPriority: String,
    ) {
        viewModelScope.launch {
            repository.addMeeting(
                title, meetingAtMillis, meetingReminderAtMillis, participants, notes,
                actionTitle, actionAssignee, actionDueAtMillis, actionReminderAtMillis, actionPriority,
            )
        }
    }

    fun setCompleted(task: TaskEntity, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(task, completed) }
    }

    fun deleteTask(taskId: Long) { viewModelScope.launch { repository.deleteTask(taskId) } }
    fun deleteMeeting(meetingId: Long) { viewModelScope.launch { repository.deleteMeeting(meetingId) } }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TaskViewModel::class.java))
            return TaskViewModel(repository) as T
        }
    }
}
