package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "availability",
    foreignKeys = [
        ForeignKey(
            entity = TutorProfile::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Availability(
    @PrimaryKey(autoGenerate = true)
    val availabilityId: Int = 0,
    val userId: Int,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val teachingMode: String
)
