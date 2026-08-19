package com.example.edugaurd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edugaurd.api.DashboardResponse
import com.example.edugaurd.api.PredictionResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.Color
import com.example.edugaurd.api.PerformanceTrendResponse
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvEnrollment: TextView

    private lateinit var tvRisk: TextView
    private lateinit var tvRiskLevel: TextView

    private lateinit var tvAttendance: TextView
    private lateinit var tvAttendanceDetail: TextView

    private lateinit var tvAcademic: TextView
    private lateinit var tvStudy: TextView
    private lateinit var tvAssignments: TextView

    private lateinit var progressAttendance: ProgressBar
    private lateinit var progressAcademic: ProgressBar
    private lateinit var progressStudy: ProgressBar
    private lateinit var progressAssignments: ProgressBar

    private lateinit var tvRecommendation: TextView

    private lateinit var btnDetailedAnalysis: Button

    private lateinit var btnLogout: ImageButton

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var performanceChart: LineChart

    private lateinit var tvTrendStatus: TextView
    private lateinit var tvAttendanceTrend: TextView
    private lateinit var tvAcademicTrend: TextView
    private lateinit var tvAssignmentTrend: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.swipeRefresh)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        initializeViews()

        val studentId =
            intent.getIntExtra("student_id", -1)

        if (studentId == -1) {

            Toast.makeText(
                this,
                "Student ID not found",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        setupButtons(studentId)
        RefreshHelper.setup(
            swipeRefresh
        ) {
            loadDashboard(studentId)
            loadPrediction(studentId)
            loadPerformanceTrend(studentId)
        }
        // Load normal dashboard data
        loadDashboard(studentId)

        // Load ML prediction
        loadPrediction(studentId)
        loadPerformanceTrend(studentId)
    }

    private fun initializeViews() {

        tvGreeting =
            findViewById(R.id.tvGreeting)

        tvEnrollment =
            findViewById(R.id.tvEnrollment)

        tvRisk =
            findViewById(R.id.tvDropoutRisk)

        tvRiskLevel =
            findViewById(R.id.tvRiskLevel)

        tvAttendance =
            findViewById(R.id.tvAttendance)

        tvAttendanceDetail =
            findViewById(R.id.tvAttendanceDetail)

        tvAcademic =
            findViewById(R.id.tvAcademic)

        tvStudy =
            findViewById(R.id.tvStudy)

        tvAssignments =
            findViewById(R.id.tvAssignments)

        progressAttendance =
            findViewById(R.id.progressAttendance)

        progressAcademic =
            findViewById(R.id.progressAcademic)

        progressStudy =
            findViewById(R.id.progressStudy)

        progressAssignments =
            findViewById(R.id.progressAssignments)

        tvRecommendation =
            findViewById(R.id.tvRecommendation)

        btnDetailedAnalysis =
            findViewById(R.id.btnDetailedAnalysis)

        btnLogout =
            findViewById(R.id.btnLogout)

        swipeRefresh =
            findViewById(R.id.swipeRefresh)

        performanceChart =
            findViewById(R.id.performanceChart)

        tvTrendStatus =
            findViewById(R.id.tvTrendStatus)

        tvAttendanceTrend =
            findViewById(R.id.tvAttendanceTrend)

        tvAcademicTrend =
            findViewById(R.id.tvAcademicTrend)

        tvAssignmentTrend =
            findViewById(R.id.tvAssignmentTrend)
    }

    private fun setupButtons(
        studentId: Int
    ) {

        btnDetailedAnalysis.setOnClickListener {

            val intent = Intent(
                this,
                DetailedAnalysisActivity::class.java
            )

            intent.putExtra(
                "student_id",
                studentId
            )

            startActivity(intent)
        }

        btnLogout.setOnClickListener {

            logout()
        }
    }

    private fun logout() {

        getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()

        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }


    // =================================================
    // NORMAL DASHBOARD
    // =================================================

    private fun loadDashboard(studentId: Int) {

        RetrofitClient.api
            .getStudentDashboard(studentId)
            .enqueue(
                object : Callback<DashboardResponse> {

                    override fun onResponse(
                        call: Call<DashboardResponse>,
                        response: Response<DashboardResponse>
                    ) {

                        if (response.isSuccessful) {

                            val data =
                                response.body()

                            if (data != null) {

                                displayDashboard(data)

                            } else {

                                showError(
                                    "Empty dashboard response"
                                )
                            }

                        } else {

                            showError(
                                "Dashboard error: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<DashboardResponse>,
                        t: Throwable
                    ) {
                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        showError(
                            "Connection error: ${t.message}"
                        )

                        t.printStackTrace()
                    }
                }
            )
    }

    private fun displayDashboard(
        data: DashboardResponse
    ) {

        // -------------------------
        // STUDENT
        // -------------------------

        tvGreeting.text =
            "Hi, ${data.student.first_name} 👋"

        tvEnrollment.text =
            data.student.enrollment_number


        // -------------------------
        // ATTENDANCE
        // -------------------------

        val attendance =
            data.attendance.percentage

        tvAttendance.text =
            "${attendance}%"

        progressAttendance.progress =
            attendance.toInt()

        tvAttendanceDetail.text =
            "${data.attendance.present} present out of " +
                    "${data.attendance.total} classes"


        // -------------------------
        // ACADEMIC
        // -------------------------

        val academic =
            data.academic.score

        tvAcademic.text =
            "${academic}%"

        progressAcademic.progress =
            academic.toInt()


        // -------------------------
        // STUDY
        // -------------------------

        val study =
            data.study.score

        tvStudy.text =
            "${study}%"

        progressStudy.progress =
            study.toInt()


        // -------------------------
        // ASSIGNMENTS
        // -------------------------

        val assignments =
            data.assignments.score

        tvAssignments.text =
            "${assignments}%"

        progressAssignments.progress =
            assignments.toInt()


        // -------------------------
        // RECOMMENDATION
        // -------------------------

        tvRecommendation.text =
            data.recommendation
    }


    // =================================================
    // ML DROPOUT PREDICTION
    // =================================================

    private fun loadPrediction(
        studentId: Int
    ) {

        RetrofitClient.api
            .getPrediction(studentId)
            .enqueue(
                object : Callback<PredictionResponse> {

                    override fun onResponse(
                        call: Call<PredictionResponse>,
                        response: Response<PredictionResponse>
                    ) {
                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        if (response.isSuccessful) {

                            val data =
                                response.body()

                            if (data != null) {

                                val risk =
                                    data.prediction.dropout_risk

                                val level =
                                    data.prediction.risk_level

                                // Display ML result
                                tvRisk.text =
                                    "${risk}%"

                                tvRiskLevel.text =
                                    "${level} RISK"

                            } else {

                                tvRisk.text =
                                    "--"

                                tvRiskLevel.text =
                                    "NO PREDICTION"
                            }

                        } else {

                            tvRisk.text =
                                "--"

                            tvRiskLevel.text =
                                "PREDICTION UNAVAILABLE"

                            Toast.makeText(
                                this@MainActivity,
                                "Prediction error: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<PredictionResponse>,
                        t: Throwable
                    ) {

                        tvRisk.text =
                            "--"

                        tvRiskLevel.text =
                            "CONNECTION ERROR"

                        Toast.makeText(
                            this@MainActivity,
                            "Unable to load ML prediction",
                            Toast.LENGTH_SHORT
                        ).show()

                        t.printStackTrace()
                    }
                }
            )
    }


    // =================================================
    // ERROR
    // =================================================

    private fun showError(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
    private fun loadPerformanceTrend(
        studentId: Int
    ) {

        RetrofitClient.api
            .getPerformanceTrend(studentId)
            .enqueue(
                object : Callback<PerformanceTrendResponse> {

                    override fun onResponse(
                        call: Call<PerformanceTrendResponse>,
                        response: Response<PerformanceTrendResponse>
                    ) {

                        if (response.isSuccessful) {

                            val data =
                                response.body()

                            if (data != null) {

                                displayPerformanceTrend(data)

                            } else {

                                tvTrendStatus.text =
                                    "No performance data available"
                            }

                        } else {

                            tvTrendStatus.text =
                                "Unable to load performance trend"
                        }
                    }

                    override fun onFailure(
                        call: Call<PerformanceTrendResponse>,
                        t: Throwable
                    ) {

                        tvTrendStatus.text =
                            "Performance trend unavailable"

                        t.printStackTrace()
                    }
                }
            )
    }
    private fun displayPerformanceTrend(
        data: PerformanceTrendResponse
    ) {

        // -------------------------
        // TREND STATUS
        // -------------------------

        tvTrendStatus.text =
            "Recent student performance"


        tvAttendanceTrend.text =
            "Attendance: ${data.attendance_trend}"

        tvAcademicTrend.text =
            "Academic: ${data.academic_trend}"

        tvAssignmentTrend.text =
            "Assignments: ${data.assignment_trend}"


        // -------------------------
        // CHART ENTRIES
        // -------------------------

        val attendanceEntries =
            ArrayList<Entry>()

        val academicEntries =
            ArrayList<Entry>()

        val assignmentEntries =
            ArrayList<Entry>()

        val labels =
            ArrayList<String>()


        data.trend.forEachIndexed { index, item ->

            // Example:
            // 2026-08-10 → 08/10

            labels.add(
                item.week.substring(5)
            )


            // Attendance

            item.attendance?.let { value ->

                attendanceEntries.add(
                    Entry(
                        index.toFloat(),
                        value.toFloat()
                    )
                )
            }


            // Academic

            item.academic?.let { value ->

                academicEntries.add(
                    Entry(
                        index.toFloat(),
                        value.toFloat()
                    )
                )
            }


            // Assignments

            item.assignments?.let { value ->

                assignmentEntries.add(
                    Entry(
                        index.toFloat(),
                        value.toFloat()
                    )
                )
            }
        }


        // -------------------------
        // DATASETS
        // -------------------------

        val dataSets =
            ArrayList<LineDataSet>()


        if (attendanceEntries.isNotEmpty()) {

            val attendanceSet =
                LineDataSet(
                    attendanceEntries,
                    "Attendance"
                )

            attendanceSet.color = Color.rgb(33, 150, 243)
            attendanceSet.setCircleColor(Color.rgb(33, 150, 243))
            attendanceSet.lineWidth = 3f
            attendanceSet.circleRadius = 4f
            attendanceSet.setDrawValues(false)

            dataSets.add(
                attendanceSet
            )
        }


        if (academicEntries.isNotEmpty()) {

            val academicSet =
                LineDataSet(
                    academicEntries,
                    "Academic"
                )

            academicSet.color = Color.rgb(156, 39, 176)
            academicSet.setCircleColor(Color.rgb(156, 39, 176))
            academicSet.lineWidth = 3f
            academicSet.circleRadius = 4f
            academicSet.setDrawValues(false)

            dataSets.add(
                academicSet
            )
        }


        if (assignmentEntries.isNotEmpty()) {

            val assignmentSet =
                LineDataSet(
                    assignmentEntries,
                    "Assignments"
                )

            assignmentSet.color = Color.rgb(255, 152, 0)
            assignmentSet.setCircleColor(Color.rgb(255, 152, 0))
            assignmentSet.lineWidth = 3f
            assignmentSet.circleRadius = 4f
            assignmentSet.setDrawValues(false)

            dataSets.add(
                assignmentSet
            )
        }


        // -------------------------
        // SET CHART DATA
        // -------------------------
        val lineData = LineData()

        for (dataSet in dataSets) {
            lineData.addDataSet(dataSet)
        }

        performanceChart.data = lineData


        // -------------------------
        // CHART SETTINGS
        // -------------------------

        performanceChart.description.isEnabled =
            false

        performanceChart.axisRight.isEnabled =
            false

        performanceChart.axisLeft.axisMinimum =
            0f

        performanceChart.axisLeft.axisMaximum =
            100f

        performanceChart.xAxis.granularity =
            1f

        performanceChart.xAxis.valueFormatter =
            IndexAxisValueFormatter(
                labels
            )

        performanceChart.xAxis.labelRotationAngle =
            -45f

        performanceChart.legend.isEnabled =
            true

        performanceChart.setTouchEnabled(true)

        performanceChart.isDragEnabled = true

        performanceChart.setScaleEnabled(true)

        performanceChart.invalidate()
    }
}