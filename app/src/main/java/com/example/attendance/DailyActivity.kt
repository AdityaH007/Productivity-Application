package com.example.attendance

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.attendance.R
import com.example.attendance.UserTask
import com.example.attendance.UserTaskAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DailyActivity : AppCompatActivity() {

    private lateinit var addTaskButton: Button
    private lateinit var taskEditText: EditText
    private lateinit var taskListView: ListView
    private lateinit var tasks: ArrayList<UserTask>
    private lateinit var tasksAdapter: ArrayAdapter<UserTask>
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily)

        addTaskButton = findViewById(R.id.addTaskButton)
        taskEditText = findViewById(R.id.taskEditText)
        taskListView = findViewById(R.id.taskListView)

        tasks = ArrayList()
        tasksAdapter = UserTaskAdapter(this, tasks)

        taskListView.adapter = tasksAdapter

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        addTaskButton.setOnClickListener {
            val taskText = taskEditText.text.toString().trim()
            if (taskText.isNotEmpty()) {
                addTask(taskText)
            }
        }

        // Load tasks from Firestore
        loadTasksFromFirestore()
    }

    private fun addTask(taskText: String) {
        val currentTimestamp = System.currentTimeMillis()
        val task = UserTask(taskText, currentTimestamp, false)

        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            // Save the task to Firestore under the user's document
            firestore.collection("users").document(uid)
                .collection("tasks").add(task)
                .addOnSuccessListener {
                    tasks.add(task)
                    tasksAdapter.notifyDataSetChanged()

                    // Clear the EditText
                    taskEditText.text.clear()
                }
        }
    }

    private fun loadTasksFromFirestore() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .collection("tasks")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    tasks.clear()

                    val currentTimestamp = System.currentTimeMillis()

                    for (document in querySnapshot) {
                        val task = document.toObject(UserTask::class.java)
                        val taskAge = currentTimestamp - task.timestamp

                        if (taskAge <= 24 * 60 * 60 * 1000) {
                            tasks.add(task)
                        } else {
                            // Delete the task from Firestore
                            firestore.collection("users").document(uid)
                                .collection("tasks").document(document.id)
                                .delete()
                        }
                    }

                    tasksAdapter.notifyDataSetChanged()
                }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer to prevent memory leaks
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
