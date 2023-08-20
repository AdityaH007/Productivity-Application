package com.example.attendance

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button

class about : AppCompatActivity() {
    private lateinit var email: Button
    private lateinit var git:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        email=findViewById(R.id.email)
        email.setOnClickListener {
            openWebsite("marvellousinc7@gmail.com")
        }
        
        git=findViewById(R.id.github)
        git.setOnClickListener {
            openWebsite("https://www.example.com")
        }
    }

    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}