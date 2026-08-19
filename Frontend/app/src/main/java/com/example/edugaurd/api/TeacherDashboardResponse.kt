package com.example.edugaurd.api

data class TeacherDashboardResponse(
    val faculty: FacultyInfo,
    val assignments: List<FacultyAssignment>,
    val mentor_divisions: List<MentorDivision>,
    val overview: TeacherOverview,
    val students: List<FacultyStudent>
)