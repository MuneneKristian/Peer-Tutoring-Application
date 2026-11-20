package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.peertutoringapplication.data.model.Message

@Dao
interface MessageDao {

    @Insert
    suspend fun sendMessage(message: Message)

    @Query("""
        SELECT * FROM messages
        WHERE (senderId = :userId AND receiverId = :otherUserId)
        OR (senderId = :otherUserId AND receiverId = :userId)
        ORDER BY sentAt ASC
    """)
    suspend fun getConversation(
        userId: Int,
        otherUserId: Int
    ): List<Message>

    @Query("""
        SELECT * FROM messages
        WHERE receiverId = :userId
        ORDER BY sentAt DESC
    """)
    suspend fun getReceivedMessages(userId: Int): List<Message>

    @Query("""
        UPDATE messages SET isRead = 1
        WHERE messageId = :messageId
    """)
    suspend fun markAsRead(messageId: Int)

    @Delete
    suspend fun deleteMessage(message: Message)
}
