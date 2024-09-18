package com.example.youthsync

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import com.example.youthsync.databinding.FragmentAdminHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AdminHomeFragment : Fragment() {

    private lateinit var binding: FragmentAdminHomeBinding
    private lateinit var firebaseRLDB: DatabaseReference
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firebaseRLDB = FirebaseDatabase.getInstance().getReference("Announcements")
        firestoreDB = FirebaseFirestore.getInstance()

        //function calls
        fetchName()
        fetchAllAnnouncements()

        binding.btnPost.setOnClickListener {
            val announcement = binding.createAnnouncement.text.toString()
            val user = auth.currentUser

            if (announcement.isNotEmpty() && user != null) {
                val counterRef =
                    FirebaseDatabase.getInstance().getReference("Counter/announcements")
                val announcementsRef = FirebaseDatabase.getInstance().getReference("Announcements")

                counterRef.get().addOnSuccessListener { snapshot ->
                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                    val newAnnouncementKey = "announcement${currentCount + 1}"

                    val announcementData = mapOf(
                        "announcement" to announcement,
                        "uid" to user.uid,
                        "timestamp" to System.currentTimeMillis()
                    )
                    announcementsRef.child(newAnnouncementKey).setValue(announcementData)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                counterRef.setValue(currentCount + 1)
                                Toast.makeText(
                                    requireContext(),
                                    "Announcement posted successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.createAnnouncement.setText("")
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to post announcement!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch counter value",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter an announcement!", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    private fun fetchName() {
        val user = auth.currentUser
        if (user != null) {
            val userID = user.uid
            val ref = firestoreDB.collection("users").document(userID)
            ref.get().addOnSuccessListener {
                if (it != null && it.exists()) {
                    val fName = it.getString("firstName")
                    binding.upperName.text = "Hello, $fName"
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_LONG)
                        .show()
                }
            }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to fetch name", Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchAllAnnouncements() {
        firebaseRLDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.announcementsContainer.removeAllViews()

                val announcementsList = mutableListOf<Pair<String, Map<String, Any>>>()
                for (announcementSnapshot in snapshot.children) {
                    val announcementData = announcementSnapshot.getValue<Map<String, Any>>()
                    val announcementKey = announcementSnapshot.key ?: ""
                    announcementsList.add(Pair(announcementKey, announcementData ?: emptyMap()))
                }

                // Sort announcements based on the key
                val sortedAnnouncements = announcementsList.sortedByDescending {
                    it.first.removePrefix("announcement").toIntOrNull() ?: 0
                }

                for ((_, announcementData) in sortedAnnouncements) {
                    val announcementText = announcementData["announcement"] as? String
                    val userUid = announcementData["uid"] as? String
                    val timestamp = announcementData["timestamp"] as? Long

                    val formattedTime = timestamp?.let {
                        val date = Date(it)
                        DateFormat.format("MM-dd-yy hh:mm a", date).toString()
                    } ?: "Unknown time"

                    val userRef = firestoreDB.collection("users").document(userUid ?: "")
                    userRef.get().addOnSuccessListener { userSnapshot ->
                        val fName = userSnapshot.getString("firstName")
                        val lName = userSnapshot.getString("lastName")
                        val userName = "$fName $lName"

                        val headerLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(16, 16, 16, 0)
                        }

                        val nameTextView = TextView(requireContext()).apply {
                            text = userName
                            textSize = 16f
                            setTextColor(Color.BLACK)
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }
                        val timeTextView = TextView(requireContext()).apply {
                            text = formattedTime
                            textSize = 14f
                            setTextColor(Color.GRAY)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        headerLayout.addView(nameTextView)
                        headerLayout.addView(timeTextView)

                        val announcementTextView = TextView(requireContext()).apply {
                            text = announcementText
                            textSize = 18f
                            setTextColor(Color.BLACK)
                            setPadding(16, 8, 16, 16)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = android.view.Gravity.CENTER
                            }
                        }

                        // Add the header layout and the announcement text to the container
                        binding.announcementsContainer.addView(headerLayout)
                        binding.announcementsContainer.addView(announcementTextView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch announcements", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

}
