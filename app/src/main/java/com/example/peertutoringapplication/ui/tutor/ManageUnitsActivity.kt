package com.example.peertutoringapplication.ui.tutor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.UniversityUnit
import com.example.peertutoringapplication.data.repository.TutorRepository
import com.example.peertutoringapplication.data.model.TutorUnit
import com.example.peertutoringapplication.databinding.ActivityManageUnitsBinding
import com.example.peertutoringapplication.utils.SessionManager
import com.example.peertutoringapplication.viewmodel.TutorViewModel
import com.example.peertutoringapplication.viewmodel.TutorViewModelFactory
import kotlinx.coroutines.launch

class ManageUnitsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageUnitsBinding
    private lateinit var tutorViewModel: TutorViewModel
    private lateinit var sessionManager: SessionManager

    private var availableUnits = listOf<UniversityUnit>()
    private var selectedUnits = listOf<UniversityUnit>()

    private val unitAdapter = UnitAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageUnitsBinding.inflate(layoutInflater)
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

        binding.rvTeachingUnits.layoutManager = LinearLayoutManager(this)
        binding.rvTeachingUnits.adapter = unitAdapter

        observeUnits()

        tutorViewModel.loadUnits()
        tutorViewModel.loadTutorUnits(sessionManager.getUserId())

        binding.btnAddUnit.setOnClickListener {
            addSelectedUnit()
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

                // Display units currently assigned to this tutor
                val assignedIds = units.map { it.unitId }.toSet()
                selectedUnits = units.filter { it.unitId in assignedIds }

                unitAdapter.updateList(selectedUnits)

                binding.tvNoUnits.visibility =
                    if (selectedUnits.isEmpty()) android.view.View.VISIBLE
                    else android.view.View.GONE
            }
        }
    }

    private fun addSelectedUnit() {
        if (availableUnits.isEmpty()) {
            Toast.makeText(this, "No university units available", Toast.LENGTH_SHORT).show()
            return
        }

        val position = binding.spUnits.selectedItemPosition

        if (position !in availableUnits.indices) return

        val unit = availableUnits[position]

        tutorViewModel.assignUnit(
            sessionManager.getUserId(),
            unit.unitId
        )

        Toast.makeText(
            this,
            "Unit added successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private inner class UnitAdapter :
        androidx.recyclerview.widget.RecyclerView.Adapter<UnitViewHolder>() {

        private var items = listOf<UniversityUnit>()

        fun updateList(newItems: List<UniversityUnit>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): UnitViewHolder {
            val textView = android.widget.TextView(parent.context).apply {
                textSize = 16f
                setPadding(16, 20, 16, 20)
            }

            return UnitViewHolder(textView)
        }

        override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
            val unit = items[position]
            holder.textView.text = "${unit.unitCode} - ${unit.unitName}"
        }

        override fun getItemCount(): Int = items.size
    }

    private class UnitViewHolder(
        val textView: android.widget.TextView
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(textView)
}//member3