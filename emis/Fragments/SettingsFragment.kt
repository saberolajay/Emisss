package com.example.emis.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.emis.EditProfileActivity
import com.example.emis.LoginActivity
import com.example.emis.ModeClasses.Users
import com.example.emis.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class SettingsFragment : Fragment() {

    private var userReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private lateinit var btnLogOut: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var usernameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var coverPhotoView: ImageView
    private lateinit var facebookImageView: ImageView
    private lateinit var instagramImageView: ImageView
    private val RequestCode = 438
    private var imageURI: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = null
    private var socialChecker: String? = null
    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var userAddressTextView: TextView

    private val profileUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the profile details in the UI
            fetchUserProfileData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views
        usernameTextView = binding.findViewById(R.id.username_settings)
        profileImageView = binding.findViewById(R.id.profile_image)
        facebookImageView = binding.findViewById(R.id.set_facebook)
        instagramImageView = binding.findViewById(R.id.set_instagram)
        coverPhotoView = binding.findViewById(R.id.cover_photo)
        btnLogOut = binding.findViewById(R.id.btnLog_out)
        userPhoneTextView = binding.findViewById(R.id.profile_phone)
        userAddressTextView = binding.findViewById(R.id.profile_address)

        // Initialize Firebase user and reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        // Register the profile update receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(profileUpdateReceiver, IntentFilter("PROFILE_UPDATED"))

        // Set up Firebase listener to fetch user data
        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (user != null) {
                        usernameTextView.text = user.getUSERNAME()

                        // Set profile image
                        val profileUrl = user.getPROFILE()
                        if (!profileUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileUrl).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.profile)
                        }

                        // Set cover image
                        val coverUrl = user.getCOVER()
                        if (!coverUrl.isNullOrEmpty()) {
                            Picasso.get().load(coverUrl).into(coverPhotoView)
                        } else {
                            coverPhotoView.setImageResource(R.drawable.cover_p)
                        }

                        // Fetch and display user profile details
                        fetchUserProfileData()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsFragment", "Firebase data fetch error: ${error.message}")
            }
        })

        val editProfileButton: ImageView = binding.findViewById(R.id.editprofile)

        // Set an onClickListener to navigate to EditProfileActivity
        editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Make profile image clickable
        profileImageView.setOnClickListener {
            coverChecker = "profile"
            pickImage()
            Toast.makeText(context, "Profile image clicked", Toast.LENGTH_SHORT).show()
        }

        coverPhotoView.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }

        facebookImageView.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }

        instagramImageView.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }

        // Initialize Firebase Auth and SharedPreferences
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Set onClickListener to handle logout
        btnLogOut.setOnClickListener {
            showLogOutConfirmationDialog()
        }

        // Check if the Intent contains the updated profile data
        val intent = activity?.intent
        if (intent != null && intent.hasExtra("role") && intent.hasExtra("phone") && intent.hasExtra("address")) {
            val phone = intent.getStringExtra("phone")
            val address = intent.getStringExtra("address")

            // Update the views in SettingsFragment with the new data
            userPhoneTextView.text = "Phone No.: $phone"
            userAddressTextView.text = "Address: $address"
        }

        return binding
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the profile update receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileUpdateReceiver)
    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext())
        builder.setTitle("Enter your username:")
        val editText = EditText(context)

        editText.hint = if (socialChecker == "facebook") "e.g. Kurumi Tokisaki" else "e.g. Kurumi Tokisaki"
        builder.setView(editText)

        builder.setPositiveButton("Create") { dialog, which ->
            val str = editText.text.toString()
            if (str.isEmpty()) {
                Toast.makeText(context, "Please write something...", Toast.LENGTH_SHORT).show()
            } else {
                saveSocialLinks(str)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun saveSocialLinks(str: String) {
        val mapSocial = HashMap<String, Any>()
        when (socialChecker) {
            "facebook" -> mapSocial["facebook"] = "https://m.facebook.com/$str"
            "instagram" -> mapSocial["instagram"] = "https://m.instagram.com/$str"
        }

        userReference!!.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageURI = data.data
            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait...")
        progressBar.show()

        if (imageURI != null) {
            try {
                // Get bitmap from URI
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageURI)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, false) // Resize the image

                val uri = getImageUri(context, resizedBitmap)
                val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                val uploadTask = fileRef.putFile(uri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        updateProfileImage(downloadUri)
                    }
                }.addOnFailureListener { e ->
                    Log.e("SettingsFragment", "Image upload failed: ${e.message}")
                    progressBar.dismiss()
                }

            } catch (e: Exception) {
                Log.e("SettingsFragment", "Error occurred while uploading image: ${e.message}")
                progressBar.dismiss()
            }
        }
    }

    private fun updateProfileImage(downloadUri: String) {
        val mapProfile = HashMap<String, Any>()
        if (coverChecker == "profile") {
            mapProfile["profile"] = downloadUri
        } else if (coverChecker == "cover") {
            mapProfile["cover"] = downloadUri
        }

        userReference!!.updateChildren(mapProfile).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getImageUri(inContext: Context?, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext?.contentResolver, inImage, "Title", null)
        return Uri.parse(path!!)
    }

    private fun fetchUserProfileData() {
        userReference!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val phone = snapshot.child("phone").value.toString()
                    val address = snapshot.child("address").value.toString()
                    userPhoneTextView.text = "Phone No.: $phone"
                    userAddressTextView.text = "Address: $address"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsFragment", "Error fetching profile data: ${error.message}")
            }
        })
    }

    private fun showLogOutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                firebaseAuth.signOut()
                sharedPreferences.edit().clear().apply()
                startActivity(Intent(context, LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
            .show()
    }
}
