package com.example.emis.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.ModeClasses.Chat
import com.example.emis.R
import com.example.emis.ViewFullImageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    private val mContext: Context,
    private val mChatList: MutableList<Chat>,
    private val imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 1) R.layout.message_item_right else R.layout.message_item_left
        val view = LayoutInflater.from(mContext).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mChatList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]

        // Load profile image
        Picasso.get()
            .load(imageUrl)
            .into(holder.profile_image)

        // Check if the message is an image
        if (chat.getMessage() == "sent an image." && chat.getUrl().isNotEmpty()) {
            holder.show_text_message?.visibility = View.GONE
            holder.left_image_view?.visibility = View.GONE
            holder.right_image_view?.visibility = View.VISIBLE
            holder.right_image_view?.loadImage(chat.getUrl())
            holder.right_image_view?.setOnClickListener { showImageOptions(chat, position, holder, true) }
        }
        // Check if the message is a file
        else if (chat.getMessage() == "sent a file." && chat.getFileUrl().isNotEmpty()) {
            holder.show_text_message?.visibility = View.GONE
            holder.left_image_view?.visibility = View.GONE
            holder.right_image_view?.visibility = View.GONE

            // Display file message with file name as a clickable link
            holder.show_file_message?.apply {
                visibility = View.VISIBLE
                text = chat.getFileName() // Display the file name directly
                setTextColor(mContext.resources.getColor(android.R.color.holo_blue_dark)) // Style as a link
                setOnClickListener { downloadFile(chat.getFileUrl()) }
            }
        }
        // Check if the message is a document
        else if (chat.getMessage() == "sent a document." && chat.getFileUrl().isNotEmpty()) {
            holder.show_text_message?.visibility = View.GONE
            holder.left_image_view?.visibility = View.GONE
            holder.right_image_view?.visibility = View.GONE

            // Display document message with document name as a clickable link
            holder.show_file_message?.apply {
                visibility = View.VISIBLE
                text = chat.getFileName() // Display the document name directly
                setTextColor(mContext.resources.getColor(android.R.color.holo_blue_dark)) // Style as a link
                setOnClickListener { downloadFile(chat.getFileUrl()) } // Open the document
            }
        } else {
            // Text message handling
            holder.show_text_message?.apply {
                visibility = View.VISIBLE
                text = chat.getMessage()
                setOnClickListener { showTextOptions(chat, position, holder) }
            }
            holder.left_image_view?.visibility = View.GONE
            holder.right_image_view?.visibility = View.GONE
        }

        // Handle seen/sent status for the last message
        if (position == mChatList.size - 1) {
            holder.text_seen?.apply {
                visibility = View.VISIBLE
                text = if (chat.isIsSeen()) "Seen" else "Sent"

                // Adjust layout for image messages
                if (chat.getMessage() == "sent an image." && chat.getUrl().isNotEmpty()) {
                    val lp = layoutParams as RelativeLayout.LayoutParams
                    lp.setMargins(0, 245, 10, 0) // Adjust positioning
                    layoutParams = lp
                }
            }
        } else {
            holder.text_seen?.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender() == firebaseUser?.uid) 1 else 0
    }

    // Function to load image into ImageView
    private fun ImageView.loadImage(url: String) {
        Picasso.get()
            .load(url)
            .resize(500, 500)
            .centerInside()
            .into(this)
    }

    private fun showImageOptions(chat: Chat, position: Int, holder: ViewHolder, isSender: Boolean) {
        val options = if (isSender) {
            arrayOf<CharSequence>("View Full Image", "Delete Image", "Cancel")
        } else {
            arrayOf<CharSequence>("View Full Image", "Cancel")
        }

        AlertDialog.Builder(mContext)
            .setTitle("What do you want?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(mContext, ViewFullImageActivity::class.java)
                        intent.putExtra("url", chat.getUrl())
                        mContext.startActivity(intent)
                    }
                    1 -> if (isSender) deleteSentMessage(position, holder)
                }
            }
            .show()
    }

    private fun showTextOptions(chat: Chat, position: Int, holder: ViewHolder) {
        val options = if (chat.getSender() == firebaseUser?.uid) {
            arrayOf<CharSequence>("Delete Message", "Cancel")
        } else {
            arrayOf<CharSequence>("Remove Message", "Cancel")
        }

        AlertDialog.Builder(mContext)
            .setTitle("What do you want?")
            .setItems(options) { _, which ->
                if (which == 0) {
                    if (chat.getSender() == firebaseUser?.uid) {
                        deleteSentMessage(position, holder)
                    } else {
                        deleteReceivedMessage(position, holder)
                    }
                }
            }
            .show()
    }

    private fun deleteSentMessage(position: Int, holder: ViewHolder) {
        val chat = mChatList[position]
        mChatList.removeAt(position)
        notifyItemRemoved(position)
        FirebaseDatabase.getInstance().reference
            .child("Chats")
            .child(chat.getMessageId())
            .removeValue()
        Toast.makeText(holder.itemView.context, "Message deleted", Toast.LENGTH_SHORT).show()
    }

    private fun deleteReceivedMessage(position: Int, holder: ViewHolder) {
        val chat = mChatList[position]
        mChatList.removeAt(position)
        notifyItemRemoved(position)
        FirebaseDatabase.getInstance().reference
            .child("Chats")
            .child(chat.getMessageId())
            .removeValue()
        Toast.makeText(holder.itemView.context, "Message deleted", Toast.LENGTH_SHORT).show()
    }

    // Method to download files
    private fun downloadFile(fileUrl: String?) {
        if (fileUrl != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(fileUrl), "*/*") // Use appropriate MIME type
            mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: CircleImageView? = null
        var show_text_message: TextView? = null
        var left_image_view: ImageView? = null
        var right_image_view: ImageView? = null
        var show_file_message: TextView? = null // TextView for displaying file messages
        var text_seen: TextView? = null

        init {
            profile_image = itemView.findViewById(R.id.profile_image)
            show_text_message = itemView.findViewById(R.id.show_text_message)
            left_image_view = itemView.findViewById(R.id.left_image_view)
            right_image_view = itemView.findViewById(R.id.right_image_view)
            show_file_message = itemView.findViewById(R.id.show_file_message) // Initialize the file message TextView
            text_seen = itemView.findViewById(R.id.text_seen)
        }
    }
}
