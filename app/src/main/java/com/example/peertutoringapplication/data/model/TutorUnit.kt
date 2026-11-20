package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tutor_units",
    primaryKeys = ["userId", "unitId"],
    foreignKeys = [
        ForeignKey(
            entity = TutorProfile::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UniversityUnit::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("unitId")
    ]
)
data class TutorUnit(
    val userId: Int,
    val unitId: Int
)
