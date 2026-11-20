package com.example.peertutoringapplication.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.peertutoringapplication.data.model.User
import com.example.peertutoringapplication.data.model.TutorProfile
import com.example.peertutoringapplication.data.model.UniversityUnit
import com.example.peertutoringapplication.data.model.TutorUnit
import com.example.peertutoringapplication.data.model.Availability
import com.example.peertutoringapplication.data.model.TutoringRequest
import com.example.peertutoringapplication.data.model.Meeting
import com.example.peertutoringapplication.data.model.Message

@Database(
    entities = [
        User::class,
        TutorProfile::class,
        UniversityUnit::class,
        TutorUnit::class,
        Availability::class,
        TutoringRequest::class,
        Meeting::class,
        Message::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun tutorProfileDao(): TutorProfileDao

    abstract fun universityUnitDao(): UniversityUnitDao

    abstract fun tutorUnitDao(): TutorUnitDao

    abstract fun availabilityDao(): AvailabilityDao

    abstract fun tutoringRequestDao(): TutoringRequestDao

    abstract fun meetingDao(): MeetingDao

    abstract fun messageDao(): MessageDao
}
