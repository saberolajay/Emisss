
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val dates = mutableListOf<Date>()
    private var currentDate: Date? = null
    private val events = mutableMapOf<String, Boolean>()

    fun setDates(dates: List<Date>, currentDate: Date) {
        this.dates.clear()
        this.dates.addAll(dates)
        this.currentDate = currentDate
        fetchEvents()
        notifyDataSetChanged()
    }

    private fun fetchEvents() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                events.clear()
                for (dateSnapshot in snapshot.children) {
                    events[dateSnapshot.key ?: ""] = true
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle Firebase errors here
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = dates[position]
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        holder.tvDate.text = dayFormat.format(date)

        // Highlight today's date
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)

        if (dateKey == today) {
            holder.tvDate.setBackgroundColor(Color.parseColor("#FF6B6B")) // Highlight color
            holder.tvDate.setTextColor(Color.WHITE)
        } else {
            holder.tvDate.setBackgroundColor(Color.TRANSPARENT)
            holder.tvDate.setTextColor(Color.BLACK)
        }

        // Mark event dates
        if (events[dateFormat.format(date)] == true) {
            holder.tvDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.event_marker)
        } else {
            holder.tvDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    override fun getItemCount(): Int = dates.size

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }
}
