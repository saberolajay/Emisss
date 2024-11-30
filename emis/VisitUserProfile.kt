package com.example.emis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.emis.ModeClasses.Users
import com.example.emis.databinding.ActivityVisitUserProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class VisitUserProfile : AppCompatActivity() {

    private lateinit var binding: ActivityVisitUserProfileBinding
    private var userVisitId: String = ""
    var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityVisitUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the user ID passed from the previous activity
        userVisitId = intent.getStringExtra("Visit_id")!!

        // Fetch user details from Firebase
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(Users::class.java)

                    // Display user information
                    user?.let {
                        binding.usernameDisplay.text = it.getUSERNAME()
                        Picasso.get().load(it.getPROFILE()).into(binding.profileDisplay)
                        Picasso.get().load(it.getCOVER()).into(binding.coverDisplay)

                        // Fetch and display phone and address
                        binding.phoneNumberDisplay.text = "Phone: ${it.getPHONE()}"  // Assuming you have a method to get phone number
                        binding.address.text = "Address: ${it.getADDRESS()}"  // Assuming you have a method to get address
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors with the database retrieval
            }
        })

        // Facebook link click listener
        binding.setFacebookDisplay.setOnClickListener {
            user?.let {
                val uri = Uri.parse(it.getFACEBOOK())
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        // Instagram link click listener
        binding.setInstagramDisplay.setOnClickListener {
            user?.let {
                val uri = Uri.parse(it.getINSTAGRAM())
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        // Send message click listener
        binding.sendMessage.setOnClickListener {
            user?.let {
                val userId = it.getUID()
                val intent = Intent(this@VisitUserProfile, MessageChatActivity::class.java)
                intent.putExtra("Visit_id", userId)
                startActivity(intent)
            }
        }
    }
}
