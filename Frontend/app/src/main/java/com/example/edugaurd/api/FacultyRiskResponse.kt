package com.example.edugaurd.api

data class FacultyRiskResponse(
    val success: Boolean,
    val mode: String,
    val division_id: Int,
    val division_code: String,
    val subject_id: Int?,
    val subject_code: String?,
    val subject_name: String?,
    val total_students: Int,
    val average_attendance: Double,
    val average_academic: Double,
    val high_risk: Int,
    val medium_risk: Int,
    val low_risk: Int,
    val students: List<FacultyRiskStudent>
)