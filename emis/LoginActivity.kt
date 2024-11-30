package com.example.emis

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.emis.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inflate the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Login button click listener
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Set up Forgot Password button click listener
        binding.tvForgotPassword.setOnClickListener {
            // Handle forgot password flow (e.g., send reset email)
            handleForgotPassword()
        }
    }

    private fun loginUser() {
        val email = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        // Validate input fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show the ProgressBar
        binding.progressBar.visibility = android.view.View.VISIBLE

        // Sign in user with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Hide the ProgressBar after task is completed
                binding.progressBar.visibility = android.view.View.GONE

                if (task.isSuccessful) {
                    // After successful login, redirect to main screen
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish() // Close LoginActivity
                } else {
                    // If login fails, show a toast with error message
                    Toast.makeText(this@LoginActivity, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleForgotPassword() {
        val email = binding.etUsername.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

