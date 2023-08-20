package com.example.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import attendance
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class SubjectManagementActivity : AppCompatActivity() {

    override fun onBackPressed() {
        super.onBackPressed()

        // Make the subjects list view visible when back is pressed
        subjectListView.visibility = View.VISIBLE
    }

    private lateinit var addSubjectButton: Button
    private lateinit var subjectListView: ListView
    private lateinit var subjects: ArrayList<String>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_management)

        addSubjectButton = findViewById(R.id.addSubjectButton)
        subjectListView = findViewById(R.id.subjectListView)
        subjects = ArrayList()
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val adapter = SubjectAdapter(this, subjects)
        subjectListView.adapter = adapter

        addSubjectButton.setOnClickListener {
            showAddSubjectDialog()
        }

        // Load user-specific subjects from Firestore
        loadUserSubjects()
    }

    private fun showAddSubjectDialog() {
        val inputEditText = EditText(this)
        inputEditText.hint = "Enter subject name"

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Subject")
            .setView(inputEditText)
            .setPositiveButton("Add") { _, _ ->
                val subjectName = inputEditText.text.toString().trim()
                if (subjectName.isNotEmpty()) {
                    addSubject(subjectName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addSubject(subjectName: String) {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            val subjectData = hashMapOf("name" to subjectName)
            firestore.collection("users").document(uid)
                .collection("subjects").add(subjectData) // Store in Firestore
        }
    }

    private fun deleteSubject(subjectName: String) {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .collection("subjects")
                .whereEqualTo("name", subjectName)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                        subjects.remove(subjectName)
                        (subjectListView.adapter as SubjectAdapter).notifyDataSetChanged()
                    }
                }
        }
    }

    private fun loadUserSubjects() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .collection("subjects").get()
                .addOnSuccessListener { result ->
                    subjects.clear()
                    for (document in result) {
                        val subjectName = document.getString("name")
                        if (subjectName != null) {
                            subjects.add(subjectName)
                        }
                    }
                    (subjectListView.adapter as SubjectAdapter).notifyDataSetChanged()
                }
        }
    }

    private inner class SubjectAdapter(private val context: AppCompatActivity, private val subjects: ArrayList<String>) : BaseAdapter() {

        override fun getCount(): Int {
            return subjects.size
        }

        override fun getItem(position: Int): Any {
            return subjects[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = LayoutInflater.from(context).inflate(R.layout.list_item_subject, null)

            val subjectNameTextView: TextView = view.findViewById(R.id.subjectNameTextView)
            val deleteButton: Button = view.findViewById(R.id.deleteButton)
            val viewAttendanceButton: Button = view.findViewById(R.id.viewAttendanceButton) // New button

            val subjectName = getItem(position) as String
            subjectNameTextView.text = subjectName

            deleteButton.setOnClickListener {
                deleteSubject(subjectName)
            }

            // Handle click on View Attendance button
            viewAttendanceButton.setOnClickListener {
                // Launch AttendanceFragment for the selected subject
                findViewById<View>(R.id.fragmentContainer).visibility = View.VISIBLE

                val attendanceFragment = attendance()
                val bundle = Bundle()
                bundle.putString("subjectName", subjectName)
                attendanceFragment.arguments = bundle

                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainer, attendanceFragment)
                transaction.addToBackStack(null)
                transaction.commit()

            }

            return view
        }
    }
}
