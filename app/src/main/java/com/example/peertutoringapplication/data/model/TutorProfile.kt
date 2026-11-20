package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tutor_profiles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TutorProfile(
    @PrimaryKey
    val userId: Int,
    val bio: String = "",
    val course: String = "",
    val yearOfStudy: String = "",
    val teachingMode: String = "Both"
)
