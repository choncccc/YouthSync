package com.example.youthsync

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.youthsync.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            try {
                auth = FirebaseAuth.getInstance()
                firebaseDB = FirebaseFirestore.getInstance()
                fetchCreds()
                disableFields()

                binding.EditProfile.setOnClickListener {
                    enableFields()
                }

                binding.BTNSave.setOnClickListener {
                    val fname = binding.ETFName.text.toString()
                    val lname= binding.ETLName.text.toString()
                            val user = auth.currentUser
                            if (user != null) {
                                val userID = user.uid
                                val ref = firebaseDB.collection("users").document(userID)
                                ref.get().addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        updateUserProfile(userID,fname, lname)
                                        }
                                    }.addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to fetch user data: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                }


                binding.TVChangePass.setOnClickListener {
                    val dialog = Dialog(requireContext())
                    val dialogView = layoutInflater.inflate(R.layout.dialog_box, null)
                    dialog.window?.setBackgroundDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.border)
                    )
                    dialog.setContentView(dialogView)
                    dialog.show()
                    dialogView.findViewById<Button>(R.id.okButton).setOnClickListener {
                        val auth = FirebaseAuth.getInstance()
                        val user = auth.currentUser
                        if (user != null) {
                            val userID = user.uid
                            val ref = firebaseDB.collection("users").document(userID)
                            ref.get().addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val email = document.getString("email")
                                    if (email != null) {
                                        auth.sendPasswordResetEmail(email)
                                            .addOnSuccessListener {
                                                Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_LONG).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(requireContext(), "Failed to send password reset email", Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_LONG).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to fetch credentials", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "No user signed in", Toast.LENGTH_LONG).show()
                        }
                        dialog.dismiss()
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    "UserProfileFragment",
                    "Error initializing Firebase or handling UI: ${e.message}"
                )
                Toast.makeText(requireContext(), "Error initializing the view", Toast.LENGTH_LONG)
                    .show()
            }
        }

        private fun enableFields(){
            binding.BTNSave.isEnabled = true
            binding.ETFName.isEnabled = true
            binding.ETLName.isEnabled = true
                binding.Label.text = "EDIT PROFILE"
    }

    private fun disableFields(){
        binding.BTNSave.isEnabled = false
        binding.ETFName.isEnabled = false
        binding.ETLName.isEnabled = false
    }

    private fun fetchCreds(){
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val userID = user.uid
            val ref = firebaseDB.collection("users").document(userID)
            ref.get().addOnSuccessListener {
                if (it != null && it.exists()) {
                    val fName = it.getString("firstName")
                    val lName = it.getString("lastName")
                    val email = it.getString("email")
                        binding.TVName.text = "$fName $lName"
                        binding.ETFName.setText("$fName")
                        binding.ETLName.setText("$lName")
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_LONG)
                        .show()
                }
            }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to fetch Credentials", Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show()
        }

    }

    private fun updateUserProfile(userID: String, firstName: String, lastName: String) {
        val userProfileUpdates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName
        )
        firebaseDB.collection("users").document(userID)
            .update(userProfileUpdates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }







}