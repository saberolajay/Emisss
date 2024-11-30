package com.example.emis

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {

    private lateinit var imageViewer: ImageView
    private var imageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        // Retrieve the ImageView from layout
        imageViewer = findViewById(R.id.image_viewer)

        // Retrieve the image URL passed through the intent
        imageUri = intent.getStringExtra("url")

        // Load the image using Picasso with error handling
        if (imageUri != null) {
            Picasso.get()
                .load(imageUri)
                .fit() // Automatically scale the image to fit the ImageView dimensions
                .centerInside() // Maintain the aspect ratio
                .into(imageViewer, object : Callback {
                    override fun onSuccess() {
                        // Optional: Actions after successful loading
                    }

                    override fun onError(e: Exception?) {
                        Toast.makeText(this@ViewFullImageActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if no valid URL is provided
        }
    }
}
