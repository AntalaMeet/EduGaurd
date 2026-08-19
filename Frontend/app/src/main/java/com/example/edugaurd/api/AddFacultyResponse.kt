package com.example.edugaurd.api

data class AddFacultyResponse(
    val success: Boolean,
    val message: String?,
    val faculty_id: Int?,
    val user_id: Int?
)