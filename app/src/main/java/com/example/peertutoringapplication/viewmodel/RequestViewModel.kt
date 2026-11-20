package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertutoringapplication.data.model.TutoringRequest
import com.example.peertutoringapplication.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RequestViewModel(
    private val repository: RequestRepository
) : ViewModel() {

    private val _requests = MutableStateFlow<List<TutoringRequest>>(emptyList())
    val requests: StateFlow<List<TutoringRequest>> = _requests

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun submitRequest(request: TutoringRequest) {
        viewModelScope.launch {
            try {
                repository.submitRequest(request)
                _message.value = "Tutoring request submitted successfully."
            } catch (e: Exception) {
                _message.value = "Could not submit tutoring request."
            }
        }
    }

    fun loadTuteeRequests(tuteeId: Int) {
        viewModelScope.launch {
            _requests.value = repository.getRequestsByTutee(tuteeId)
        }
    }

    fun loadTutorRequests(tutorId: Int) {
        viewModelScope.launch {
            _requests.value = repository.getRequestsByTutor(tutorId)
        }
    }

    fun updateRequestStatus(requestId: Int, status: String) {
        viewModelScope.launch {
            try {
                repository.updateRequestStatus(requestId, status)
                _message.value = "Request status updated successfully."
            } catch (e: Exception) {
                _message.value = "Could not update request status."
            }
        }
    }
}
