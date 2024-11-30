package com.example.emis.AdapterClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.EventAppointmentActivity
import com.example.emis.MessageChatActivity
import com.example.emis.ModeClasses.Chat
import com.example.emis.ModeClasses.Users
import com.example.emis.R
import com.example.emis.VisitUserProfile
import com.example.emis.databinding.UserSearchItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(
    private val mContext: Context,
    private val mUsers: List<Users>,
    private val isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserSearchItemBinding.inflate(
            LayoutInflater.from(mContext),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUsers[position]
        holder.bind(user)
    }

    inner class ViewHolder(private val binding: UserSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: Users) {
            // Access the views using holder
            val holder = this

            holder.binding.username.text = user.getUSERNAME()

            // Load the profile image using Picasso
            Picasso.get().load(user.getPROFILE())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(holder.binding.profileImage)

            // Handle online/offline visibility
            if (isChatCheck) {
                if (user.getSTATUS() == "online") {
                    holder.binding.imageOnline.visibility = View.VISIBLE
                    holder.binding.imageOffline.visibility = View.GONE
                } else {
                    holder.binding.imageOnline.visibility = View.GONE
                    holder.binding.imageOffline.visibility = View.VISIBLE
                }
                // Retrieve last message if chat is checked
                retrieveLastMessage(user.getUID(), holder.binding.messageLast)
            } else {
                holder.binding.imageOnline.visibility = View.GONE
                holder.binding.imageOffline.visibility = View.GONE
                holder.binding.messageLast.visibility = View.GONE
            }

            // Set onClick listener to show options in the dialog
            holder.binding.root.setOnClickListener {
                val options = arrayOf<CharSequence>(
                    "Send Message",
                    "Visit Profile",
                    "Set Appointment"
                )
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("What do you want?")
                builder.setItems(options) { _, selectedPosition ->
                    when (selectedPosition) {
                        0 -> {
                            val userId = user.getUID()
                            val intent = Intent(mContext, MessageChatActivity::class.java)
                            intent.putExtra("Visit_id", userId)
                            mContext.startActivity(intent)
                        }
                        1 -> {
                            val userId = user.getUID()
                            val intent = Intent(mContext, VisitUserProfile::class.java)
                            intent.putExtra("Visit_id", userId)
                            mContext.startActivity(intent)
                        }
                        2 -> {
                            val userId = user.getUID()
                            val intent = Intent(mContext, EventAppointmentActivity::class.java)
                            intent.putExtra("Visit_id", userId)
                            mContext.startActivity(intent)
                        }

                    }
                }
                builder.show()
            }
        }


        private fun retrieveLastMessage(chatUserId: String, lastMessageTxt: TextView) {
            // Firebase logic to fetch the last message
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().reference.child("Chats")

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastMsgTemp = "defaultMsg" // Temporary variable to hold the last message

                    for (dataSnapshot in snapshot.children) {
                        val chat: Chat? = dataSnapshot.getValue(Chat::class.java)
                        if (firebaseUser != null && chat != null) {
                            if ((chat.getReceiver() == firebaseUser!!.uid && chat.getSender() == chatUserId) ||
                                (chat.getReceiver() == chatUserId && chat.getSender() == firebaseUser!!.uid)) {
                                lastMsgTemp = chat.getMessage()!!
                            }
                        }
                    }

                    // Update the UI based on the last message
                    when (lastMsgTemp) {
                        "defaultMsg" -> lastMessageTxt.text = "No Message"
                        "sent you an image." -> lastMessageTxt.text = "Image Sent"
                        else -> lastMessageTxt.text = lastMsgTemp
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error here
                }
            })
        }
    }
}
