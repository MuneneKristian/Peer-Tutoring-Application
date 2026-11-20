package com.example.peertutoringapplication.data.repository

import com.example.peertutoringapplication.data.datasource.MeetingDao
import com.example.peertutoringapplication.data.model.Meeting

class MeetingRepository(
    private val meetingDao: MeetingDao
) {

    suspend fun createMeeting(meeting: Meeting) =
        meetingDao.insertMeeting(meeting)

    suspend fun getMeetingById(meetingId: Int) =
        meetingDao.getMeetingById(meetingId)

    suspend fun getMeetingsForUser(userId: Int) =
        meetingDao.getMeetingsForUser(userId)

    suspend fun updateMeetingStatus(
        meetingId: Int,
        status: String
    ) = meetingDao.updateMeetingStatus(meetingId, status)

    suspend fun updateMeeting(meeting: Meeting) =
        meetingDao.updateMeeting(meeting)

    suspend fun deleteMeeting(meeting: Meeting) =
        meetingDao.deleteMeeting(meeting)
}
