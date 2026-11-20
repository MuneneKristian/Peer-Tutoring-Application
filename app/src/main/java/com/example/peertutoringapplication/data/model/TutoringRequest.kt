package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tutoring_requests",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["tuteeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["tutorId"],
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
        Index("tuteeId"),
        Index("tutorId"),
        Index("unitId")
    ]
)
data class TutoringRequest(
    @PrimaryKey(autoGenerate = true)
    val requestId: Int = 0,
    val tuteeId: Int,
    val tutorId: Int,
    val unitId: Int,
    val description: String,
    val status: String = "Pending",
    val createdAt: Long = System.currentTimeMillis()
)
