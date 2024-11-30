package com.example.emis

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.Adapter.StudentAdapter

class StudentListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        // Sample data for students
        val studentList = listOf(
            "John Doe",
            "Jane Smith",
            "Mark Johnson",
            "Emily Davis",
            "Michael Brown"
        )

        // RecyclerView setup
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StudentAdapter(studentList) { studentName ->
            onStudentClick(studentName)
        }
    }

    // Function to handle student clicks
    private fun onStudentClick(studentName: String) {
        Toast.makeText(this, "Clicked on: $studentName", Toast.LENGTH_SHORT).show()

        // Start the BmiCalculatorActivity
        val intent = Intent(this, BmiCalculatorActivity::class.java)

        // Optionally, pass the student's name to the BMI Calculator Activity
        intent.putExtra("STUDENT_NAME", studentName)
        startActivity(intent)
    }
}
