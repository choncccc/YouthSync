package com.example.youthsync
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.youthsync.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth= FirebaseAuth.getInstance()
        binding.SignUpButton.setOnClickListener{
            val fname = binding.FirstName.text.toString().lowercase()
            val lname = binding.LastName.text.toString().lowercase()
            val username = binding.Username.text.toString()
            val password = binding.Password.text.toString()
            if (fname.isEmpty() || lname.isEmpty() || username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Fields should not be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            firebaseAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        user?.let {
                            saveUserToDatabase(it.uid, fname, lname, username, password)
                        }
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.zoon_in, R.anim.zoom_out)
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }


        binding.Login.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.zoon_in, R.anim.zoom_out)
        }
    }
    private fun saveUserToDatabase(userId: String, firstName: String, lastName: String, email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "password" to password,
            "role" to "user"
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Sign Up Success!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("SignUpActivity", "Error saving user to database", e)
            }
    }
}