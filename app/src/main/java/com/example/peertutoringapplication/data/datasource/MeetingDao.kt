package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.peertutoringapplication.data.model.Meeting

@Dao
interface MeetingDao {

    @Insert
    suspend fun insertMeeting(meeting: Meeting)

    @Query("SELECT * FROM meetings WHERE meetingId = :meetingId")
    suspend fun getMeetingById(meetingId: Int): Meeting?

    @Query("""
        SELECT meetings.* FROM meetings
        INNER JOIN tutoring_requests
        ON meetings.requestId = tutoring_requests.requestId
        WHERE tutoring_requests.tuteeId = :userId
        OR tutoring_requests.tutorId = :userId
    """)
    suspend fun getMeetingsForUser(userId: Int): List<Meeting>

    @Query("UPDATE meetings SET status = :status WHERE meetingId = :meetingId")
    suspend fun updateMeetingStatus(meetingId: Int, status: String)

    @Update
    suspend fun updateMeeting(meeting: Meeting)

    @Delete
    suspend fun deleteMeeting(meeting: Meeting)
}
