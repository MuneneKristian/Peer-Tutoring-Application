package com.example.peertutoringapplication.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.peertutoringapplication.databinding.ActivityDashboardBinding
import com.example.peertutoringapplication.ui.auth.LoginActivity
import com.example.peertutoringapplication.ui.tutor.TutorProfileActivity
import com.example.peertutoringapplication.ui.tutor.ManageUnitsActivity
import com.example.peertutoringapplication.ui.tutor.AvailabilityActivity
import com.example.peertutoringapplication.ui.student.FindTutorsActivity
import com.example.peertutoringapplication.ui.requests.RequestsActivity
import com.example.peertutoringapplication.ui.meetings.MeetingsActivity
import com.example.peertutoringapplication.ui.messages.MessagesActivity
import com.example.peertutoringapplication.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        displayUserInformation()
        setupNavigation()
    }

    private fun displayUserInformation() {
        val role = sessionManager.getUserRole()

        binding.tvWelcome.text = "Welcome to Peer Tutoring"
        binding.tvRole.text = "Logged in as: $role"
    }

    private fun setupNavigation() {

        binding.btnFindTutors.setOnClickListener {
            startActivity(Intent(this, FindTutorsActivity::class.java))
        }

        binding.btnTutorProfile.setOnClickListener {
            startActivity(Intent(this, TutorProfileActivity::class.java))
        }

        binding.btnManageUnits.setOnClickListener {
            startActivity(Intent(this, ManageUnitsActivity::class.java))
        }

        binding.btnAvailability.setOnClickListener {
            startActivity(Intent(this, AvailabilityActivity::class.java))
        }

        binding.btnRequests.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        binding.btnMeetings.setOnClickListener {
            startActivity(Intent(this, MeetingsActivity::class.java))
        }

        binding.btnMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
