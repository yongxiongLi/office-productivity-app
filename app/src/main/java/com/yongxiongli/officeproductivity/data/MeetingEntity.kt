package com.yongxiongli.officeproductivity.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val meetingAtMillis: Long,
    val participants: String,
    val notes: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
