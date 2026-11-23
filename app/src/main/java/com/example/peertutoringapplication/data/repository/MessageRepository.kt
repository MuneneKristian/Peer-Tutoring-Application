package com.example.peertutoringapplication.data.repository

import com.example.peertutoringapplication.data.datasource.MessageDao
import com.example.peertutoringapplication.data.model.Message

class MessageRepository(private val messageDao: MessageDao) {

    suspend fun sendMessage(message: Message) =
        messageDao.sendMessage(message)

    suspend fun getConversation(userId: Int, otherUserId: Int) =
        messageDao.getConversation(userId, otherUserId)

    suspend fun getReceivedMessages(userId: Int) =
        messageDao.getReceivedMessages(userId)

    suspend fun getMessagesForUser(userId: Int) =
        messageDao.getMessagesForUser(userId)

    suspend fun markAsRead(messageId: Int) =
        messageDao.markAsRead(messageId)

    suspend fun deleteMessage(message: Message) =
        messageDao.deleteMessage(message)
}
