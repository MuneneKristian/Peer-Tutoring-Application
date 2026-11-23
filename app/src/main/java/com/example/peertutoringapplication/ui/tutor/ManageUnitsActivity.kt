package com.example.peertutoringapplication.ui.tutor

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.UniversityUnit
import com.example.peertutoringapplication.data.repository.TutorRepository
import com.example.peertutoringapplication.databinding.ActivityManageUnitsBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.TutorViewModel
import com.example.peertutoringapplication.viewmodel.TutorViewModelFactory
import kotlinx.coroutines.launch

class ManageUnitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUnitsBinding
    private lateinit var tutorViewModel: TutorViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: TutorRepository

    private var availableUnits = listOf<UniversityUnit>()
    private var selectedUnits = listOf<UniversityUnit>()

    private val unitAdapter = UnitAdapter()

    private val sampleUnits = listOf(
        UniversityUnit(unitCode = "BBIT101", unitName = "Introduction to Programming"),
        UniversityUnit(unitCode = "BBIT102", unitName = "Object-Oriented Programming"),
        UniversityUnit(unitCode = "BBIT103", unitName = "Database Management Systems"),
        UniversityUnit(unitCode = "BBIT104", unitName = "Web Application Development"),
        UniversityUnit(unitCode = "BBIT105", unitName = "Application Programming for the Internet"),
        UniversityUnit(unitCode = "BBIT106", unitName = "Data Structures and Algorithms"),
        UniversityUnit(unitCode = "BBIT107", unitName = "Mobile Application Development"),
        UniversityUnit(unitCode = "BBIT108", unitName = "Software Engineering"),
        UniversityUnit(unitCode = "BBIT109", unitName = "Operating Systems"),
        UniversityUnit(unitCode = "BBIT110", unitName = "Computer Organization and Architecture"),
        UniversityUnit(unitCode = "BBIT111", unitName = "Probability and Statistics"),
        UniversityUnit(unitCode = "BBIT112", unitName = "Simulation and Modelling"),
        UniversityUnit(unitCode = "BBIT113", unitName = "Operations Research"),
        UniversityUnit(unitCode = "BBIT114", unitName = "Research Methodology"),
        UniversityUnit(unitCode = "BBIT115", unitName = "Artificial Intelligence"),
        UniversityUnit(unitCode = "BBIT116", unitName = "Accounting Information Systems"),
        UniversityUnit(unitCode = "BBIT117", unitName = "Business Analytics"),
        UniversityUnit(unitCode = "BBIT118", unitName = "Systems Analysis and Design"),
        UniversityUnit(unitCode = "BBIT119", unitName = "Computer Networks"),
        UniversityUnit(unitCode = "BBIT120", unitName = "Ethics in Information Technology")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageUnitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val database = DatabaseProvider.getDatabase(applicationContext)

        repository = TutorRepository(
            database.tutorProfileDao(),
            database.universityUnitDao(),
            database.tutorUnitDao(),
            database.availabilityDao()
        )

        tutorViewModel = ViewModelProvider(
            this,
            TutorViewModelFactory(repository)
        )[TutorViewModel::class.java]

        binding.rvTeachingUnits.layoutManager = LinearLayoutManager(this)
        binding.rvTeachingUnits.adapter = unitAdapter

        observeUnits()

        binding.btnAddUnit.setOnClickListener {
            addSelectedUnit()
        }

        seedAndLoadUnits()
    }

    private fun seedAndLoadUnits() {
        lifecycleScope.launch {
            try {
                val existingUnits = repository.getAllUnits()
                val existingCodes = existingUnits.map { it.unitCode }.toSet()

                for (unit in sampleUnits) {
                    if (unit.unitCode !in existingCodes) {
                        repository.addUnit(unit)
                    }
                }

                tutorViewModel.loadUnits()
                tutorViewModel.loadTutorUnits(sessionManager.getUserId())

            } catch (e: Exception) {
                Toast.makeText(
                    this@ManageUnitsActivity,
                    "Could not load units: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeUnits() {

        lifecycleScope.launch {
            tutorViewModel.units.collect { units ->
                availableUnits = units

                val spinnerAdapter = ArrayAdapter(
                    this@ManageUnitsActivity,
                    android.R.layout.simple_spinner_item,
                    units.map { "${it.unitCode} - ${it.unitName}" }
                )

                spinnerAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )

                binding.spUnits.adapter = spinnerAdapter
            }
        }

        lifecycleScope.launch {
            tutorViewModel.tutorUnits.collect { units ->
                selectedUnits = units
                unitAdapter.updateList(selectedUnits)

                binding.tvNoUnits.visibility =
                    if (selectedUnits.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            tutorViewModel.message.collect { message ->
                if (message != null) {
                    Toast.makeText(
                        this@ManageUnitsActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addSelectedUnit() {

        if (availableUnits.isEmpty()) {
            Toast.makeText(this, "No units available", Toast.LENGTH_SHORT).show()
            return
        }

        val position = binding.spUnits.selectedItemPosition

        if (position !in availableUnits.indices) return

        val unit = availableUnits[position]

        if (selectedUnits.any { it.unitId == unit.unitId }) {
            Toast.makeText(
                this,
                "You have already added this unit",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        tutorViewModel.assignUnit(
            sessionManager.getUserId(),
            unit.unitId
        )
    }

    private fun confirmRemoveUnit(unit: UniversityUnit) {

        AlertDialog.Builder(this)
            .setTitle("Remove Teaching Unit")
            .setMessage(
                "Are you sure you want to remove ${unit.unitCode} - ${unit.unitName} from your teaching units?"
            )
            .setPositiveButton("Remove") { _, _ ->

                tutorViewModel.removeUnit(
                    sessionManager.getUserId(),
                    unit.unitId
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private inner class UnitAdapter :
        RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

        private var items = listOf<UniversityUnit>()

        fun updateList(newItems: List<UniversityUnit>) {
            items = newItems
            notifyDataSetChanged()
        }

        inner class UnitViewHolder(
            val row: LinearLayout,
            val textView: TextView,
            val removeButton: Button
        ) : RecyclerView.ViewHolder(row)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): UnitViewHolder {

            val row = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(16, 12, 8, 12)
            }

            val textView = TextView(parent.context).apply {
                textSize = 15f
                setTextColor(android.graphics.Color.BLACK)
            }

            val removeButton = Button(parent.context).apply {
                text = "−"
                textSize = 20f
                minWidth = 48
                contentDescription = "Remove teaching unit"
                setTextColor(android.graphics.Color.WHITE)
                backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.rgb(220, 53, 69)
                    )
            }

            row.addView(
                textView,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(
                removeButton,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            return UnitViewHolder(row, textView, removeButton)
        }

        override fun onBindViewHolder(
            holder: UnitViewHolder,
            position: Int
        ) {
            val unit = items[position]

            holder.textView.text =
                "${unit.unitCode} - ${unit.unitName}"

            holder.removeButton.setOnClickListener {
                confirmRemoveUnit(unit)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
