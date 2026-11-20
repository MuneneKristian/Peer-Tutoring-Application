package com.example.peertutoringapplication.ui.tutor

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.model.Availability
import com.example.peertutoringapplication.databinding.ActivityAvailabilityBinding
import com.example.peertutoringapplication.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

class AvailabilityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvailabilityBinding
    private lateinit var sessionManager: SessionManager

    private var startTime = ""
    private var endTime = ""

    private val availabilityList = mutableListOf<Availability>()
    private val availabilityAdapter = AvailabilityAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAvailabilityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.spDayOfWeek.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
            )
        )

        binding.rvAvailability.layoutManager = LinearLayoutManager(this)
        binding.rvAvailability.adapter = availabilityAdapter

        binding.btnStartTime.setOnClickListener {
            showTimePicker(true)
        }

        binding.btnEndTime.setOnClickListener {
            showTimePicker(false)
        }

        binding.btnSaveAvailability.setOnClickListener {
            saveAvailability()
        }

        loadAvailability()
    }

    private fun showTimePicker(isStart: Boolean) {
        val picker = TimePickerDialog(
            this,
            { _, hour, minute ->
                val time = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    hour,
                    minute
                )

                if (isStart) {
                    startTime = time
                    binding.btnStartTime.text = "Start Time: $time"
                } else {
                    endTime = time
                    binding.btnEndTime.text = "End Time: $time"
                }
            },
            9,
            0,
            false
        )

        picker.show()
    }

    private fun saveAvailability() {

        val userId = sessionManager.getUserId()
        val day = binding.spDayOfWeek.selectedItem.toString()

        val teachingMode = when (binding.rgAvailabilityMode.checkedRadioButtonId) {
            binding.rbAvailabilityPhysical.id -> "Physical"
            binding.rbAvailabilityOnline.id -> "Online"
            binding.rbAvailabilityBoth.id -> "Both"
            else -> ""
        }

        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please select both times", Toast.LENGTH_SHORT).show()
            return
        }

        if (startTime >= endTime) {
            Toast.makeText(this, "End time must be later than start time", Toast.LENGTH_SHORT).show()
            return
        }

        if (teachingMode.isEmpty()) {
            Toast.makeText(this, "Please select a teaching mode", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)

                val availability = Availability(
                    userId = userId,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    teachingMode = teachingMode
                )

                database.availabilityDao().insertAvailability(availability)

                Toast.makeText(
                    this@AvailabilityActivity,
                    "Availability saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                startTime = ""
                endTime = ""

                binding.btnStartTime.text = "Select Start Time"
                binding.btnEndTime.text = "Select End Time"

                loadAvailability()

            } catch (e: Exception) {
                Toast.makeText(
                    this@AvailabilityActivity,
                    "Could not save availability: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadAvailability() {
        lifecycleScope.launch {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)

                val records = database.availabilityDao()
                    .getAvailabilityForTutor(sessionManager.getUserId())

                availabilityList.clear()
                availabilityList.addAll(records)

                availabilityAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(
                    this@AvailabilityActivity,
                    "Could not load availability",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteAvailability(item: Availability) {

        AlertDialog.Builder(this)
            .setTitle("Delete Availability")
            .setMessage(
                "Are you sure you want to delete your ${item.dayOfWeek} availability?"
            )
            .setPositiveButton("Delete") { _, _ ->

                lifecycleScope.launch {
                    try {
                        val database = DatabaseProvider.getDatabase(applicationContext)

                        database.availabilityDao().deleteAvailability(item)

                        Toast.makeText(
                            this@AvailabilityActivity,
                            "Availability deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadAvailability()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@AvailabilityActivity,
                            "Could not delete availability",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private inner class AvailabilityAdapter :
        RecyclerView.Adapter<AvailabilityAdapter.AvailabilityViewHolder>() {

        inner class AvailabilityViewHolder(
            val container: LinearLayout,
            val textView: TextView,
            val deleteButton: Button
        ) : RecyclerView.ViewHolder(container)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AvailabilityViewHolder {

            val container = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val textView = TextView(parent.context).apply {
                textSize = 15f
            }

            val deleteButton = Button(parent.context).apply {
                text = "Delete"
            }

            container.addView(
                textView,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )

            container.addView(
                deleteButton,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            return AvailabilityViewHolder(container, textView, deleteButton)
        }

        override fun onBindViewHolder(
            holder: AvailabilityViewHolder,
            position: Int
        ) {
            val item = availabilityList[position]

            holder.textView.text =
                "${item.dayOfWeek}: ${item.startTime} - ${item.endTime}\n" +
                        "Mode: ${item.teachingMode}"

            holder.deleteButton.setOnClickListener {
                deleteAvailability(item)
            }
        }

        override fun getItemCount(): Int = availabilityList.size
    }
}
