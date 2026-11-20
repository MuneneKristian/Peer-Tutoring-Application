package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.peertutoringapplication.data.repository.MeetingRepository

class MeetingViewModelFactory(
    private val repository: MeetingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeetingViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
