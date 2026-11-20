package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.peertutoringapplication.data.model.UniversityUnit

@Dao
interface UniversityUnitDao {

    @Insert
    suspend fun insertUnit(unit: UniversityUnit)

    @Query("SELECT * FROM units")
    suspend fun getAllUnits(): List<UniversityUnit>

    @Query("SELECT * FROM units WHERE unitId = :unitId")
    suspend fun getUnitById(unitId: Int): UniversityUnit?

    @Query("SELECT * FROM units WHERE unitCode = :unitCode")
    suspend fun getUnitByCode(unitCode: String): UniversityUnit?

    @Update
    suspend fun updateUnit(unit: UniversityUnit)

    @Delete
    suspend fun deleteUnit(unit: UniversityUnit)
}
