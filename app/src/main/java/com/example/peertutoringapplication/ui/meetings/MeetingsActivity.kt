package com.example.peertutoringapplication.ui.meetings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertutoringapplication.data.model.Meeting
import com.example.peertutoringapplication.databinding.ActivityMeetingsBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.MeetingViewModel
import com.example.peertutoringapplication.viewmodel.MeetingViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MeetingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeetingsBinding
    private lateinit var meetingViewModel: MeetingViewModel
    private lateinit var sessionManager: SessionManager

    private var selectedDate = ""
    private var selectedTime = ""

    private val meetings = mutableListOf<Meeting>()
    private lateinit var meetingAdapter: MeetingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMeetingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val factory = MeetingViewModelFactory(application)
        meetingViewModel = ViewModelProvider(this, factory)[MeetingViewModel::class.java]

        setupSpinners()
        setupRecyclerView()
        setupDatePicker()
        setupTimePicker()
        setupButton()
        loadMeetings()
    }

    private fun setupSpinners() {
        val modes = listOf("Physical", "Online")

        binding.spMeetingMode.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            modes
        )
    }

    private fun setupRecyclerView() {
        meetingAdapter = MeetingAdapter(
            meetings,
            onConfirm = { meeting ->
                meetingViewModel.updateMeetingStatus(meeting.meetingId, "Confirmed")
                loadMeetings()
            },
            onDecline = { meeting ->
                meetingViewModel.updateMeetingStatus(meeting.meetingId, "Declined")
                loadMeetings()
            }
        )

        binding.rvMeetings.layoutManager = LinearLayoutManager(this)
        binding.rvMeetings.adapter = meetingAdapter
    }

    private fun setupDatePicker() {
        binding.etMeetingDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                    )
                    binding.etMeetingDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker() {
        binding.etMeetingTime.setOnClickListener {
            val calendar = Calendar.getInstance()

            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hour,
                        minute
                    )
                    binding.etMeetingTime.setText(selectedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupButton() {
        binding.btnProposeMeeting.setOnClickListener {
            proposeMeeting()
        }
    }

    private fun proposeMeeting() {
        val requestId = binding.spMeetingRequest.selectedItem?.toString()?.toIntOrNull()
        val mode = binding.spMeetingMode.selectedItem.toString()
        val location = binding.etMeetingLocation.text.toString().trim()

        if (requestId == null || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please complete all meeting details.", Toast.LENGTH_SHORT).show()
            return
        }

        val meeting = Meeting(
            requestId = requestId,
            meetingDateTime = "$selectedDate $selectedTime",
            meetingMode = mode,
            status = "Proposed",
            location = location
        )

        meetingViewModel.insertMeeting(meeting)

        Toast.makeText(this, "Meeting proposed successfully.", Toast.LENGTH_SHORT).show()

        binding.etMeetingDate.text.clear()
        binding.etMeetingTime.text.clear()
        binding.etMeetingLocation.text.clear()

        selectedDate = ""
        selectedTime = ""

        loadMeetings()
    }

    private fun loadMeetings() {
        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            try {
                val result = meetingViewModel.getMeetingsForUser(userId)
                meetings.clear()
                meetings.addAll(result)
                meetingAdapter.notifyDataSetChanged()

                binding.tvNoMeetings.visibility =
                    if (meetings.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Toast.makeText(
                    this@MeetingsActivity,
                    "Could not load meetings: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}