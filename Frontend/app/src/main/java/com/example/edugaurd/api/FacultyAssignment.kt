package com.example.edugaurd.api

data class FacultyAssignment(
    val assignment_id: Int,
    val division_id: Int,
    val division_code: String,
    val subject_id: Int,
    val subject_code: String,
    val subject_name: String
)