package com.example.edugaurd.api

data class DashboardResponse(
    val student: StudentData,
    val overall: OverallData,
    val attendance: AttendanceData,
    val academic: AcademicData,
    val study: StudyData,
    val assignments: AssignmentData,
    val recommendation: String
)

data class StudentData(
    val student_id: Int,
    val enrollment_number: String,
    val first_name: String,
    val last_name: String,
    val division_id: Int
)

data class OverallData(
    val dropout_risk: Double,
    val risk_level: String
)

data class AttendanceData(
    val percentage: Double,
    val total: Int,
    val present: Int
)

data class AcademicData(
    val score: Double
)

data class StudyData(
    val score: Double
)

data class AssignmentData(
    val score: Double
)