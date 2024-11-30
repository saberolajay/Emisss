package com.example.emis.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.emis.QrCodeActivity
import com.example.emis.R
import com.example.emis.StudentListActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var slideshowImage: ImageView
    private val imageList = listOf(
        R.drawable.school, // Replace with your image resources
        R.drawable.slide,
        R.drawable.slide2
    )
    private var currentImageIndex = 0
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnPreviousMonth: View
    private lateinit var btnNextMonth: View
    private lateinit var calendarGrid: GridLayout
    private val calendar: Calendar = Calendar.getInstance()
    private val events = mutableMapOf<String, String>() // Store event title by date

    private val slideshowRunnable = object : Runnable {
        override fun run() {
            slideshowImage.alpha = 0f
            slideshowImage.setImageResource(imageList[currentImageIndex])
            slideshowImage.animate().alpha(1f).setDuration(500).start()

            slideshowImage.postDelayed(this, 3000)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // QR Code setup
        val qrCodeCard = view.findViewById<CardView>(R.id.qrCodeCard)
        qrCodeCard.setOnClickListener {
            val intent = Intent(requireContext(), QrCodeActivity::class.java)
            startActivity(intent)
        }
            val bmiCard = view.findViewById<CardView>(R.id.bmi)
            bmiCard.setOnClickListener {
                val intent = Intent(requireContext(), StudentListActivity::class.java)
                startActivity(intent)
        }

        // Slideshow setup
        slideshowImage = view.findViewById(R.id.slideshowImage)
        startSlideshow()

        // Calendar setup
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        calendarGrid = view.findViewById(R.id.calendarGrid)

        // Fetch events from Firebase
        fetchEventsFromDatabase()

        updateCalendar()

        // Navigation buttons
        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        return view
    }

    private fun fetchEventsFromDatabase() {
        val database = FirebaseDatabase.getInstance()
        val eventsRef = database.getReference("events")

        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                events.clear() // Clear previous events
                for (eventSnapshot in snapshot.children) {
                    val date = eventSnapshot.child("date").getValue(String::class.java)
                    val title = eventSnapshot.child("title").getValue(String::class.java)
                    if (date != null && title != null) {
                        events[date] = title // Store event by date
                    }
                }
                updateCalendar() // Update calendar with fetched events
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    private fun updateCalendar() {
        // Update the month title
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvCurrentMonth.text = dateFormat.format(calendar.time)

        // Clear previous views from the GridLayout
        calendarGrid.removeAllViews()

        // Add day headers
        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (day in daysOfWeek) {
            val dayHeader = TextView(requireContext()).apply {
                text = day
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // Use weight to fill the space
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute space evenly
                }
            }
            calendarGrid.addView(dayHeader)
        }

        // Generate dates for the calendar
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        for (i in 0 until 42) { // 6 rows, 7 columns
            val dayView = TextView(requireContext()).apply {
                text = monthCalendar.get(Calendar.DAY_OF_MONTH).toString()
                textSize = 16f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(8, 8, 8, 8)

                // Highlight the current date
                if (monthCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    monthCalendar.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                ) {
                    setBackgroundResource(R.drawable.current_date_highlight) // Custom background for current date
                }

                // Adjust text color for days outside the current month
                if (monthCalendar.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.card_background))
                }

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // Use weight to fill the space
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Distribute space evenly
                }
            }
            calendarGrid.addView(dayView)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }


    private fun startSlideshow() {
        slideshowImage.postDelayed(object : Runnable {
            override fun run() {
                slideshowImage.animate().alpha(0f).setDuration(500).withEndAction {
                    currentImageIndex = (currentImageIndex + 1) % imageList.size
                    slideshowImage.setImageResource(imageList[currentImageIndex])
                    slideshowImage.animate().alpha(1f).setDuration(500).start()
                }

                slideshowImage.postDelayed(this, 3000)
            }
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        slideshowImage.removeCallbacks(slideshowRunnable)
    }
}
