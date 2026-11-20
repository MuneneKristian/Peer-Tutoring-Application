package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.peertutoringapplication.data.model.TutorUnit
import com.example.peertutoringapplication.data.model.UniversityUnit

@Dao
interface TutorUnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignUnitToTutor(tutorUnit: TutorUnit)

    @Query("""
        SELECT units.* FROM units
        INNER JOIN tutor_units
        ON units.unitId = tutor_units.unitId
        WHERE tutor_units.userId = :userId
    """)
    suspend fun getUnitsForTutor(userId: Int): List<UniversityUnit>

    @Query("""
        SELECT tutor_units.userId FROM tutor_units
        WHERE tutor_units.unitId = :unitId
    """)
    suspend fun getTutorsForUnit(unitId: Int): List<Int>

    @Query("""
        DELETE FROM tutor_units
        WHERE userId = :userId AND unitId = :unitId
    """)
    suspend fun removeUnitFromTutor(userId: Int, unitId: Int)

    @Query("DELETE FROM tutor_units WHERE userId = :userId")
    suspend fun removeAllUnitsFromTutor(userId: Int)
}
