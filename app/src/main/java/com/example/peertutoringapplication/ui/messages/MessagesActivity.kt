package com.example.peertutoringapplication.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.repository.MessageRepository
import com.example.peertutoringapplication.databinding.ActivityMessagesBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.MessageViewModel
import com.example.peertutoringapplication.viewmodel.MessageViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var sessionManager: SessionManager

    private val conversations = mutableListOf<Int>()
    private lateinit var conversationAdapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val database = DatabaseProvider.getDatabase(this)
        val repository = MessageRepository(database.messageDao())
        val factory = MessageViewModelFactory(repository)

        messageViewModel = ViewModelProvider(
            this,
            factory
        )[MessageViewModel::class.java]

        setupRecyclerView()
        observeMessages()
        loadConversations()
    }

    private fun setupRecyclerView() {

        conversationAdapter = ConversationAdapter(conversations) { otherUserId ->

            val intent = Intent(this, ConversationActivity::class.java)

            intent.putExtra("OTHER_USER_ID", otherUserId)

            startActivity(intent)
        }

        binding.rvConversations.layoutManager =
            LinearLayoutManager(this)

        binding.rvConversations.adapter = conversationAdapter
    }

    private fun observeMessages() {

        lifecycleScope.launch {
            messageViewModel.messages.collect { messages ->

                conversations.clear()

                messages.forEach { message ->

                    val otherUserId = message.senderId

                    if (!conversations.contains(otherUserId)) {
                        conversations.add(otherUserId)
                    }
                }

                conversationAdapter.notifyDataSetChanged()

                binding.tvNoConversations.visibility =
                    if (conversations.isEmpty())
                        View.VISIBLE
                    else
                        View.GONE
            }
        }
    }

    private fun loadConversations() {

        val currentUserId = sessionManager.getUserId()

        messageViewModel.loadReceivedMessages(currentUserId)
    }

    override fun onResume() {
        super.onResume()

        if (::messageViewModel.isInitialized) {
            loadConversations()
        }
    }

    private class ConversationAdapter(
        private val users: List<Int>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

        class ConversationViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {

            val tvUser: TextView =
                view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): ConversationViewHolder {

            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(
                    android.R.layout.simple_list_item_1,
                    parent,
                    false
                )

            return ConversationViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ConversationViewHolder,
            position: Int
        ) {

            val userId = users[position]

            holder.tvUser.text = "Conversation with User #$userId"

            holder.itemView.setOnClickListener {
                onClick(userId)
            }
        }

        override fun getItemCount(): Int = users.size
    }
}
