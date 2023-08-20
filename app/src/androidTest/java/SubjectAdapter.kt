package com.example.attendance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class SubjectAdapter(private val context: Context, private val subjects: ArrayList<String>) : BaseAdapter() {

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

        val subjectName = getItem(position) as String
        subjectNameTextView.text = subjectName

        deleteButton.setOnClickListener {
            // Handle delete button click here
            // You can call a function to delete the subject
        }

        return view
    }
}
