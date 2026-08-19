package com.example.edugaurd.api

data class AdminDashboardResponse(
    val total_students: Int,
    val total_faculty: Int,
    val total_divisions: Int,
    val total_users: Int
)