package com.example.peertutoringapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertutoringapplication.data.model.Message
import com.example.peertutoringapplication.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _messageStatus = MutableStateFlow<String?>(null)
    val messageStatus: StateFlow<String?> = _messageStatus

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            try {
                repository.sendMessage(message)
                _messageStatus.value = "Message sent successfully."
                loadConversation(message.senderId, message.receiverId)
            } catch (e: Exception) {
                _messageStatus.value = "Could not send message."
            }
        }
    }

    fun loadConversation(userId: Int, otherUserId: Int) {
        viewModelScope.launch {
            _messages.value = repository.getConversation(userId, otherUserId)
        }
    }

    fun loadMessagesForUser(userId: Int) {
        viewModelScope.launch {
            _messages.value = repository.getMessagesForUser(userId)
        }
    }

    fun loadReceivedMessages(userId: Int) {
        viewModelScope.launch {
            _messages.value = repository.getReceivedMessages(userId)
        }
    }

    fun markAsRead(messageId: Int) {
        viewModelScope.launch {
            try {
                repository.markAsRead(messageId)
            } catch (e: Exception) {
                _messageStatus.value = "Could not update message status."
            }
        }
    }
}
