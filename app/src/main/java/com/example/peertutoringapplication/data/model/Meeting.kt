package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meetings",
    foreignKeys = [
        ForeignKey(
            entity = TutoringRequest::class,
            parentColumns = ["requestId"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["requestId"], unique = true)
    ]
)
data class Meeting(
    @PrimaryKey(autoGenerate = true)
    val meetingId: Int = 0,
    val requestId: Int,
    val meetingDateTime: String,
    val meetingMode: String,
    val status: String = "Proposed",
    val location: String = ""
)
