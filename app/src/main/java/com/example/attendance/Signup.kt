package com.example.attendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonSignUp: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase


    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        database = FirebaseDatabase.getInstance()

        val buttonSignUp: View = findViewById(R.id.button)
        buttonSignUp.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    if (user != null) {
                        val emailNode = user.email?.replace(".", ",") ?: "" // Handle null case
                        val passwordNode = user.uid // Use UID as a key for the password
                        databaseReference.child("users").child(emailNode).setValue(passwordNode)

                        // Redirect to the landing page or another activity
                        // For example:
                         val intent = Intent(this, landing::class.java)
                       startActivity(intent)
                    }
                } else {
                    // Handle registration failure
                }
            }
    }
}

