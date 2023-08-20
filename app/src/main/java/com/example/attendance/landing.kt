@file:Suppress("DEPRECATION")

package com.example.attendance


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class landing : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var timeTextView: TextView
    private lateinit var timetextviewtwo: TextView
    private lateinit var attendance: Button
    private lateinit var dailyt:Button
    private lateinit var focus:Button
    private lateinit var back:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        @Suppress("DEPRECATION")
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val quoteTextView: TextView = findViewById(R.id.quote)
        fetchRandomQuote(quoteTextView)



        timeTextView= findViewById(R.id.timeTextView)
        updateCurrentTime()
        timetextviewtwo=findViewById(R.id.timeTextViewtwo)
        updateCurrentTimetwo()

        button = findViewById(R.id.am)
        button.setOnClickListener {
            val intent= Intent(this,home::class.java)
            startActivity(intent)
        }



    }

    private fun fetchRandomQuote(quoteTextView: TextView) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.quotable.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val quotableApiService = retrofit.create(QuotableApiService::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = quotableApiService.getRandomQuote()
                if (response.isSuccessful) {
                    val quote = response.body()?.content ?: "No quote available"
                    withContext(Dispatchers.Main) {
                        quoteTextView.text = quote
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateCurrentTime() {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime)

        timeTextView.text = formattedTime
    }

    private fun updateCurrentTimetwo() {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("mm", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime)

        timetextviewtwo.text = formattedTime
    }
}

