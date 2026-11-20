package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.peertutoringapplication.data.model.Availability

@Dao
interface AvailabilityDao {

    @Insert
    suspend fun insertAvailability(availability: Availability)

    @Query("SELECT * FROM availability WHERE userId = :userId")
    suspend fun getAvailabilityForTutor(userId: Int): List<Availability>

    @Update
    suspend fun updateAvailability(availability: Availability)

    @Delete
    suspend fun deleteAvailability(availability: Availability)

    @Query("DELETE FROM availability WHERE userId = :userId")
    suspend fun deleteAllAvailability(userId: Int)
}
