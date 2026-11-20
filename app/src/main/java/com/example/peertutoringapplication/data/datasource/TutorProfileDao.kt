package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.peertutoringapplication.data.model.TutorProfile

@Dao
interface TutorProfileDao {

    @Insert
    suspend fun insertTutorProfile(profile: TutorProfile)

    @Query("SELECT * FROM tutor_profiles WHERE userId = :userId")
    suspend fun getTutorProfile(userId: Int): TutorProfile?

    @Query("SELECT * FROM tutor_profiles")
    suspend fun getAllTutorProfiles(): List<TutorProfile>

    @Update
    suspend fun updateTutorProfile(profile: TutorProfile)

    @Delete
    suspend fun deleteTutorProfile(profile: TutorProfile)
}
