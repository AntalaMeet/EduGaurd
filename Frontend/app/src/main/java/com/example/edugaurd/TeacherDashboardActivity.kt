package com.example.edugaurd

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edugaurd.api.FacultyAssignment
import com.example.edugaurd.api.RetrofitClient
import com.example.edugaurd.api.TeacherDashboardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var tvTeacherName: TextView
    private lateinit var tvFacultyEmail: TextView

    private lateinit var tvHighRisk: TextView
    private lateinit var tvMediumRisk: TextView
    private lateinit var tvLowRisk: TextView

    private lateinit var tvAverageAttendance: TextView
    private lateinit var tvAverageAcademic: TextView

    private lateinit var mentorContainer: LinearLayout
    private lateinit var subjectContainer: LinearLayout

    private lateinit var cardHighRisk: LinearLayout
    private lateinit var cardMediumRisk: LinearLayout
    private lateinit var cardLowRisk: LinearLayout

    private lateinit var btnLogout: ImageButton
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var facultyId = -1


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_teacher_dashboard
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

        if (facultyId == -1) {

            Toast.makeText(
                this,
                "Faculty ID not found",
                Toast.LENGTH_LONG
            ).show()

            finish()

            return
        }

        setupButtons()

        loadTeacherDashboard()
    }


    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private fun initializeViews() {

        tvTeacherName =
            findViewById(
                R.id.tvTeacherName
            )

        tvFacultyEmail =
            findViewById(
                R.id.tvFacultyEmail
            )

        tvHighRisk =
            findViewById(
                R.id.tvHighRisk
            )

        tvMediumRisk =
            findViewById(
                R.id.tvMediumRisk
            )

        tvLowRisk =
            findViewById(
                R.id.tvLowRisk
            )

        tvAverageAttendance =
            findViewById(
                R.id.tvAverageAttendance
            )

        tvAverageAcademic =
            findViewById(
                R.id.tvAverageAcademic
            )

        mentorContainer =
            findViewById(
                R.id.mentorContainer
            )

        subjectContainer =
            findViewById(
                R.id.subjectContainer
            )

        cardHighRisk =
            findViewById(
                R.id.cardHighRisk
            )

        cardMediumRisk =
            findViewById(
                R.id.cardMediumRisk
            )

        cardLowRisk =
            findViewById(
                R.id.cardLowRisk
            )

        btnLogout =
            findViewById(
                R.id.btnLogout
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

        btnLogout.setOnClickListener {

            logout()
        }


        cardHighRisk.setOnClickListener {

            openRiskStudents(
                "HIGH"
            )
        }


        cardMediumRisk.setOnClickListener {

            openRiskStudents(
                "MEDIUM"
            )
        }


        cardLowRisk.setOnClickListener {

            openRiskStudents(
                "LOW"
            )
        }


        RefreshHelper.setup(
            swipeRefresh
        ) {

            loadTeacherDashboard()
        }
    }


    // =====================================================
    // LOAD DASHBOARD
    // =====================================================

    private fun loadTeacherDashboard() {

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

                            showError(
                                "Server error: ${response.code()}"
                            )

                            return
                        }

                        val data =
                            response.body()

                        if (data == null) {

                            showError(
                                "Empty dashboard response"
                            )

                            return
                        }

                        displayDashboard(
                            data
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


    // =====================================================
    // DISPLAY DASHBOARD
    // =====================================================

    private fun displayDashboard(
        data: TeacherDashboardResponse
    ) {

        // FACULTY

        tvTeacherName.text =
            "${data.faculty.first_name} " +
                    data.faculty.last_name

        tvFacultyEmail.text =
            data.faculty.email
                ?: "Faculty"


        // RISK

        tvHighRisk.text =
            data.overview.high_risk.toString()

        tvMediumRisk.text =
            data.overview.medium_risk.toString()

        tvLowRisk.text =
            data.overview.low_risk.toString()


        // PERFORMANCE

        tvAverageAttendance.text =
            "${data.overview.average_attendance}%"

        tvAverageAcademic.text =
            "${data.overview.average_academic}%"


        // ACCESS

        displayMentorDivisions(
            data
        )

        displaySubjectAssignments(
            data
        )
    }


    // =====================================================
    // MY CLASS
    // =====================================================

    private fun displayMentorDivisions(
        data: TeacherDashboardResponse
    ) {

        mentorContainer.removeAllViews()

        if (
            data.mentor_divisions.isEmpty()
        ) {

            addEmptyText(
                mentorContainer,
                "No class mentor assignment"
            )

            return
        }


        for (
        mentor in data.mentor_divisions
        ) {

            val studentCount =
                data.students.count {

                    it.division_id ==
                            mentor.division_id
                }

            addMentorCard(
                mentor.division_id,
                mentor.division_code,
                studentCount
            )
        }
    }


    // =====================================================
    // MY SUBJECTS
    // =====================================================

    private fun displaySubjectAssignments(
        data: TeacherDashboardResponse
    ) {

        subjectContainer.removeAllViews()

        val assignments =
            data.assignments.distinctBy {

                "${it.subject_id}_${it.division_id}"
            }


        if (assignments.isEmpty()) {

            addEmptyText(
                subjectContainer,
                "No subject assignments"
            )

            return
        }


        for (
        assignment in assignments
        ) {

            val studentCount =
                data.students.count {

                    it.division_id ==
                            assignment.division_id
                }

            addSubjectCard(
                assignment,
                studentCount
            )
        }
    }


    // =====================================================
    // MENTOR CARD
    // =====================================================

    private fun addMentorCard(
        divisionId: Int,
        divisionCode: String,
        studentCount: Int
    ) {

        val card =
            createCard()


        val title =
            createTitle(
                divisionCode
            )


        val subtitle =
            createSubtitle(
                "Class Mentor"
            )


        val students =
            createSubtitle(
                "$studentCount Students"
            )

        students.setPadding(
            0,
            8,
            0,
            0
        )


        card.addView(title)

        card.addView(subtitle)

        card.addView(students)


        // IMPORTANT:
        // Click opens DIVISION risk dashboard

        card.setOnClickListener {

            openDivisionRisk(
                divisionId,
                divisionCode
            )
        }


        mentorContainer.addView(
            card
        )
    }


    // =====================================================
    // SUBJECT CARD
    // =====================================================

    private fun addSubjectCard(
        assignment: FacultyAssignment,
        studentCount: Int
    ) {

        val card =
            createCard()


        val title =
            createTitle(
                assignment.subject_name
            )


        val subject =
            createSubtitle(
                "${assignment.subject_code} • " +
                        assignment.division_code
            )


        val students =
            createSubtitle(
                "$studentCount Students"
            )

        students.setPadding(
            0,
            8,
            0,
            0
        )


        card.addView(title)

        card.addView(subject)

        card.addView(students)


        // IMPORTANT:
        // Click opens SUBJECT risk dashboard

        card.setOnClickListener {

            openSubjectRisk(
                assignment.division_id,
                assignment.division_code,
                assignment.subject_id
            )
        }


        subjectContainer.addView(
            card
        )
    }


    // =====================================================
    // CARD
    // =====================================================

    private fun createCard():
            LinearLayout {

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

        return card
    }


    // =====================================================
    // TITLE
    // =====================================================

    private fun createTitle(
        text: String
    ): TextView {

        val view =
            TextView(this)

        view.text =
            text

        view.textSize =
            19f

        view.setTextColor(
            Color.parseColor(
                "#151528"
            )
        )

        view.setTypeface(
            null,
            Typeface.BOLD
        )

        return view
    }


    // =====================================================
    // SUBTITLE
    // =====================================================

    private fun createSubtitle(
        text: String
    ): TextView {

        val view =
            TextView(this)

        view.text =
            text

        view.textSize =
            14f

        view.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        return view
    }


    // =====================================================
    // EMPTY
    // =====================================================

    private fun addEmptyText(
        container: LinearLayout,
        text: String
    ) {

        val view =
            TextView(this)

        view.text =
            text

        view.textSize =
            15f

        view.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        view.setPadding(
            20,
            20,
            20,
            20
        )

        container.addView(
            view
        )
    }


    // =====================================================
    // DIVISION RISK
    // =====================================================

    private fun openDivisionRisk(
        divisionId: Int,
        divisionCode: String
    ) {

        val intent =
            Intent(
                this,
                FacultyRiskDashboardActivity::class.java
            )

        intent.putExtra(
            "faculty_id",
            facultyId
        )

        intent.putExtra(
            "division_id",
            divisionId
        )

        intent.putExtra(
            "division_code",
            divisionCode
        )

        startActivity(
            intent
        )
    }


    // =====================================================
    // SUBJECT RISK
    // =====================================================

    private fun openSubjectRisk(
        divisionId: Int,
        divisionCode: String,
        subjectId: Int
    ) {

        val intent =
            Intent(
                this,
                FacultyRiskDashboardActivity::class.java
            )

        intent.putExtra(
            "faculty_id",
            facultyId
        )

        intent.putExtra(
            "division_id",
            divisionId
        )

        intent.putExtra(
            "division_code",
            divisionCode
        )

        intent.putExtra(
            "subject_id",
            subjectId
        )

        startActivity(
            intent
        )
    }


    // =====================================================
    // RISK CARDS
    // =====================================================

    private fun openRiskStudents(
        riskLevel: String
    ) {

        val intent =
            Intent(
                this,
                FacultyStudentsActivity::class.java
            )

        intent.putExtra(
            "faculty_id",
            facultyId
        )

        intent.putExtra(
            "risk_level",
            riskLevel
        )

        startActivity(
            intent
        )
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    private fun logout() {

        getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()


        val intent =
            Intent(
                this,
                LoginActivity::class.java
            )


        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK


        startActivity(
            intent
        )

        finish()
    }


    // =====================================================
    // ERROR
    // =====================================================

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