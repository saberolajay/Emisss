package com.example.emis.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.AdapterClasses.UserAdapter
import com.example.emis.ModeClasses.Chatlist
import com.example.emis.ModeClasses.Users
import com.example.emis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class ChatFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users> = ArrayList()
    private var usersChatList: List<Chatlist> = ArrayList()
    lateinit var recycler_view_chatlist: RecyclerView
    private var firebaseUser: FirebaseUser?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val  view = inflater.inflate(R.layout.fragment_chat, container, false)

        recycler_view_chatlist = view.findViewById(R.id.recycler_view_chatlist)
        recycler_view_chatlist.setHasFixedSize(true)
        recycler_view_chatlist.layoutManager =LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (usersChatList as ArrayList).clear()

                for (dataSnapshot in snapshot.children)
                {
                    val  chatlist = dataSnapshot.getValue(Chatlist::class.java)

                    (usersChatList as ArrayList).add (chatlist!!)
                }
                retrieveChatLists()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        return view
    }

    private fun retrieveChatLists(){

        mUsers = ArrayList()

        val  ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (mUsers as ArrayList).clear()

                for (dataSnapshot in snapshot.children){
                    val user = dataSnapshot.getValue(Users::class.java)

                    for (eachChatList in usersChatList!!){
                        if (user!!.getUID().equals(eachChatList.getId())){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true)
                recycler_view_chatlist.adapter =userAdapter
            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        })

    }
}
