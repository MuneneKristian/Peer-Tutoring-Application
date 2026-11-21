package com.example.peertutoringapplication.ui.student

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.TutorProfile
import com.example.peertutoringapplication.data.model.User
import com.example.peertutoringapplication.data.repository.TutorRepository
import com.example.peertutoringapplication.databinding.ActivityFindTutorsBinding
import com.example.peertutoringapplication.ui.tutor.TutorDetailsActivity
import kotlinx.coroutines.launch

class FindTutorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindTutorsBinding
    private lateinit var repository: TutorRepository

    private var tutorList = listOf<TutorProfile>()
    private var userList = listOf<User>()

    private val tutorAdapter = TutorAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFindTutorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = DatabaseProvider.getDatabase(applicationContext)

        repository = TutorRepository(
            database.tutorProfileDao(),
            database.universityUnitDao(),
            database.tutorUnitDao(),
            database.availabilityDao()
        )

        binding.rvTutors.layoutManager = LinearLayoutManager(this)
        binding.rvTutors.adapter = tutorAdapter

        binding.btnSearchTutor.setOnClickListener {
            searchTutors()
        }

        loadTutors()
    }

    private fun loadTutors() {
        lifecycleScope.launch {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)

                tutorList = repository.getAllTutorProfiles()
                userList = database.userDao().getAllUsers()

                displayTutors(tutorList)

            } catch (e: Exception) {
                Toast.makeText(
                    this@FindTutorsActivity,
                    "Could not load tutors: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun searchTutors() {
        val query = binding.etSearchTutor.text.toString().trim()

        if (query.isEmpty()) {
            displayTutors(tutorList)
            return
        }

        lifecycleScope.launch {
            try {
                val matchingProfiles = tutorList.filter { profile ->
                    val user = userList.find { it.userId == profile.userId }

                    val nameMatches = user?.let {
                        "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true)
                    } ?: false

                    val courseMatches = profile.course.contains(query, ignoreCase = true)

                    val unitMatches = repository.getUnitsForTutor(profile.userId).any {
                        it.unitCode.contains(query, ignoreCase = true) ||
                                it.unitName.contains(query, ignoreCase = true)
                    }

                    nameMatches || courseMatches || unitMatches
                }

                displayTutors(matchingProfiles)

            } catch (e: Exception) {
                Toast.makeText(
                    this@FindTutorsActivity,
                    "Search failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayTutors(profiles: List<TutorProfile>) {
        tutorAdapter.updateList(profiles)

        binding.tvNoTutors.visibility =
            if (profiles.isEmpty()) View.VISIBLE else View.GONE
    }

    private inner class TutorAdapter :
        RecyclerView.Adapter<TutorAdapter.TutorViewHolder>() {

        private var items = listOf<TutorProfile>()

        fun updateList(newItems: List<TutorProfile>) {
            items = newItems
            notifyDataSetChanged()
        }

        inner class TutorViewHolder(val textView: TextView) :
            RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TutorViewHolder {

            val textView = TextView(parent.context).apply {
                textSize = 16f
                setPadding(20, 24, 20, 24)
                setBackgroundResource(R.drawable.list_selector_background)
            }

            return TutorViewHolder(textView)
        }

        override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
            val profile = items[position]
            val user = userList.find { it.userId == profile.userId }

            holder.textView.text =
                "${user?.firstName ?: "Tutor"} ${user?.lastName ?: ""}\n" +
                "Course: ${profile.course}\n" +
                "Year: ${profile.yearOfStudy}\n" +
                "Teaching mode: ${profile.teachingMode}\n" +
                "Tap to view details"

            holder.textView.setOnClickListener {
                val intent = Intent(
                    this@FindTutorsActivity,
                    TutorDetailsActivity::class.java
                )

                intent.putExtra("TUTOR_USER_ID", profile.userId)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
