package com.example.emis

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.Adapter.ChatsAdapter
import com.example.emis.ModeClasses.Chat
import com.example.emis.ModeClasses.Users
import com.example.emis.databinding.ActivityMessageChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageChatBinding
    private var userIdVisit: String = ""
    private var firebaseUser: FirebaseUser? = null
    private var imageUri: Uri? = null
    private var chatsAdapter: ChatsAdapter? = null
    private var mChatList: MutableList<Chat> = mutableListOf()
    private var reference: DatabaseReference? = null
    lateinit var recycler_view_chats: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressed()  // Handle back navigation
        }

        userIdVisit = intent.getStringExtra("Visit_id") ?: ""
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager

        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return // Stop execution if no user is logged in
        }

        val chatUserReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)

        chatUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatUser: Users? = snapshot.getValue(Users::class.java)
                if (chatUser != null) {
                    binding.usernameChat.text = chatUser.getUSERNAME()
                    if (chatUser.getPROFILE() != null && chatUser.getPROFILE()!!.isNotEmpty()) {
                        Picasso.get().load(chatUser.getPROFILE()).into(binding.profileImageChat)
                    } else {
                        binding.profileImageChat.setImageResource(R.drawable.ic_profile)
                    }
                }
                retrieveMessages(firebaseUser!!.uid, userIdVisit, chatUser!!.getPROFILE())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageChatActivity, "Failed to load chat user data", Toast.LENGTH_SHORT).show()
            }
        })

        binding.sendMessageBtn.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this@MessageChatActivity, "Please write a message first...", Toast.LENGTH_SHORT).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            binding.textMessage.setText("") // Clear the message input
        }

        binding.attachImageFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        binding.attachFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*" // Allows any type of file
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Pick Document"), 439)
        }

        seenMessage(userIdVisit)
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message // Set the message field to message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        messageHashMap["messageType"] = "text" // Default message type

        reference.child("Chats").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateChatList(senderId, receiverId)
                }
            }
    }

    private fun updateChatList(senderId: String, receiverId: String?) {
        val chatListReference = FirebaseDatabase.getInstance()
            .reference.child("ChatList")
            .child(senderId)
            .child(receiverId!!)

        chatListReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatListReference.child("id").setValue(receiverId)
                }
                val chatListReceiverRef = FirebaseDatabase.getInstance()
                    .reference.child("ChatList")
                    .child(receiverId)
                    .child(senderId)
                chatListReceiverRef.child("id").setValue(senderId)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {
            sendImageMessage(data.data!!)
        } else if (requestCode == 439 && resultCode == RESULT_OK && data != null && data.data != null) {
            sendDocumentMessage(data.data!!)
        }
    }

    private fun sendImageMessage(imageUri: Uri) {
        val loadingBar = ProgressDialog(this)
        loadingBar.setMessage("Please wait, image is sending.....")
        loadingBar.show()

        val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
        val ref = FirebaseDatabase.getInstance().reference
        val messageId = ref.push().key
        val filePath = storageReference.child("$messageId.jpg")

        val uploadTask = filePath.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            return@continueWithTask filePath.downloadUrl // Get the URL after upload
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()

                val messageHashMap = HashMap<String, Any?>()
                messageHashMap["sender"] = firebaseUser!!.uid
                messageHashMap["message"] = "sent an image."
                messageHashMap["receiver"] = userIdVisit
                messageHashMap["isseen"] = false
                messageHashMap["url"] = url // Store the image URL
                messageHashMap["messageId"] = messageId
                messageHashMap["messageType"] = "image" // Set message type to image

                ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loadingBar.dismiss()
                        }
                    }
            }
        }
    }

        private fun sendDocumentMessage(documentUri: Uri) {
            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Please wait, document is uploading...")
            loadingBar.show()

            val fileName = getFileName(documentUri)
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Documents")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.$fileName")

            filePath.putFile(documentUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                filePath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    sendFileMessage(messageId!!, downloadUrl, fileName)
                    loadingBar.dismiss()
                } else {
                    loadingBar.dismiss()
                    Toast.makeText(this, "Failed to upload document", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun sendFileMessage(messageId: String, fileUrl: String, fileName: String) {
            val messageHashMap = HashMap<String, Any?>()
            messageHashMap["sender"] = firebaseUser!!.uid
            messageHashMap["message"] = "sent a file"
            messageHashMap["receiver"] = userIdVisit
            messageHashMap["isseen"] = false
            messageHashMap["url"] = fileUrl
            messageHashMap["messageId"] = messageId
            messageHashMap["fileName"] = fileName
            messageHashMap["messageType"] = "file"

            FirebaseDatabase.getInstance().reference.child("Chats").child(messageId)
                .setValue(messageHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateChatList(firebaseUser!!.uid, userIdVisit)
                    }
                }
        }

        private fun getFileName(uri: Uri): String {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result
        }
    


    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()

                for (snapshot in snapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)

                    if (chat != null && ((chat.getReceiver() == senderId && chat.getSender() == receiverId) ||
                                (chat.getReceiver() == receiverId && chat.getSender() == senderId))) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                }

                chatsAdapter = ChatsAdapter(this@MessageChatActivity, mChatList, receiverImageUrl!!)
                recycler_view_chats.adapter = chatsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageChatActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat != null && chat.getReceiver() == userIdVisit && chat.getSender() == userId) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        snapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
