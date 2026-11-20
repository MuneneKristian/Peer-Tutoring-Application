package com.example.peertutoringapplication.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.peertutoringapplication.databinding.ActivityDashboardBinding
import com.example.peertutoringapplication.ui.auth.LoginActivity
import com.example.peertutoringapplication.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Check whether the user is logged in
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
            showMessage("Tutor Search")
        }

        binding.btnTutorProfile.setOnClickListener {
            showMessage("Tutor Profile")
        }

        binding.btnManageUnits.setOnClickListener {
            showMessage("Manage Teaching Units")
        }

        binding.btnAvailability.setOnClickListener {
            showMessage("Manage Availability")
        }

        binding.btnRequests.setOnClickListener {
            showMessage("Tutoring Requests")
        }

        binding.btnMeetings.setOnClickListener {
            showMessage("Meetings")
        }

        binding.btnMessages.setOnClickListener {
            showMessage("Messages")
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            navigateToLogin()
        }
    }

    private fun showMessage(feature: String) {
        Toast.makeText(
            this,
            "$feature will be available soon",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}