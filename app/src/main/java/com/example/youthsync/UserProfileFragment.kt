package com.example.youthsync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.example.youthsync.databinding.FragmentUserProfileBinding
import com.google.firebase.Firebase
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
        disableFields()
        auth = FirebaseAuth.getInstance()
        firebaseDB = FirebaseFirestore.getInstance()
        binding.EditProfile.setOnClickListener(){
            enableFields()
        }
        binding.BTNSave.setOnClickListener(){
            //TODO BUSINESS LOGIC FOR EDITING THE USER'S PROFILE!!!!!!



            binding.Label.text = "PROFILE"
            disableFields()

        }
    }

    private fun enableFields(){
            binding.BTNSave.isEnabled = true
            binding.ETFName.isEnabled = true
            binding.ETLName.isEnabled = true
            binding.ETEmail.isEnabled = true
            binding.ETPassword.isEnabled = true
        binding.Label.text = "EDIT PROFILE"
    }

    private fun disableFields(){
        binding.BTNSave.isEnabled = false
        binding.ETFName.isEnabled = false
        binding.ETLName.isEnabled = false
        binding.ETEmail.isEnabled = false
        binding.ETPassword.isEnabled = false
    }
}