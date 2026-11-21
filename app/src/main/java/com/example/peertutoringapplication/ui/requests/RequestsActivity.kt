//member5
package com.example.peertutoringapplication.ui.requests

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.TutoringRequest
import com.example.peertutoringapplication.data.repository.RequestRepository
import com.example.peertutoringapplication.databinding.ActivityRequestsBinding
import com.example.peertutoringapplication.utils.SessionManager
import kotlinx.coroutines.launch

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsBinding
    private val database by lazy {
        DatabaseProvider.getDatabase(this)
    }

    private var currentUserId: Int = -1
    private var tutors = emptyList<com.example.peertutoringapplication.data.model.User>()
    private var units = emptyList<com.example.peertutoringapplication.data.model.UniversityUnit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = SessionManager(this).getUserId()

        binding.rvIncomingRequests.layoutManager = LinearLayoutManager(this)
        binding.rvOutgoingRequests.layoutManager = LinearLayoutManager(this)

        loadTutorsAndUnits()
        loadRequests()

        binding.btnSendRequest.setOnClickListener {
            sendRequest()
        }
    }

    private fun loadTutorsAndUnits() {
        lifecycleScope.launch {
            try {
                tutors = database.userDao().getAllUsers()
                    .filter { it.userId != currentUserId }

                units = database.universityUnitDao().getAllUnits()

                binding.spTutor.adapter = ArrayAdapter(
                    this@RequestsActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    tutors.map { "${it.firstName} ${it.lastName}" }
                )

                binding.spUnit.adapter = ArrayAdapter(
                    this@RequestsActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    units.map { "${it.unitCode} - ${it.unitName}" }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsActivity,
                    "Could not load tutors and units",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendRequest() {
        if (tutors.isEmpty() || units.isEmpty()) {
            Toast.makeText(this, "No tutors or units available", Toast.LENGTH_SHORT).show()
            return
        }

        val tutor = tutors[binding.spTutor.selectedItemPosition]
        val unit = units[binding.spUnit.selectedItemPosition]
        val description = binding.etRequestDescription.text.toString().trim()

        if (description.isEmpty()) {
            binding.etRequestDescription.error = "Enter a request description"
            return
        }

        val request = TutoringRequest(
            tuteeId = currentUserId,
            tutorId = tutor.userId,
            unitId = unit.unitId,
            description = description
        )

        lifecycleScope.launch {
            try {
                database.tutoringRequestDao().insert(request)
                binding.etRequestDescription.text.clear()
                Toast.makeText(
                    this@RequestsActivity,
                    "Request sent successfully",
                    Toast.LENGTH_SHORT
                ).show()
                loadRequests()
            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsActivity,
                    "Failed to send request: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            try {
                val incoming = database.tutoringRequestDao()
                    .getByTutor(currentUserId)

                val outgoing = database.tutoringRequestDao()
                    .getByTutee(currentUserId)

                displayRequests(
                    binding.incomingRequestsContainer,
                    incoming,
                    true
                )

                displayRequests(
                    binding.outgoingRequestsContainer,
                    outgoing,
                    false
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsActivity,
                    "Could not load requests",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayRequests(
        container: LinearLayout,
        requests: List<TutoringRequest>,
        isIncoming: Boolean
    ) {
        container.removeAllViews()

        requests.forEach { request ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val details = TextView(this).apply {
                text = """
                    Request ID: ${request.requestId}
                    Unit ID: ${request.unitId}
                    Description: ${request.description}
                    Status: ${request.status}
                """.trimIndent()
                textSize = 16f
            }

            card.addView(details)

            if (isIncoming && request.status == "Pending") {
                addActionButton(card, "Accept") {
                    updateStatus(request, "Accepted")
                }
                addActionButton(card, "Decline") {
                    updateStatus(request, "Declined")
                }
            }

            if (!isIncoming && request.status == "Pending") {
                addActionButton(card, "Cancel") {
                    AlertDialog.Builder(this)
                        .setMessage("Cancel this request?")
                        .setPositiveButton("Yes") { _, _ ->
                            updateStatus(request, "Cancelled")
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }

            container.addView(card)
        }

        if (requests.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No requests available"
                textSize = 15f
            }
            container.addView(emptyText)
        }
    }

    private fun addActionButton(
        container: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            setOnClickListener { action() }
        }
        container.addView(button)
    }

    private fun updateStatus(request: TutoringRequest, status: String) {
        lifecycleScope.launch {
            try {
                database.tutoringRequestDao().updateStatus(
                    request.requestId,
                    status
                )
                loadRequests()
            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsActivity,
                    "Could not update request",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}