package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertutoringapplication.data.model.Meeting
import com.example.peertutoringapplication.data.repository.MeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeetingViewModel(
    private val repository: MeetingRepository
) : ViewModel() {

    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun createMeeting(meeting: Meeting) {
        viewModelScope.launch {
            try {
                repository.createMeeting(meeting)
                _message.value = "Meeting proposed successfully."
            } catch (e: Exception) {
                _message.value = "Could not create meeting."
            }
        }
    }

    fun loadMeetings(userId: Int) {
        viewModelScope.launch {
            _meetings.value = repository.getMeetingsForUser(userId)
        }
    }

    fun updateMeetingStatus(meetingId: Int, status: String) {
        viewModelScope.launch {
            try {
                repository.updateMeetingStatus(meetingId, status)
                _message.value = "Meeting status updated successfully."
                loadMeetingsForRefresh()
            } catch (e: Exception) {
                _message.value = "Could not update meeting status."
            }
        }
    }

    private suspend fun loadMeetingsForRefresh() {
        // Meeting list will be refreshed by the screen.
    }
}
