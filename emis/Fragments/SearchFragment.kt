package com.example.emis.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.emis.AdapterClasses.UserAdapter
import com.example.emis.ModeClasses.Users
import com.example.emis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: MutableList<Users> = ArrayList()
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null

    // Variables to store Firebase listeners for later removal
    private var retrieveUsersListener: ValueEventListener? = null
    private var searchUsersListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        mUsers = ArrayList()
        retrieveAllUsers()

        recyclerView = view.findViewById(R.id.search_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        // Initialize EditText for searching
        searchEditText = view.findViewById(R.id.searchUserSet)

        // Add TextWatcher to searchEditText
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun retrieveAllUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val refUser = FirebaseDatabase.getInstance().reference.child("Users")

        // Define and assign listener to retrieveUsersListener for later removal
        retrieveUsersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return  // Ensure the fragment is attached
                mUsers.clear()
                if (searchEditText?.text.toString().isEmpty()) {
                    for (Snapshot in snapshot.children) {
                        val user: Users? = Snapshot.getValue(Users::class.java)
                        if (user != null && user.getUID() != firebaseUser) {
                            mUsers.add(user)
                        }
                    }
                    userAdapter = UserAdapter(requireContext(), mUsers, false)
                    recyclerView!!.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any database errors here
            }
        }
        refUser.addValueEventListener(retrieveUsersListener!!)
    }

    private fun searchForUsers(str: String) {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        // Define and assign listener to searchUsersListener for later removal
        searchUsersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return  // Ensure the fragment is attached
                mUsers.clear()
                for (Snapshot in snapshot.children) {
                    val user: Users? = Snapshot.getValue(Users::class.java)
                    if (user != null && user.getUID() != firebaseUserID) {
                        mUsers.add(user)
                    }
                }
                userAdapter = UserAdapter(requireContext(), mUsers, false)
                recyclerView!!.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any database errors here
            }
        }
        queryUsers.addValueEventListener(searchUsersListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firebase listeners to prevent memory leaks
        retrieveUsersListener?.let { FirebaseDatabase.getInstance().reference.child("Users").removeEventListener(it) }
        searchUsersListener?.let { FirebaseDatabase.getInstance().reference.child("Users").removeEventListener(it) }
    }
}
