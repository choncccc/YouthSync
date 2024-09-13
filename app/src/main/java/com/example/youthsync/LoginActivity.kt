package com.example.youthsync

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.youthsync.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=  FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener{
            val email = binding.Username.text.toString()
            val pw= binding.Password.text.toString()
            if (email.isEmpty() || pw.isEmpty()){
                Toast.makeText(this, "All fields must filled up!", Toast.LENGTH_LONG).show()
            }else
                firebaseAuth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, HomeScreenActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        else
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                    }
        }

        binding.SignUp.setOnClickListener{
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
}