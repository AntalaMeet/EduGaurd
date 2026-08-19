package com.example.edugaurd.api

data class AddFacultyRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val division_id: Int,
    val username: String,
    val password: String
)