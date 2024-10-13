package com.example.youthsync

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.youthsync.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.Username.text.toString()
            val pw = binding.Password.text.toString()

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "All fields must be filled up!", Toast.LENGTH_LONG).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            //Toast.makeText(this, userId.toString(), Toast.LENGTH_LONG).show()
                            if (userId != null) {
                                checkUserRole(userId)
                            }
                        } else {
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        binding.SignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.zoon_in, R.anim.zoom_out)
        }

        binding.forgotPassword.setOnClickListener {
            val email = binding.Username.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first!", Toast.LENGTH_LONG).show()
            } else {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_box, null)
                val dialog = Dialog(this)
                dialog.setContentView(dialogView)
                dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.border))
                dialog.window?.setLayout(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                firebaseAuth.sendPasswordResetEmail(email)
                val okButton = dialogView.findViewById<Button>(R.id.okButton)
                okButton.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
    }

    private fun checkUserRole(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    //Toast.makeText(this, role.toString(), Toast.LENGTH_LONG).show()

                    if (role == "admin") {
                        val intent = Intent(this, HomeScreenActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, UserHomeScreenActivity::class.java)
                        startActivity(intent)
                    }
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    Toast.makeText(this, "User role not found", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}