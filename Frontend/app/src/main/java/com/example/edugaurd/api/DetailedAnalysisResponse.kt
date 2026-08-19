package com.example.edugaurd.api

data class DetailedAnalysisResponse(
    val student: AnalysisStudent,
    val overall_academic: Double,
    val attendance: DetailedAttendance,
    val study: DetailedStudy,
    val assignments: DetailedAssignments,
    val subjects: List<SubjectAnalysis>

)

data class DetailedAssignments(
    val score: Double,
    val total: Int,
    val submitted: Int,
    val late: Int,
    val missing: Int,
    val completion: Double,
    val average_score: Double
)

data class AnalysisStudent(
    val student_id: Int,
    val enrollment_number: String,
    val first_name: String,
    val last_name: String
)

data class DetailedStudy(
    val score: Double,
    val avg_logins: Double,
    val avg_study_hours: Double,
    val avg_videos: Double,
    val avg_resources: Double
)

data class DetailedAttendance(
    val percentage: Double,
    val total: Int,
    val present: Int,
    val absent: Int
)

data class SubjectAnalysis(
    val subject_id: Int,
    val subject_name: String,
    val exam1: Double,
    val exam1_max: Double,
    val exam2: Double,
    val exam2_max: Double,
    val exam3: Double,
    val exam3_max: Double,
    val percentage: Double
)