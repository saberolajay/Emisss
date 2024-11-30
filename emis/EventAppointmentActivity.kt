package com.example.emis

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.databinding.ActivityEventAppointmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar


class EventAppointmentActivity : AppCompatActivity() {

    // Declare View Binding
    private lateinit var binding: ActivityEventAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityEventAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize CalendarView
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            binding.editTextDate.setText(selectedDate) // Update EditText with selected date
        }

        // Set OnClickListener for the Date EditText to show DatePickerDialog
        binding.editTextDate.setOnClickListener {
            showDatePicker()
        }

        // Save Event Button Logic
        binding.btnSaveEvent.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val date = binding.editTextDate.text.toString().trim()
            val description = binding.editTextDescription.text.toString().trim()

            if (title.isEmpty() || date.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                saveEvent(title, date, description)
            }
        }
    }

    // Function to display DatePickerDialog
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                binding.editTextDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Function to handle saving event logic
    private fun saveEvent(title: String, date: String, description: String) {
        // Example: Save event to database or Firebase (stubbed for now)
        Toast.makeText(this, "Event Saved:\n$title on $date\nDescription: $description", Toast.LENGTH_LONG).show()
        clearFields()
    }

    // Function to clear input fields after saving
    private fun clearFields() {
        binding.editTextTitle.text.clear()
        binding.editTextDate.text.clear()
        binding.editTextDescription.text.clear()
    }
}
