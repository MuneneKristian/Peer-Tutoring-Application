package com.example.peertutoringapplication.ui.requests

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.TutoringRequest
import com.example.peertutoringapplication.data.model.User
import com.example.peertutoringapplication.data.model.UniversityUnit
import com.example.peertutoringapplication.databinding.ActivityRequestsBinding
import com.example.peertutoringapplication.utils.SessionManager
import kotlinx.coroutines.launch

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsBinding

    private val database by lazy {
        DatabaseProvider.getDatabase(this)
    }

    private var currentUserId: Int = -1

    private var tutors = emptyList<User>()
    private var units = emptyList<UniversityUnit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = SessionManager(this).getUserId()

        binding.rvIncomingRequests.layoutManager =
            LinearLayoutManager(this)

        binding.rvOutgoingRequests.layoutManager =
            LinearLayoutManager(this)

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
                    .filter {
                        it.userId != currentUserId &&
                                (it.role.equals("Tutor", true) ||
                                        it.role.equals("Both", true))
                    }

                units = database.universityUnitDao().getAllUnits()

                binding.spTutor.adapter = ArrayAdapter(
                    this@RequestsActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    tutors.map {
                        "${it.firstName} ${it.lastName}"
                    }
                )

                binding.spUnit.adapter = ArrayAdapter(
                    this@RequestsActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    units.map {
                        "${it.unitCode} - ${it.unitName}"
                    }
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
            Toast.makeText(
                this,
                "No tutors or units available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val tutor = tutors[binding.spTutor.selectedItemPosition]
        val unit = units[binding.spUnit.selectedItemPosition]

        val description =
            binding.etRequestDescription.text.toString().trim()

        if (description.isEmpty()) {
            binding.etRequestDescription.error =
                "Enter a request description"
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
                database.tutoringRequestDao().insertRequest(request)

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
                    .getRequestsByTutor(currentUserId)

                val outgoing = database.tutoringRequestDao()
                    .getRequestsByTutee(currentUserId)

                binding.rvIncomingRequests.adapter =
                    RequestAdapter(incoming, true)

                binding.rvOutgoingRequests.adapter =
                    RequestAdapter(outgoing, false)

                binding.tvNoIncomingRequests.visibility =
                    if (incoming.isEmpty()) View.VISIBLE else View.GONE

                binding.tvNoOutgoingRequests.visibility =
                    if (outgoing.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsActivity,
                    "Could not load requests: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateStatus(
        request: TutoringRequest,
        status: String
    ) {
        lifecycleScope.launch {
            try {
                database.tutoringRequestDao().updateRequestStatus(
                    request.requestId,
                    status
                )

                Toast.makeText(
                    this@RequestsActivity,
                    "Request updated",
                    Toast.LENGTH_SHORT
                ).show()

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

    private inner class RequestAdapter(
        private val requests: List<TutoringRequest>,
        private val isIncoming: Boolean
    ) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

        inner class RequestViewHolder(
            val layout: LinearLayout
        ) : RecyclerView.ViewHolder(layout)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RequestViewHolder {

            val layout = LinearLayout(this@RequestsActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 20, 20, 20)
            }

            return RequestViewHolder(layout)
        }

        override fun onBindViewHolder(
            holder: RequestViewHolder,
            position: Int
        ) {
            val request = requests[position]
            val layout = holder.layout

            layout.removeAllViews()

            val details = TextView(this@RequestsActivity).apply {
                text = """
                    Request ID: ${request.requestId}
                    Unit ID: ${request.unitId}
                    Description: ${request.description}
                    Status: ${request.status}
                """.trimIndent()

                textSize = 15f
                setPadding(8, 8, 8, 8)
            }

            layout.addView(details)

            if (isIncoming && request.status == "Pending") {

                addButton(layout, "Accept") {
                    updateStatus(request, "Accepted")
                }

                addButton(layout, "Decline") {
                    updateStatus(request, "Declined")
                }
            }

            if (!isIncoming && request.status == "Pending") {

                addButton(layout, "Cancel") {

                    AlertDialog.Builder(this@RequestsActivity)
                        .setMessage("Cancel this request?")
                        .setPositiveButton("Yes") { _, _ ->
                            updateStatus(request, "Cancelled")
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
        }

        override fun getItemCount(): Int = requests.size

        private fun addButton(
            layout: LinearLayout,
            label: String,
            action: () -> Unit
        ) {
            val button = Button(this@RequestsActivity).apply {
                text = label
                setOnClickListener { action() }
            }

            layout.addView(button)
        }
    }
}
