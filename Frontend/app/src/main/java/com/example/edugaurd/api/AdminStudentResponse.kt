package com.example.edugaurd.api

data class AdminStudent(
    val student_id: Int,
    val enrollment_number: String,
    val first_name: String,
    val last_name: String,
    val division_id: Int?,
    val division_code: String?
)

data class AdminStudentResponse(
    val success: Boolean,
    val students: List<AdminStudent>
)