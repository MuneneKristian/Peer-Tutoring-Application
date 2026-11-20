package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "units",
    indices = [Index(value = ["unitCode"], unique = true)]
)
data class UniversityUnit(
    @PrimaryKey(autoGenerate = true)
    val unitId: Int = 0,
    val unitCode: String,
    val unitName: String,
    val description: String = ""
)
