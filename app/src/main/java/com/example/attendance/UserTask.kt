package com.example.attendance
data class UserTask(
    val taskText: String = "",
    val timestamp: Long = 0,
    var isExpired: Boolean = false
)


