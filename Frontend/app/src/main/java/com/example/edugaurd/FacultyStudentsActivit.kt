package com.example.edugaurd

import android.content.Intent
import android.graphics.Color
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

class FacultyStudentsActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var studentsContainer: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var facultyId = -1
    private var divisionId = -1
    private var divisionCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_faculty_students
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        initializeViews()

        facultyId =
            intent.getIntExtra(
                "faculty_id",
                -1
            )

        divisionId =
            intent.getIntExtra(
                "division_id",
                -1
            )

        divisionCode =
            intent.getStringExtra(
                "division_code"
            ) ?: ""

        if (
            facultyId == -1 ||
            divisionId == -1
        ) {

            Toast.makeText(
                this,
                "Invalid class information",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        setupButtons()

        tvTitle.text =
            divisionCode

        tvSubtitle.text =
            "Students in $divisionCode"

        loadStudents()
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

    private fun initializeViews() {

        tvTitle =
            findViewById(
                R.id.tvTitle
            )

        tvSubtitle =
            findViewById(
                R.id.tvSubtitle
            )

        tvStudentCount =
            findViewById(
                R.id.tvStudentCount
            )

        studentsContainer =
            findViewById(
                R.id.studentsContainer
            )

        tvLoading =
            findViewById(
                R.id.tvLoading
            )

        swipeRefresh =
            findViewById(
                R.id.swipeRefresh
            )
    }


    // =====================================================
    // BUTTONS
    // =====================================================

    private fun setupButtons() {

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {

            finish()
        }

        RefreshHelper.setup(
            swipeRefresh
        ) {

            loadStudents()
        }
    }


    // =====================================================
    // LOAD
    // =====================================================

    private fun loadStudents() {

        tvLoading.visibility =
            View.VISIBLE

        tvLoading.text =
            "Loading students..."

        studentsContainer.removeAllViews()

        RetrofitClient.api
            .getFacultyDashboard(
                facultyId
            )
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

                            showLoadingError(
                                "Server error: ${response.code()}"
                            )

                            return
                        }

                        val data =
                            response.body()

                        if (data == null) {

                            showLoadingError(
                                "Empty server response"
                            )

                            return
                        }

                        val students =
                            data.students
                                .filter {
                                    it.division_id ==
                                            divisionId
                                }
                                .sortedByDescending {
                                    it.dropout_risk
                                }

                        displayStudents(
                            students
                        )
                    }


                    override fun onFailure(
                        call: Call<TeacherDashboardResponse>,
                        t: Throwable
                    ) {

                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        showLoadingError(
                            "Unable to load students"
                        )

                        Toast.makeText(
                            this@FacultyStudentsActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }


    // =====================================================
    // DISPLAY
    // =====================================================

    private fun displayStudents(
        students: List<FacultyStudent>
    ) {

        tvLoading.visibility =
            View.GONE

        studentsContainer.removeAllViews()

        tvStudentCount.text =
            "${students.size} students"


        if (students.isEmpty()) {

            val emptyText =
                TextView(this)

            emptyText.text =
                "No students found in $divisionCode"

            emptyText.textSize =
                16f

            emptyText.setTextColor(
                Color.parseColor(
                    "#555568"
                )
            )

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


        for (student in students) {

            addStudentCard(
                student
            )
        }
    }


    // =====================================================
    // STUDENT CARD
    // =====================================================

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


        // NAME

        val name =
            TextView(this)

        name.text =
            "${student.first_name} ${student.last_name}"

        name.textSize =
            19f

        name.setTextColor(
            Color.parseColor(
                "#151528"
            )
        )

        name.setTypeface(
            null,
            Typeface.BOLD
        )


        // ROLL

        val roll =
            TextView(this)

        roll.text =
            "Roll No: ${student.enrollment_number}"

        roll.textSize =
            14f

        roll.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        roll.setPadding(
            0,
            6,
            0,
            0
        )


        // RISK

        val risk =
            TextView(this)

        risk.text =
            "Dropout Risk: ${student.dropout_risk}%"

        risk.textSize =
            15f

        risk.setTypeface(
            null,
            Typeface.BOLD
        )

        risk.setPadding(
            0,
            10,
            0,
            0
        )

        risk.setTextColor(
            getRiskColor(
                student.risk_level
            )
        )


        // RISK LEVEL

        val riskLevel =
            TextView(this)

        riskLevel.text =
            "Risk Level: ${student.risk_level}"

        riskLevel.textSize =
            14f

        riskLevel.setTextColor(
            getRiskColor(
                student.risk_level
            )
        )


        // ATTENDANCE

        val attendance =
            TextView(this)

        attendance.text =
            "Attendance: ${student.attendance}%"

        attendance.textSize =
            14f

        attendance.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        attendance.setPadding(
            0,
            8,
            0,
            0
        )


        // ACADEMIC

        val academic =
            TextView(this)

        academic.text =
            "Academic: ${student.academic}%"

        academic.textSize =
            14f

        academic.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        academic.setPadding(
            0,
            4,
            0,
            0
        )


        card.addView(name)
        card.addView(roll)
        card.addView(risk)
        card.addView(riskLevel)
        card.addView(attendance)
        card.addView(academic)


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


        // OPEN DETAIL

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


    // =====================================================
    // RISK COLOR
    // =====================================================

    private fun getRiskColor(
        riskLevel: String
    ): Int {

        return when (
            riskLevel.uppercase()
        ) {

            "HIGH" ->
                Color.parseColor(
                    "#DC2626"
                )

            "MEDIUM" ->
                Color.parseColor(
                    "#D97706"
                )

            "LOW" ->
                Color.parseColor(
                    "#16A34A"
                )

            else ->
                Color.parseColor(
                    "#555568"
                )
        }
    }


    // =====================================================
    // ERROR
    // =====================================================

    private fun showLoadingError(
        message: String
    ) {

        tvLoading.visibility =
            View.VISIBLE

        tvLoading.text =
            message

        tvLoading.setTextColor(
            Color.parseColor(
                "#DC2626"
            )
        )
    }
}