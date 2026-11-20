package com.example.peertutoringapplication.ui.tutor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
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

        loadProfile()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfile() {
        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            val profile = tutorViewModel.getProfile(userId)

            if (profile != null) {
                binding.etCourse.setText(profile.course)
                binding.etYearOfStudy.setText(profile.yearOfStudy)
                binding.etBio.setText(profile.bio)

                when (profile.teachingMode) {
                    "Physical" -> binding.rbPhysical.isChecked = true
                    "Online" -> binding.rbOnline.isChecked = true
                    "Both" -> binding.rbBoth.isChecked = true
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
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (teachingMode.isEmpty()) {
            Toast.makeText(this, "Please select a teaching mode", Toast.LENGTH_SHORT).show()
            return
        }

        tutorViewModel.saveProfile(
            userId,
            bio,
            course,
            year,
            teachingMode
        )

        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
    }
}//Member3