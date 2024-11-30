package com.example.emis

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.emis.databinding.ActivityScreenBinding

class ScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val seekBar = binding.slideSeekBar
        val slideStatus = binding.slideStatus

        // Set up SeekBar listener to trigger the transition when fully slid
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the slide status as the progress changes
                slideStatus.text = "Sliding... $progress%"
                // Automatically move to next screen when SeekBar reaches max value
                if (progress == seekBar!!.max) {
                    navigateToSelectActivity()  // Go to ChooseActivity
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // Navigate to ChooseActivity
    private fun navigateToSelectActivity() {
        val intent = Intent(this, ChooseActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish the current activity so the user can't go back
    }
}
