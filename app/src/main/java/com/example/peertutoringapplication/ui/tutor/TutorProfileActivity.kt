package com.example.peertutoringapplication.ui.tutor

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.TutorProfile
import com.example.peertutoringapplication.data.repository.TutorRepository
import com.example.peertutoringapplication.databinding.ActivityTutorProfileBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.TutorViewModel
import com.example.peertutoringapplication.viewmodel.TutorViewModelFactory
import kotlinx.coroutines.launch

class TutorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorProfileBinding
    private lateinit var tutorViewModel: TutorViewModel
    private lateinit var sessionManager: SessionManager

    private var profileExists = false
    private var originalProfile: TutorProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTutorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val database = DatabaseProvider.getDatabase(applicationContext)

        val repository = TutorRepository(
            database.tutorProfileDao(),
            database.universityUnitDao(),
            database.tutorUnitDao(),
            database.availabilityDao()
        )

        tutorViewModel = ViewModelProvider(
            this,
            TutorViewModelFactory(repository)
        )[TutorViewModel::class.java]

        observeProfile()
        observeMessages()

        setupButtons()

        tutorViewModel.loadTutorProfile(sessionManager.getUserId())
    }

    private fun setupButtons() {

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnEditProfile.setOnClickListener {
            setFormEnabled(true)

            binding.btnEditProfile.visibility = View.GONE
            binding.btnSaveProfile.visibility = View.VISIBLE
            binding.btnSaveProfile.text = "Save Changes"
            binding.btnCancelEdit.visibility = View.VISIBLE
        }

        binding.btnCancelEdit.setOnClickListener {
            originalProfile?.let { profile ->
                displayProfile(profile)
            }

            setFormEnabled(false)
            showExistingProfileButtons()
        }
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            tutorViewModel.tutorProfile.collect { profile ->

                if (profile != null) {
                    profileExists = true
                    originalProfile = profile

                    displayProfile(profile)
                    setFormEnabled(false)
                    showExistingProfileButtons()
                }
            }
        }
    }

    private fun displayProfile(profile: TutorProfile) {

        binding.etCourse.setText(profile.course)
        binding.etYearOfStudy.setText(profile.yearOfStudy)
        binding.etBio.setText(profile.bio)

        binding.rgTeachingMode.clearCheck()

        when (profile.teachingMode) {
            "Physical" -> binding.rbPhysical.isChecked = true
            "Online" -> binding.rbOnline.isChecked = true
            "Both" -> binding.rbBoth.isChecked = true
        }
    }

    private fun setFormEnabled(enabled: Boolean) {

        binding.etCourse.isEnabled = enabled
        binding.etYearOfStudy.isEnabled = enabled
        binding.etBio.isEnabled = enabled

        binding.rbPhysical.isEnabled = enabled
        binding.rbOnline.isEnabled = enabled
        binding.rbBoth.isEnabled = enabled
    }

    private fun showExistingProfileButtons() {

        binding.btnEditProfile.visibility = View.VISIBLE
        binding.btnSaveProfile.visibility = View.GONE
        binding.btnCancelEdit.visibility = View.GONE

        binding.btnSaveProfile.text = "Save Changes"
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            tutorViewModel.message.collect { message ->

                if (message != null) {

                    Toast.makeText(
                        this@TutorProfileActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()

                    if (
                        message == "Tutor profile saved successfully." ||
                        message == "Tutor profile updated successfully."
                    ) {
                        profileExists = true

                        // Reload the saved profile before leaving edit mode.
                        tutorViewModel.loadTutorProfile(
                            sessionManager.getUserId()
                        )
                    }
                }
            }
        }
    }

    private fun saveProfile() {

        val userId = sessionManager.getUserId()

        val course = binding.etCourse.text.toString().trim()
        val year = binding.etYearOfStudy.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        val teachingMode = when (binding.rgTeachingMode.checkedRadioButtonId) {
            binding.rbPhysical.id -> "Physical"
            binding.rbOnline.id -> "Online"
            binding.rbBoth.id -> "Both"
            else -> ""
        }

        if (course.isEmpty() || year.isEmpty() || bio.isEmpty()) {
            Toast.makeText(
                this,
                "Please fill in all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (teachingMode.isEmpty()) {
            Toast.makeText(
                this,
                "Please select a teaching mode",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val profile = TutorProfile(
            userId = userId,
            bio = bio,
            course = course,
            yearOfStudy = year,
            teachingMode = teachingMode
        )

        if (profileExists) {
            tutorViewModel.updateTutorProfile(profile)
        } else {
            tutorViewModel.saveTutorProfile(profile)
        }
    }
}
