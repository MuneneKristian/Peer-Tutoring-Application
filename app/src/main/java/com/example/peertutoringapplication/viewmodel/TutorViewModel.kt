package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertutoringapplication.data.model.Availability
import com.example.peertutoringapplication.data.model.TutorProfile
import com.example.peertutoringapplication.data.model.TutorUnit
import com.example.peertutoringapplication.data.model.UniversityUnit
import com.example.peertutoringapplication.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TutorViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    private val _tutorProfile = MutableStateFlow<TutorProfile?>(null)
    val tutorProfile: StateFlow<TutorProfile?> = _tutorProfile

    private val _units = MutableStateFlow<List<UniversityUnit>>(emptyList())
    val units: StateFlow<List<UniversityUnit>> = _units

    private val _tutorUnits = MutableStateFlow<List<UniversityUnit>>(emptyList())
    val tutorUnits: StateFlow<List<UniversityUnit>> = _tutorUnits

    private val _availability = MutableStateFlow<List<Availability>>(emptyList())
    val availability: StateFlow<List<Availability>> = _availability

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun saveTutorProfile(profile: TutorProfile) {
        viewModelScope.launch {
            try {
                repository.saveTutorProfile(profile)
                _tutorProfile.value = profile
                _message.value = "Tutor profile saved successfully."
            } catch (e: Exception) {
                _message.value = "Could not save tutor profile: ${e.message}"
            }
        }
    }

    fun updateTutorProfile(profile: TutorProfile) {
        viewModelScope.launch {
            try {
                repository.updateTutorProfile(profile)
                _tutorProfile.value = profile
                _message.value = "Tutor profile updated successfully."
            } catch (e: Exception) {
                _message.value = "Could not update tutor profile: ${e.message}"
            }
        }
    }

    fun loadTutorProfile(userId: Int) {
        viewModelScope.launch {
            _tutorProfile.value = repository.getTutorProfile(userId)
        }
    }

    fun loadUnits() {
        viewModelScope.launch {
            _units.value = repository.getAllUnits()
        }
    }

    fun assignUnit(userId: Int, unitId: Int) {
        viewModelScope.launch {
            try {
                repository.assignUnit(TutorUnit(userId, unitId))

                _tutorUnits.value = repository.getUnitsForTutor(userId)

                _message.value = "Unit assigned successfully."
            } catch (e: Exception) {
                _message.value = "Could not assign unit: ${e.message}"
            }
        }
    }

    fun removeUnit(userId: Int, unitId: Int) {
        viewModelScope.launch {
            try {
                repository.removeUnit(userId, unitId)

                _tutorUnits.value = repository.getUnitsForTutor(userId)

                _message.value = "Teaching unit removed successfully."
            } catch (e: Exception) {
                _message.value = "Could not remove teaching unit: ${e.message}"
            }
        }
    }

    fun loadTutorUnits(userId: Int) {
        viewModelScope.launch {
            _tutorUnits.value = repository.getUnitsForTutor(userId)
        }
    }

    fun addAvailability(availability: Availability) {
        viewModelScope.launch {
            try {
                repository.addAvailability(availability)
                loadAvailability(availability.userId)
                _message.value = "Availability added successfully."
            } catch (e: Exception) {
                _message.value = "Could not add availability."
            }
        }
    }

    fun loadAvailability(userId: Int) {
        viewModelScope.launch {
            _availability.value = repository.getAvailability(userId)
        }
    }
}
