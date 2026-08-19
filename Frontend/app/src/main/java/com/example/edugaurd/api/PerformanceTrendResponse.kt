package com.example.edugaurd.api

data class PerformanceTrendResponse(
    val success: Boolean,
    val student: TrendStudent,
    val attendance_trend: String,
    val academic_trend: String,
    val assignment_trend: String,
    val trend: List<PerformanceTrend>
)

data class TrendStudent(
    val student_id: Int,
    val first_name: String,
    val last_name: String
)

data class PerformanceTrend(
    val week: String,
    val attendance: Double?,
    val academic: Double?,
    val assignments: Double?
)