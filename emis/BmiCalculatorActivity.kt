package com.example.emis
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnCalculateBMI: Button
    private lateinit var btnSaveBMI: Button
    private lateinit var tvBMIResult: TextView
    private lateinit var tvBMIStatus: TextView

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        // Initialize views
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        btnCalculateBMI = findViewById(R.id.btnCalculateBMI)
        btnSaveBMI = findViewById(R.id.btnSaveBMI)
        tvBMIResult = findViewById(R.id.tvBMIResult)
        tvBMIStatus = findViewById(R.id.tvBMIStatus)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("bmi_records")

        // Set up button click listeners
        btnCalculateBMI.setOnClickListener {
            calculateBMI()
        }

        btnSaveBMI.setOnClickListener {
            saveBMI()
        }
    }

    private fun calculateBMI() {
        val height = etHeight.text.toString().toFloatOrNull()
        val weight = etWeight.text.toString().toFloatOrNull()

        if (height != null && weight != null && height > 0) {
            val bmi = weight / ((height / 100) * (height / 100))
            tvBMIResult.text = String.format("BMI: %.2f", bmi)

            // Determine BMI status
            val status = when {
                bmi < 18.5 -> "Underweight"
                bmi < 24.9 -> "Normal weight"
                bmi < 29.9 -> "Overweight"
                else -> "Obesity"
            }
            tvBMIStatus.text = "Status: $status"
        } else {
            Toast.makeText(this, "Please enter valid height and weight", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBMI() {
        val studentName = intent.getStringExtra("studentName") // Get the student name from the intent
        val height = etHeight.text.toString().toFloatOrNull()
        val weight = etWeight.text.toString().toFloatOrNull()
        val bmiResult = tvBMIResult.text.toString().replace("BMI: ", "").toFloatOrNull()
        val bmiStatus = tvBMIStatus.text.toString().replace("Status: ", "")

        if (studentName != null && height != null && weight != null && bmiResult != null) {
            // Create a new BMI record
            val bmiRecord = BMIRecord(studentName, height, weight, bmiResult, bmiStatus)

            // Save the record to Firebase
            database.push().setValue(bmiRecord)
                .addOnSuccessListener {
                    Toast.makeText(this, "BMI record saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save BMI record: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please calculate BMI before saving", Toast.LENGTH_SHORT).show()
        }
    }
}
