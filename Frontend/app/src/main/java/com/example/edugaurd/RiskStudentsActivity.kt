package com.example.edugaurd

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edugaurd.api.FacultyStudent
import com.example.edugaurd.api.RetrofitClient
import com.example.edugaurd.api.TeacherDashboardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiskStudentsActivity : AppCompatActivity() {

    private lateinit var tvRiskTitle: TextView
    private lateinit var tvRiskSubtitle: TextView
    private lateinit var studentsContainer: LinearLayout
    private lateinit var btnBack: ImageButton

    private lateinit var tvLoading: TextView
    private var facultyId: Int = -1
    private var riskLevel: String = ""

    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_risk_students
        )

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


        // --------------------------------
        // INITIALIZE VIEWS
        // --------------------------------

        tvRiskTitle =
            findViewById(R.id.tvRiskTitle)

        tvRiskSubtitle =
            findViewById(R.id.tvRiskSubtitle)

        studentsContainer =
            findViewById(R.id.studentsContainer)

        tvLoading =
            findViewById(R.id.tvLoading)

        btnBack =
            findViewById(R.id.btnBack)
        swipeRefresh =
            findViewById(R.id.swipeRefresh)

        // --------------------------------
        // BACK BUTTON
        // --------------------------------

        btnBack.setOnClickListener {

            finish()
        }


        // --------------------------------
        // GET DATA FROM DASHBOARD
        // --------------------------------

        facultyId =
            intent.getIntExtra(
                "faculty_id",
                -1
            )

        riskLevel =
            intent.getStringExtra(
                "risk_level"
            ) ?: ""


        // --------------------------------
        // VALIDATE
        // --------------------------------

        if (
            facultyId == -1 ||
            riskLevel.isBlank()
        ) {

            Toast.makeText(
                this,
                "Faculty ID or risk level missing",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }


        // --------------------------------
        // HEADER
        // --------------------------------

        tvRiskTitle.text =
            "${riskLevel.uppercase()} RISK STUDENTS"

        tvRiskSubtitle.text =
            "Students classified as ${riskLevel.uppercase()} risk"


        // --------------------------------
        // LOAD STUDENTS
        // --------------------------------
        RefreshHelper.setup(
            swipeRefresh
        ) {
            loadStudents()
        }

        loadStudents()
    }


    // ==================================================
    // LOAD STUDENTS
    // ==================================================

    private fun loadStudents() {

        RetrofitClient.api
            .getFacultyDashboard(facultyId)
            .enqueue(
                object :
                    Callback<TeacherDashboardResponse> {

                    override fun onResponse(
                        call: Call<TeacherDashboardResponse>,
                        response: Response<TeacherDashboardResponse>
                    ) {
                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        if (!response.isSuccessful) {

                            showError(
                                "Server error: ${response.code()}"
                            )

                            return
                        }

                        val data = response.body()

                        if (data == null) {

                            showError(
                                "Empty server response"
                            )

                            return
                        }

                        displayStudents(
                            data.students
                        )
                    }


                    override fun onFailure(
                        call: Call<TeacherDashboardResponse>,
                        t: Throwable
                    ) {
                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        showError(
                            "Connection error: ${t.message}"
                        )
                    }
                }
            )
    }


    // ==================================================
    // DISPLAY STUDENTS
    // ==================================================

    private fun displayStudents(
        students: List<FacultyStudent>
    ) {

        tvLoading.visibility = View.GONE

        studentsContainer.removeAllViews()

        // rest of your code...


        for (student in students) {

            android.util.Log.d(
                "RISK_STUDENTS",
                "ID=${student.student_id}, " +
                        "Name=${student.first_name} ${student.last_name}, " +
                        "Risk='${student.risk_level}', " +
                        "Dropout=${student.dropout_risk}"
            )
        }

        val filteredStudents =
            students
                .filter { student ->
                    student.risk_level
                        .trim()
                        .equals(
                            riskLevel.trim(),
                            ignoreCase = true
                        )
                }
                .sortedByDescending {
                    it.dropout_risk
                }


        if (filteredStudents.isEmpty()) {

            val emptyText =
                TextView(this)

            emptyText.text =
                "No $riskLevel risk students found.\n\n" +
                        "Total received: ${students.size}"

            emptyText.textSize = 16f

            emptyText.setPadding(
                20,
                30,
                20,
                30
            )

            studentsContainer.addView(
                emptyText
            )

            return
        }


        for (student in filteredStudents) {

            addStudentCard(student)
        }
    }

    // ==================================================
    // STUDENT CARD
    // ==================================================

    private fun addStudentCard(
        student: FacultyStudent
    ) {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            24,
            20,
            24,
            20
        )

        card.setBackgroundResource(
            R.drawable.bg_dashboard_card
        )

        card.isClickable = true
        card.isFocusable = true


        // --------------------------------
        // NAME
        // --------------------------------

        val name =
            TextView(this)

        name.text =
            "${student.first_name} ${student.last_name}"

        name.textSize =
            19f

        name.setTextColor(
            android.graphics.Color.parseColor("#151528")
        )

        name.setTypeface(
            null,
            Typeface.BOLD
        )


        // --------------------------------
        // ENROLLMENT / ROLL
        // --------------------------------

        val enrollment =
            TextView(this)

        enrollment.text =
            "Roll No: ${student.enrollment_number}"

        enrollment.textSize =
            14f

        enrollment.setTextColor(
            android.graphics.Color.parseColor("#555568")
        )

        enrollment.setPadding(
            0,
            6,
            0,
            0
        )


        // --------------------------------
        // DROPOUT RISK
        // --------------------------------

        val risk =
            TextView(this)

        risk.text =
            "Dropout Risk: ${student.dropout_risk}%"

        risk.textSize =
            15f

        risk.setTextColor(
            android.graphics.Color.parseColor("#DC2626")
        )

        risk.setPadding(
            0,
            8,
            0,
            0
        )


        // --------------------------------
        // RISK LEVEL
        // --------------------------------

        val status =
            TextView(this)

        status.text =
            "Risk Level: ${student.risk_level}"

        status.textSize =
            14f

        status.setTextColor(
            android.graphics.Color.parseColor("#555568")
        )


        // --------------------------------
        // ATTENDANCE
        // --------------------------------

        val attendance =
            TextView(this)

        attendance.text =
            "Attendance: ${student.attendance}%"

        attendance.textSize =
            14f

        attendance.setTextColor(
            android.graphics.Color.parseColor("#555568")
        )


        // --------------------------------
        // ACADEMIC
        // --------------------------------

        val academic =
            TextView(this)

        academic.text =
            "Academic: ${student.academic}%"

        academic.textSize =
            14f

        academic.setTextColor(
            android.graphics.Color.parseColor("#555568")
        )


        // --------------------------------
        // ADD VIEWS
        // --------------------------------

        card.addView(name)
        card.addView(enrollment)
        card.addView(risk)
        card.addView(status)
        card.addView(attendance)
        card.addView(academic)


        // --------------------------------
        // CARD MARGIN
        // --------------------------------

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            0,
            0,
            16
        )

        card.layoutParams =
            params


        // --------------------------------
        // OPEN DETAILED ANALYSIS
        // --------------------------------

        card.setOnClickListener {

            val intent =
                Intent(
                    this,
                    DetailedAnalysisActivity::class.java
                )

            intent.putExtra(
                "student_id",
                student.student_id
            )

            startActivity(
                intent
            )
        }


        studentsContainer.addView(
            card
        )
    }


    // ==================================================
    // ERROR
    // ==================================================

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