package com.example.peertutoringapplication.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.Message
import com.example.peertutoringapplication.data.repository.MessageRepository
import com.example.peertutoringapplication.databinding.ActivityMessagesBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.MessageViewModel
import com.example.peertutoringapplication.viewmodel.MessageViewModelFactory
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var sessionManager: SessionManager

    private val database by lazy {
        DatabaseProvider.getDatabase(this)
    }

    private val conversations = mutableListOf<ConversationItem>()
    private lateinit var conversationAdapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val repository = MessageRepository(database.messageDao())
        val factory = MessageViewModelFactory(repository)

        messageViewModel = ViewModelProvider(
            this,
            factory
        )[MessageViewModel::class.java]

        setupRecyclerView()
        observeMessages()

        binding.btnNewConversation.setOnClickListener {
            showAcceptedContacts()
        }

        loadConversations()
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(conversations) { otherUserId ->
            openConversation(otherUserId)
        }

        binding.rvConversations.layoutManager = LinearLayoutManager(this)
        binding.rvConversations.adapter = conversationAdapter
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            messageViewModel.messages.collect { messages ->
                updateConversationList(messages)
            }
        }
    }

    private fun updateConversationList(messages: List<Message>) {
        lifecycleScope.launch {
            val currentUserId = sessionManager.getUserId()

            val latestMessages = messages
                .groupBy { message ->
                    if (message.senderId == currentUserId) {
                        message.receiverId
                    } else {
                        message.senderId
                    }
                }
                .mapNotNull { (otherUserId, userMessages) ->
                    val latestMessage = userMessages.maxByOrNull { it.sentAt }
                        ?: return@mapNotNull null

                    val user = database.userDao().getUserById(otherUserId)
                        ?: return@mapNotNull null

                    ConversationItem(
                        userId = otherUserId,
                        name = "${user.firstName} ${user.lastName}",
                        lastMessage = latestMessage.content,
                        sentAt = latestMessage.sentAt
                    )
                }
                .sortedByDescending { it.sentAt }

            conversations.clear()
            conversations.addAll(latestMessages)
            conversationAdapter.notifyDataSetChanged()

            binding.tvNoConversations.visibility =
                if (conversations.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun loadConversations() {
        messageViewModel.loadMessagesForUser(sessionManager.getUserId())
    }

    private fun showAcceptedContacts() {
        lifecycleScope.launch {
            try {
                val currentUserId = sessionManager.getUserId()

                val tutorRequests = database.tutoringRequestDao()
                    .getRequestsByTutor(currentUserId)
                    .filter { it.status.equals("Accepted", ignoreCase = true) }

                val tuteeRequests = database.tutoringRequestDao()
                    .getRequestsByTutee(currentUserId)
                    .filter { it.status.equals("Accepted", ignoreCase = true) }

                val contactIds = (
                        tutorRequests.map { it.tuteeId } +
                                tuteeRequests.map { it.tutorId }
                        )
                    .filter { it != currentUserId }
                    .distinct()

                if (contactIds.isEmpty()) {
                    Toast.makeText(
                        this@MessagesActivity,
                        "You have no accepted tutoring connections yet.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val contacts = contactIds.mapNotNull { userId ->
                    database.userDao().getUserById(userId)
                }

                if (contacts.isEmpty()) {
                    Toast.makeText(
                        this@MessagesActivity,
                        "Could not load your tutoring contacts.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val contactNames = contacts.map {
                    "${it.firstName} ${it.lastName}"
                }.toTypedArray()

                AlertDialog.Builder(this@MessagesActivity)
                    .setTitle("Start a Conversation")
                    .setItems(contactNames) { _, position ->
                        openConversation(contacts[position].userId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@MessagesActivity,
                    "Could not load tutoring contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openConversation(otherUserId: Int) {
        val intent = Intent(this, ConversationActivity::class.java)
        intent.putExtra("OTHER_USER_ID", otherUserId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        if (::messageViewModel.isInitialized) {
            loadConversations()
        }
    }

    private data class ConversationItem(
        val userId: Int,
        val name: String,
        val lastMessage: String,
        val sentAt: Long
    )

    private class ConversationAdapter(
        private val items: List<ConversationItem>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

        class ConversationViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {

            val name: TextView = view.findViewById(android.R.id.text1)
            val preview: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ConversationViewHolder {

            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)

            return ConversationViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ConversationViewHolder,
            position: Int
        ) {
            val conversation = items[position]

            holder.name.text = conversation.name
            holder.preview.text = conversation.lastMessage

            holder.itemView.setOnClickListener {
                onClick(conversation.userId)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
