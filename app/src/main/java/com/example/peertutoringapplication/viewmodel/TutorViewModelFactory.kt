package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.peertutoringapplication.data.repository.TutorRepository

class TutorViewModelFactory(
    private val repository: TutorRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TutorViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
