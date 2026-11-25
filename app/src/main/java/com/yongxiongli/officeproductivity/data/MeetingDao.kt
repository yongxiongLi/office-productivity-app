package com.yongxiongli.officeproductivity.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY meetingAtMillis DESC")
    fun observeMeetings(): Flow<List<MeetingEntity>>

    @Insert
    suspend fun insert(meeting: MeetingEntity): Long

    @Query("DELETE FROM meetings WHERE id = :meetingId")
    suspend fun delete(meetingId: Long)
}
