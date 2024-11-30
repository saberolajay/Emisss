package com.example.emis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.emis.databinding.ActivityChooseBinding
import com.google.firebase.auth.FirebaseAuth

class ChooseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is logged in, redirect to MainActivity
            val intent = Intent(this@ChooseActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Close ChooseActivity so it doesn't stay in the back stack
        }

        // Button listeners for navigation to Login or Register
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this@ChooseActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this@ChooseActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
