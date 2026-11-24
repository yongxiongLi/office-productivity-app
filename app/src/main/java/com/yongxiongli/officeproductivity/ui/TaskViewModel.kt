package com.yongxiongli.officeproductivity.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yongxiongli.officeproductivity.data.TaskEntity
import com.yongxiongli.officeproductivity.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: Flow<List<TaskEntity>> = repository.tasks

    fun addTask(title: String, dueAtMillis: Long?) {
        viewModelScope.launch { repository.addTask(title, dueAtMillis) }
    }

    fun setCompleted(task: TaskEntity, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(task, completed) }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch { repository.deleteTask(taskId) }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TaskViewModel::class.java))
            return TaskViewModel(repository) as T
        }
    }
}
