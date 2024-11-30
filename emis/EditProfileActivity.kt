package com.example.emis

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var saveButton: AppCompatButton

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()

        // Get current user reference
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        }

        // Initialize views
        phoneEditText = findViewById(R.id.edit_phone_number)
        addressEditText = findViewById(R.id.edit_address)
        saveButton = findViewById(R.id.save_button)

        // Load existing profile data
        displayUpdatedProfile()

        // Set up save button click listener
        saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val phoneNumber = phoneEditText.text.toString()
        val address = addressEditText.text.toString()

        if (phoneNumber.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Confirmation dialog
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Confirm Save")
        alertDialog.setMessage("Are you sure you want to save the changes?")
        alertDialog.setPositiveButton("Yes") { _, _ ->
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Saving profile...")
            progressDialog.show()

            val userMap = HashMap<String, Any>()
            userMap["phone"] = phoneNumber
            userMap["address"] = address

            userReference.updateChildren(userMap).addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                    // Send result back to SettingsFragment
                    val resultIntent = Intent()
                    resultIntent.putExtra("phone", phoneNumber)
                    resultIntent.putExtra("address", address)
                    setResult(Activity.RESULT_OK, resultIntent)

                    finish() // Close activity and return to previous screen
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.create().show()
    }

    // Retrieve and display updated profile data in the CardView
    private fun displayUpdatedProfile() {
        userReference.get().addOnSuccessListener { snapshot ->
            val phone = snapshot.child("phone").value.toString()
            val address = snapshot.child("address").value.toString()

            phoneEditText.setText(phone) // Set existing phone number in EditText
            addressEditText.setText(address) // Set existing address in EditText
            findViewById<TextView>(R.id.profile_phone).text = "Phone No.: $phone"
            findViewById<TextView>(R.id.profile_address).text = "Address: $address"
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
        }
    }
}
