package com.example.edugaurd.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @GET("students/{student_id}/dashboard")
    fun getStudentDashboard(
        @Path("student_id") studentId: Int
    ): Call<DashboardResponse>

    @GET("students/{student_id}/detailed-analysis")
    fun getDetailedAnalysis(
        @Path("student_id") studentId: Int
    ): Call<DetailedAnalysisResponse>

    @GET("students/{student_id}/prediction")
    fun getPrediction(
        @Path("student_id") studentId: Int
    ): Call<PredictionResponse>

    @GET("faculty/{faculty_id}/dashboard")
    fun getFacultyDashboard(
        @Path("faculty_id") facultyId: Int
    ): Call<TeacherDashboardResponse>

    @GET("faculty/{faculty_id}/risk")
    fun getFacultyRisk(
        @Path("faculty_id") facultyId: Int,
        @Query("division_id") divisionId: Int,
        @Query("subject_id") subjectId: Int? = null
    ): Call<FacultyRiskResponse>
    @GET("admin/dashboard")
    fun getAdminDashboard(
    ): Call<AdminDashboardResponse>

    @GET("admin/faculty")
    fun getAllFaculty():
            Call<FacultyAdminResponse>

    @POST("admin/faculty")
    fun addFaculty(
        @Body request: AddFacultyRequest
    ): Call<AddFacultyResponse>

    @GET("admin/students")
    fun getAllStudents(): Call<AdminStudentResponse>

    @GET("admin/divisions")
    fun getAllDivisions(): Call<AdminDivisionResponse>

    @GET("student/{student_id}/performance-trend")
    fun getPerformanceTrend(
        @Path("student_id") studentId: Int
    ): Call<PerformanceTrendResponse>

}