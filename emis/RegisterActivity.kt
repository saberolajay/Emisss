package com.example.emis

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.emis.databinding.ActivityRegisterBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inflate the layout using ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Register button click listener
        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = binding.registerUsername.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    firebaseUserId = auth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users")
                        .child(firebaseUserId)

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseUserId
                    userHashMap["username"] = username
                    userHashMap["profile"] =
                        "https://firebasestorage.googleapis.com/v0/b/emis-application-1.appspot.com/o/profile.jpg?alt=media&token=d89230ef-e96a-4834-a713-4131d9fdf7fd"
                    userHashMap["cover"] =
                        "https://firebasestorage.googleapis.com/v0/b/emis-application-1.appspot.com/o/cover_p.jpg?alt=media&token=86f35188-28a8-446d-8109-e31cee1efcc4"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.toLowerCase()
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.facebook.com"

                    refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Show success message
                            Toast.makeText(
                                this@RegisterActivity,
                                "Successfully Registered",
                                Toast.LENGTH_SHORT
                            ).show()

                            // After successful registration, redirect to LoginActivity
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish() // Close RegisterActivity
                        }
                    }

                } else {
                    // If registration fails, show a toast with error message
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error Message: " + task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
