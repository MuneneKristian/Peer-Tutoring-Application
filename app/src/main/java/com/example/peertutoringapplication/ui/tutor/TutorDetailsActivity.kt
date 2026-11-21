package com.example.peertutoringapplication.ui.tutor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.databinding.ActivityTutorDetailsBinding
import kotlinx.coroutines.launch

class TutorDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTutorDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tutorId = intent.getIntExtra("TUTOR_USER_ID", -1)

        if (tutorId == -1) {
            Toast.makeText(this, "Tutor information not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTutorDetails(tutorId)
    }

    private fun loadTutorDetails(tutorId: Int) {

        lifecycleScope.launch {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)

                val profile = database.tutorProfileDao()
                    .getTutorProfile(tutorId)

                val user = database.userDao()
                    .getUserByEmail(
                        database.userDao().getAllUsers()
                            .find { it.userId == tutorId }?.email ?: ""
                    )

                if (profile == null || user == null) {
                    Toast.makeText(
                        this@TutorDetailsActivity,
                        "Tutor profile not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                binding.tvTutorName.text =
                    "${user.firstName} ${user.lastName}"

                binding.tvTutorCourse.text =
                    "Course: ${profile.course}"

                binding.tvTutorYear.text =
                    "Year of Study: ${profile.yearOfStudy}"

                binding.tvTutorBio.text =
                    profile.bio

                binding.tvTutorTeachingMode.text =
                    "Teaching Mode: ${profile.teachingMode}"

                val units = database.tutorUnitDao()
                    .getUnitsForTutor(tutorId)

                binding.tvTutorUnits.text =
                    if (units.isEmpty()) {
                        "No teaching units listed."
                    } else {
                        units.joinToString("\n") {
                            "${it.unitCode} - ${it.unitName}"
                        }
                    }

                val availability = database.availabilityDao()
                    .getAvailabilityForTutor(tutorId)

                binding.tvTutorAvailability.text =
                    if (availability.isEmpty()) {
                        "No availability listed."
                    } else {
                        availability.joinToString("\n\n") {
                            "${it.dayOfWeek}: ${it.startTime} - ${it.endTime}\n" +
                                    "Mode: ${it.teachingMode}"
                        }
                    }

            } catch (e: Exception) {
                Toast.makeText(
                    this@TutorDetailsActivity,
                    "Could not load tutor details: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
