package com.example.edugaurd.api

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val role: String?,
    val student_id: Int?,
    val faculty_id: Int?
)