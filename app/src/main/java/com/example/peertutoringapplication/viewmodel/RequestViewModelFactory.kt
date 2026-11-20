package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.peertutoringapplication.data.repository.RequestRepository

class RequestViewModelFactory(
    private val repository: RequestRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RequestViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
