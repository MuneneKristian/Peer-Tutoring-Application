package com.example.peertutoringapplication.data.repository

import com.example.peertutoringapplication.data.datasource.TutorProfileDao
import com.example.peertutoringapplication.data.datasource.UniversityUnitDao
import com.example.peertutoringapplication.data.datasource.TutorUnitDao
import com.example.peertutoringapplication.data.datasource.AvailabilityDao
import com.example.peertutoringapplication.data.model.TutorProfile
import com.example.peertutoringapplication.data.model.UniversityUnit
import com.example.peertutoringapplication.data.model.TutorUnit
import com.example.peertutoringapplication.data.model.Availability

class TutorRepository(
    private val tutorProfileDao: TutorProfileDao,
    private val universityUnitDao: UniversityUnitDao,
    private val tutorUnitDao: TutorUnitDao,
    private val availabilityDao: AvailabilityDao
) {

    suspend fun saveTutorProfile(profile: TutorProfile) =
        tutorProfileDao.insertTutorProfile(profile)

    suspend fun getTutorProfile(userId: Int) =
        tutorProfileDao.getTutorProfile(userId)

    suspend fun getAllTutorProfiles() =
        tutorProfileDao.getAllTutorProfiles()

    suspend fun updateTutorProfile(profile: TutorProfile) =
        tutorProfileDao.updateTutorProfile(profile)

    suspend fun addUnit(unit: UniversityUnit) =
        universityUnitDao.insertUnit(unit)

    suspend fun getAllUnits() =
        universityUnitDao.getAllUnits()

    suspend fun assignUnit(tutorUnit: TutorUnit) =
        tutorUnitDao.assignUnitToTutor(tutorUnit)

    suspend fun getUnitsForTutor(userId: Int) =
        tutorUnitDao.getUnitsForTutor(userId)

    suspend fun getTutorsForUnit(unitId: Int) =
        tutorUnitDao.getTutorsForUnit(unitId)

    suspend fun removeUnit(userId: Int, unitId: Int) =
        tutorUnitDao.removeUnitFromTutor(userId, unitId)

    suspend fun addAvailability(availability: Availability) =
        availabilityDao.insertAvailability(availability)

    suspend fun getAvailability(userId: Int) =
        availabilityDao.getAvailabilityForTutor(userId)

    suspend fun updateAvailability(availability: Availability) =
        availabilityDao.updateAvailability(availability)

    suspend fun deleteAvailability(availability: Availability) =
        availabilityDao.deleteAvailability(availability)
}
