package com.example.emis

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.emis.Fragments.ChatFragment
import com.example.emis.Fragments.DashboardFragment
import com.example.emis.Fragments.SearchFragment
import com.example.emis.Fragments.SettingsFragment
import com.example.emis.ModeClasses.Chat
import com.example.emis.ModeClasses.Users
import com.example.emis.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private var refUser: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null

    // Declare binding variables
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        getFCMToken()
        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUser = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val tabLayout: TabLayout = binding.tabLayout
        val viewPager: ViewPager = binding.viewpager

//
//        // Adding fragments to the adapter

        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

                var countUnreadMessages = 0

                for (dataSnapshot in snapshot.children){

                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(firebaseUser!!.uid)&& !chat.isIsSeen())
                    {
                        countUnreadMessages += 1
                    }
                }
                viewPagerAdapter.addFragment(DashboardFragment(), "Dashboard")

                if (countUnreadMessages == 0)

                {
                    viewPagerAdapter.addFragment(ChatFragment(), "Chat")
                }
                else{
                    viewPagerAdapter.addFragment(ChatFragment(), "($countUnreadMessages)Chat")
                }


            viewPagerAdapter.addFragment(SearchFragment(), "Search")
            viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

            viewPager.adapter = viewPagerAdapter
            tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.usernames.text = "Loading..."

        // Retrieve user data from Firebase
        refUser!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user: Users? = dataSnapshot.getValue(Users::class.java)

                    binding.usernames.text = user!!.getUSERNAME()
                    Picasso.get().load(user.getPROFILE()).placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.profileImage)
                }
                }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to read value.", error.toException())
            }
        })
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    private fun updatesStatus(status: String)
    {
       val ref =FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()

        updatesStatus("online")
    }

    override fun onPause() {
        super.onPause()

        updatesStatus("offline")
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            sendFCMTokenToServer(token)
        }
    }

    private fun sendFCMTokenToServer(token: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userRef.child("fcmToken").setValue(token)
    }
}