package com.example.peertutoringapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["receiverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("senderId"),
        Index("receiverId")
    ]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val content: String,
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
