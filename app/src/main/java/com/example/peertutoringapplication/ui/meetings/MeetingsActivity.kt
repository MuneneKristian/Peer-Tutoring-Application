package com.example.peertutoringapplication.ui.meetings

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.Meeting
import com.example.peertutoringapplication.data.repository.MeetingRepository
import com.example.peertutoringapplication.databinding.ActivityMeetingsBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.MeetingViewModel
import com.example.peertutoringapplication.viewmodel.MeetingViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MeetingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeetingsBinding
    private lateinit var meetingViewModel: MeetingViewModel
    private lateinit var sessionManager: SessionManager

    private val pendingMeetings = mutableListOf<Meeting>()
    private val scheduledMeetings = mutableListOf<Meeting>()

    private lateinit var pendingAdapter: MeetingAdapter
    private lateinit var scheduledAdapter: MeetingAdapter

    private var currentUserId = 0
    private val requestOptions = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMeetingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId()

        val database = DatabaseProvider.getDatabase(this)
        val repository = MeetingRepository(database.meetingDao())
        val factory = MeetingViewModelFactory(repository)

        meetingViewModel = ViewModelProvider(
            this,
            factory
        )[MeetingViewModel::class.java]

        setupSpinners()
        setupRecyclerViews()
        setupDateTimeFields()
        setupProposeButton()
        loadAcceptedRequests()
        observeMeetings()

        meetingViewModel.loadMeetings(currentUserId)
    }

    private fun setupSpinners() {
        val modes = listOf("Physical", "Online")

        binding.spMeetingMode.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            modes
        )
    }

    private fun setupRecyclerViews() {

        pendingAdapter = MeetingAdapter(
            pendingMeetings,
            currentUserId,
            onAction = { meeting, status ->
                updateStatus(meeting, status)
            }
        )

        scheduledAdapter = MeetingAdapter(
            scheduledMeetings,
            currentUserId,
            onAction = { meeting, status ->
                updateStatus(meeting, status)
            }
        )

        binding.rvPendingMeetings.layoutManager =
            LinearLayoutManager(this)

        binding.rvPendingMeetings.adapter = pendingAdapter

        binding.rvScheduledMeetings.layoutManager =
            LinearLayoutManager(this)

        binding.rvScheduledMeetings.adapter = scheduledAdapter
    }

    private fun setupDateTimeFields() {

        binding.etMeetingDate.setOnClickListener {
            Toast.makeText(
                this,
                "Enter date in YYYY-MM-DD format",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.etMeetingTime.setOnClickListener {
            Toast.makeText(
                this,
                "Enter time in HH:MM format",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadAcceptedRequests() {

        lifecycleScope.launch {
            try {
                val dao = DatabaseProvider
                    .getDatabase(this@MeetingsActivity)
                    .tutoringRequestDao()

                val tutorRequests =
                    dao.getRequestsByTutor(currentUserId)

                val tuteeRequests =
                    dao.getRequestsByTutee(currentUserId)

                val acceptedRequests =
                    (tutorRequests + tuteeRequests)
                        .filter { it.status.equals("Accepted", true) }
                        .distinctBy { it.requestId }

                requestOptions.clear()
                requestOptions.addAll(
                    acceptedRequests.map { it.requestId }
                )

                val labels = acceptedRequests.map {
                    "Request #${it.requestId} - Unit #${it.unitId}"
                }

                binding.spMeetingRequest.adapter = ArrayAdapter(
                    this@MeetingsActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    labels
                )

            } catch (e: Exception) {
                Toast.makeText(
                    this@MeetingsActivity,
                    "Could not load requests: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupProposeButton() {

        binding.btnProposeMeeting.setOnClickListener {

            val position = binding.spMeetingRequest.selectedItemPosition

            if (position < 0 || position >= requestOptions.size) {
                Toast.makeText(
                    this,
                    "Select an accepted tutoring request.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val date = binding.etMeetingDate.text.toString().trim()
            val time = binding.etMeetingTime.text.toString().trim()
            val mode = binding.spMeetingMode.selectedItem.toString()
            val location =
                binding.etMeetingLocation.text.toString().trim()

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(
                    this,
                    "Enter the meeting date and time.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val meeting = Meeting(
                requestId = requestOptions[position],
                meetingDateTime = "$date $time",
                meetingMode = mode,
                status = "Proposed",
                location = location
            )

            meetingViewModel.createMeeting(meeting)

            binding.etMeetingDate.text.clear()
            binding.etMeetingTime.text.clear()
            binding.etMeetingLocation.text.clear()
        }
    }

    private fun observeMeetings() {

        lifecycleScope.launch {
            meetingViewModel.meetings.collect { result ->

                pendingMeetings.clear()
                scheduledMeetings.clear()

                result.forEach { meeting ->

                    when (meeting.status) {
                        "Proposed" -> pendingMeetings.add(meeting)
                        "Confirmed" -> scheduledMeetings.add(meeting)
                    }
                }

                pendingAdapter.notifyDataSetChanged()
                scheduledAdapter.notifyDataSetChanged()

                binding.tvNoPendingMeetings.visibility =
                    if (pendingMeetings.isEmpty())
                        View.VISIBLE else View.GONE

                binding.tvNoScheduledMeetings.visibility =
                    if (scheduledMeetings.isEmpty())
                        View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateStatus(meeting: Meeting, status: String) {
        meetingViewModel.updateMeetingStatus(
            meeting.meetingId,
            status
        )

        meetingViewModel.loadMeetings(currentUserId)
    }

    private class MeetingAdapter(
        private val meetings: List<Meeting>,
        private val currentUserId: Int,
        private val onAction: (Meeting, String) -> Unit
    ) : RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>() {

        class MeetingViewHolder(
            val container: LinearLayout
        ) : RecyclerView.ViewHolder(container)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MeetingViewHolder {

            val container = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 16, 20, 16)
                setBackgroundColor(0xFFE8EDF5.toInt())
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }

            return MeetingViewHolder(container)
        }

        override fun onBindViewHolder(
            holder: MeetingViewHolder,
            position: Int
        ) {

            val meeting = meetings[position]
            holder.container.removeAllViews()

            val details = TextView(holder.container.context).apply {
                text = """
                    Request: #${meeting.requestId}
                    Date and Time: ${meeting.meetingDateTime}
                    Mode: ${meeting.meetingMode}
                    Location: ${meeting.location}
                    Status: ${meeting.status}
                """.trimIndent()
                textSize = 15f
                setTextColor(0xFF172B4D.toInt())
            }

            holder.container.addView(details)

            if (meeting.status == "Proposed") {

                val confirmButton = Button(holder.container.context).apply {
                    text = "Confirm Meeting"
                    setOnClickListener {
                        onAction(meeting, "Confirmed")
                    }
                }

                val declineButton = Button(holder.container.context).apply {
                    text = "Decline Meeting"
                    setOnClickListener {
                        onAction(meeting, "Declined")
                    }
                }

                holder.container.addView(confirmButton)
                holder.container.addView(declineButton)
            }
        }

        override fun getItemCount(): Int = meetings.size
    }
}
