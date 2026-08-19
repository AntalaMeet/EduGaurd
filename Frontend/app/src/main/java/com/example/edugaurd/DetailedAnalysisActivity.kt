package com.example.edugaurd

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edugaurd.api.DetailedAnalysisResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailedAnalysisActivity : AppCompatActivity() {

    private lateinit var tvStudentName: TextView
    private lateinit var tvOverallAcademic: TextView
    private lateinit var tvDetailedAttendance: TextView
    private lateinit var tvAttendanceStats: TextView
    private lateinit var tvMarksDetails: TextView

    private lateinit var tvAssignmentDetails: TextView
    private lateinit var tvStudyDetails: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_detailed_analysis)

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

        RefreshHelper.setup(
            swipeRefresh
        ) {
            loadDetailedAnalysis(studentId)
        }
        loadDetailedAnalysis(studentId)
    }

    private fun initializeViews() {

        tvStudentName =
            findViewById(R.id.tvStudentName)

        tvOverallAcademic =
            findViewById(R.id.tvOverallAcademic)

        tvDetailedAttendance =
            findViewById(R.id.tvDetailedAttendance)

        tvAttendanceStats =
            findViewById(R.id.tvAttendanceStats)

        tvMarksDetails =
            findViewById(R.id.tvMarksDetails)

        tvStudyDetails =
            findViewById(R.id.tvStudyDetails)

        tvAssignmentDetails =
            findViewById(R.id.tvAssignmentDetails)
        swipeRefresh =
            findViewById(R.id.swipeRefresh)
    }

    private fun loadDetailedAnalysis(
        studentId: Int
    ) {

        RetrofitClient.api
            .getDetailedAnalysis(studentId)
            .enqueue(
                object : Callback<DetailedAnalysisResponse> {

                    override fun onResponse(
                        call: Call<DetailedAnalysisResponse>,
                        response: Response<DetailedAnalysisResponse>
                    ) {
                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        if (response.isSuccessful) {

                            val data =
                                response.body()

                            if (data != null) {

                                displayData(data)

                            } else {

                                showError(
                                    "Empty response from server"
                                )
                            }

                        } else {

                            showError(
                                "Server error: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<DetailedAnalysisResponse>,
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

    private fun displayData(
        data: DetailedAnalysisResponse
    ) {

        // -----------------------------
        // STUDENT
        // -----------------------------

        tvStudentName.text =
            "${data.student.first_name} ${data.student.last_name}"


        // -----------------------------
        // ASSIGNMENTS
        // -----------------------------

        tvAssignmentDetails.text =
            "Assignment Score: ${data.assignments.score}%\n\n" +
                    "Total Assignments: ${data.assignments.total}\n" +
                    "Submitted: ${data.assignments.submitted}\n" +
                    "Late: ${data.assignments.late}\n" +
                    "Missing: ${data.assignments.missing}\n" +
                    "Completion: ${data.assignments.completion}%\n" +
                    "Average Score: ${data.assignments.average_score}%"


        // -----------------------------
        // OVERALL ACADEMIC
        // -----------------------------

        tvOverallAcademic.text =
            "${data.overall_academic}%"


        // -----------------------------
        // ATTENDANCE
        // -----------------------------

        tvDetailedAttendance.text =
            "${data.attendance.percentage}%"

        tvAttendanceStats.text =
            "${data.attendance.present} present • " +
                    "${data.attendance.absent} absent • " +
                    "${data.attendance.total} total"


        // -----------------------------
        // STUDY
        // -----------------------------

        tvStudyDetails.text =
            "Study Score: ${data.study.score}%\n\n" +
                    "LMS Logins: ${data.study.avg_logins}\n" +
                    "Study Hours: ${data.study.avg_study_hours} hours\n" +
                    "Videos Watched: ${data.study.avg_videos}\n" +
                    "Resources Accessed: ${data.study.avg_resources}"


        // -----------------------------
        // SUBJECT MARKS
        // -----------------------------

        val builder =
            StringBuilder()

        for (subject in data.subjects) {

            builder.append("\n")
            builder.append(subject.subject_name)
            builder.append("\n\n")

            builder.append(
                "Exam 1    ${subject.exam1} / ${subject.exam1_max}\n"
            )

            builder.append(
                "Exam 2    ${subject.exam2} / ${subject.exam2_max}\n"
            )

            builder.append(
                "Exam 3    ${subject.exam3} / ${subject.exam3_max}\n"
            )

            builder.append(
                "\nSubject Average: ${subject.percentage}%\n"
            )

            builder.append(
                "────────────────────────\n"
            )
        }

        tvMarksDetails.text =
            builder.toString()
    }

    private fun showError(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

}