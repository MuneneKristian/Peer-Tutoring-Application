package com.example.peertutoringapplication.ui.messages

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.model.Message
import com.example.peertutoringapplication.databinding.ActivityConversationBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.MessageViewModel
import com.example.peertutoringapplication.viewmodel.MessageViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var sessionManager: SessionManager

    private var currentUserId = 0
    private var otherUserId = 0

    private val messages = mutableListOf<Message>()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        currentUserId = sessionManager.getUserId()
        otherUserId = intent.getIntExtra("OTHER_USER_ID", -1)

        if (otherUserId == -1) {
            Toast.makeText(this, "Invalid conversation.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val factory = MessageViewModelFactory(application)
        messageViewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]

        binding.tvConversationTitle.text = "Conversation with User #$otherUserId"

        setupRecyclerView()
        setupSendButton()
        loadMessages()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, currentUserId)

        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        binding.rvMessages.adapter = messageAdapter
    }

    private fun setupSendButton() {
        binding.btnSendMessage.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(this, "Enter a message first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = Message(
                senderId = currentUserId,
                receiverId = otherUserId,
                content = content,
                sentAt = System.currentTimeMillis(),
                isRead = false
            )

            messageViewModel.sendMessage(message)

            binding.etMessage.text.clear()
            loadMessages()
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            try {
                val result = messageViewModel.getConversation(
                    currentUserId,
                    otherUserId
                )

                messages.clear()
                messages.addAll(result)

                messageAdapter.notifyDataSetChanged()

                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@ConversationActivity,
                    "Could not load messages: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private class MessageAdapter(
        private val messages: List<Message>,
        private val currentUserId: Int
    ) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        class MessageViewHolder(val textView: TextView) :
            RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MessageViewHolder {
            val textView = TextView(parent.context).apply {
                textSize = 15f
                setPadding(16, 12, 16, 12)
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 6, 8, 6)
                }
            }

            return MessageViewHolder(textView)
        }

        override fun onBindViewHolder(
            holder: MessageViewHolder,
            position: Int
        ) {
            val message = messages[position]
            val isMine = message.senderId == currentUserId

            holder.textView.text =
                "${if (isMine) "You" else "Other user"}: ${message.content}\n" +
                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                            .format(Date(message.sentAt))

            holder.textView.gravity =
                if (isMine) Gravity.END else Gravity.START

            holder.textView.setBackgroundColor(
                if (isMine) 0xFFDCEBFF.toInt()
                else 0xFFE8EAED.toInt()
            )
        }

        override fun getItemCount(): Int = messages.size
    }
}