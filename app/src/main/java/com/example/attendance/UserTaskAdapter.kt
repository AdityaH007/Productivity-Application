package com.example.attendance

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

  class UserTaskAdapter(context: Context, tasksList: ArrayList<UserTask>) : ArrayAdapter<UserTask>(context, 0, tasksList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)

        val view = LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false)

        val taskTextView: TextView = view.findViewById(R.id.taskTextView)
        val timerTextView: TextView = view.findViewById(R.id.timerTextView)

        taskTextView.text = task?.taskText

        if (task?.isExpired == true) {
            timerTextView.text = "Expired"
        } else {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - (task?.timestamp ?: 0)
            val remainingTime = (24 * 60 * 60 * 1000) - elapsedTime

            val remainingHours = (remainingTime / (1000 * 60 * 60)) % 24
            val remainingMinutes = (remainingTime / (1000 * 60)) % 60
            val remainingSeconds = (remainingTime / 1000) % 60

            val timerText = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds)
            timerTextView.text = timerText

            // Start a countdown timer
            if (remainingTime > 0) {
                object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val remainingHours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                        val remainingMinutes = (millisUntilFinished / (1000 * 60)) % 60
                        val remainingSeconds = (millisUntilFinished / 1000) % 60

                        val timerText = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds)
                        timerTextView.text = timerText
                    }

                    override fun onFinish() {
                        timerTextView.text = "Expired"
                    }
                }.start()
            }
        }

        return view
    }
}
