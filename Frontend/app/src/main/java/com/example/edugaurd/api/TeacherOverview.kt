package com.example.edugaurd.api

data class TeacherOverview(
    val total_students: Int,
    val average_attendance: Double,
    val average_academic: Double,
    val high_risk: Int,
    val medium_risk: Int,
    val low_risk: Int
)