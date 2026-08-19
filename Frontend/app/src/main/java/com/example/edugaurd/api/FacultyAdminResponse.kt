package com.example.edugaurd.api

data class FacultyAdminResponse(
    val success: Boolean,
    val faculty: List<FacultyAdmin>
)

data class FacultyAdmin(
    val faculty_id: Int,
    val first_name: String?,
    val last_name: String?,
    val email: String?,
    val division_id: Int?,
    val division_code: String?,
    val assignments: List<FacultyAssignment> = emptyList()
)