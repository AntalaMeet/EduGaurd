package com.example.edugaurd.api

data class FacultyStudent(
    val student_id: Int,
    val enrollment_number: String,
    val first_name: String,
    val last_name: String,
    val attendance: Double,
    val academic: Double,
    val study: Double,
    val assignments: Double,
    val dropout_risk: Double,
    val risk_level: String,
    val division_id: Int
)