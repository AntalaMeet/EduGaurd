package com.example.edugaurd.api

data class AdminDivision(
    val division_id: Int,
    val division_code: String,
    val start_enrollment: Int,
    val end_enrollment: Int
)

data class AdminDivisionResponse(
    val success: Boolean,
    val divisions: List<AdminDivision>
)