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
import com.example.edugaurd.api.FacultyRiskResponse
import com.example.edugaurd.api.FacultyRiskStudent
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyRiskDashboardActivity : AppCompatActivity() {

    // =====================================================
    // VIEWS
    // =====================================================

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvStudentCount: TextView

    private lateinit var tvHighRisk: TextView
    private lateinit var tvMediumRisk: TextView
    private lateinit var tvLowRisk: TextView

    private lateinit var tvAverageAttendance: TextView
    private lateinit var tvAverageAcademic: TextView

    private lateinit var studentsContainer: LinearLayout
    private lateinit var tvLoading: TextView

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var cardHighRisk: LinearLayout
    private lateinit var cardMediumRisk: LinearLayout
    private lateinit var cardLowRisk: LinearLayout


    // =====================================================
    // DATA
    // =====================================================

    private var facultyId = -1

    private var divisionId = -1

    private var subjectId: Int? = null

    /*
     * Keeps the complete list received from API.
     *
     * We need this because when the user changes
     * HIGH -> MEDIUM -> LOW, we don't want to
     * call the API again.
     */
    private var allStudents =
        emptyList<FacultyRiskStudent>()


    /*
     * Currently selected filter.
     *
     * null     = show all
     * HIGH     = show high-risk students
     * MEDIUM   = show medium-risk students
     * LOW      = show low-risk students
     */
    private var selectedRisk: String? = null


    // =====================================================
    // ON CREATE
    // =====================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_faculty_risk_dashboard
        )


        // -------------------------------------------------
        // SYSTEM BAR / EDGE TO EDGE
        // -------------------------------------------------

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


        // -------------------------------------------------
        // INITIALIZE
        // -------------------------------------------------

        initializeViews()


        // -------------------------------------------------
        // READ INTENT
        // -------------------------------------------------

        readIntentData()


        // -------------------------------------------------
        // BUTTONS
        // -------------------------------------------------

        setupButtons()


        // -------------------------------------------------
        // LOAD DATA
        // -------------------------------------------------

        loadRiskDashboard()
    }


    // =====================================================
    // INITIALIZE VIEWS
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


        // -------------------------------------------------
        // RISK CARDS
        // -------------------------------------------------

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
    }


    // =====================================================
    // READ INTENT DATA
    // =====================================================

    private fun readIntentData() {

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


        if (
            intent.hasExtra(
                "subject_id"
            )
        ) {

            subjectId =
                intent.getIntExtra(
                    "subject_id",
                    -1
                )


            if (
                subjectId == -1
            ) {

                subjectId = null
            }
        }


        // -------------------------------------------------
        // VALIDATION
        // -------------------------------------------------

        if (
            facultyId == -1 ||
            divisionId == -1
        ) {

            Toast.makeText(
                this,
                "Invalid risk information",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }


    // =====================================================
    // BUTTONS
    // =====================================================

    private fun setupButtons() {

        // -------------------------------------------------
        // BACK
        // -------------------------------------------------

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {

            finish()
        }


        // -------------------------------------------------
        // HIGH RISK
        // -------------------------------------------------

        cardHighRisk.setOnClickListener {

            toggleRiskFilter(
                "HIGH"
            )
        }


        // -------------------------------------------------
        // MEDIUM RISK
        // -------------------------------------------------

        cardMediumRisk.setOnClickListener {

            toggleRiskFilter(
                "MEDIUM"
            )
        }


        // -------------------------------------------------
        // LOW RISK
        // -------------------------------------------------

        cardLowRisk.setOnClickListener {

            toggleRiskFilter(
                "LOW"
            )
        }


        // -------------------------------------------------
        // SWIPE REFRESH
        // -------------------------------------------------

        RefreshHelper.setup(
            swipeRefresh
        ) {

            loadRiskDashboard()
        }
    }


    // =====================================================
    // RISK FILTER
    // =====================================================

    private fun toggleRiskFilter(
        risk: String
    ) {

        /*
         * Clicking the currently selected card
         * removes the filter and shows everybody.
         */

        if (
            selectedRisk == risk
        ) {

            selectedRisk = null

            updateRiskCardSelection()

            displayStudents(
                allStudents
            )

            tvLoading.visibility =
                View.GONE

            return
        }


        /*
         * Select new risk.
         */

        selectedRisk =
            risk


        updateRiskCardSelection()


        /*
         * Filter locally.
         *
         * No API call is necessary.
         */

        val filteredStudents =
            allStudents.filter {

                it.risk_level.uppercase() ==
                        risk.uppercase()
            }


        displayStudents(
            filteredStudents
        )


        tvLoading.visibility =
            View.GONE
    }


    // =====================================================
    // UPDATE CARD SELECTION
    // =====================================================

    private fun updateRiskCardSelection() {

        /*
         * Selected card = full opacity
         *
         * Other cards = slightly faded
         */

        cardHighRisk.alpha =
            if (
                selectedRisk == null ||
                selectedRisk == "HIGH"
            ) {

                1f

            } else {

                0.45f
            }


        cardMediumRisk.alpha =
            if (
                selectedRisk == null ||
                selectedRisk == "MEDIUM"
            ) {

                1f

            } else {

                0.45f
            }


        cardLowRisk.alpha =
            if (
                selectedRisk == null ||
                selectedRisk == "LOW"
            ) {

                1f

            } else {

                0.45f
            }
    }


    // =====================================================
    // LOAD RISK DASHBOARD
    // =====================================================

    private fun loadRiskDashboard() {

        tvLoading.visibility =
            View.VISIBLE


        tvLoading.text =
            "Loading risk data..."


        /*
         * Reset filter whenever fresh data
         * comes from the server.
         */

        selectedRisk = null

        updateRiskCardSelection()


        RetrofitClient.api
            .getFacultyRisk(
                facultyId,
                divisionId,
                subjectId
            )
            .enqueue(
                object :
                    Callback<FacultyRiskResponse> {


                    // =====================================
                    // SUCCESS
                    // =====================================

                    override fun onResponse(
                        call: Call<FacultyRiskResponse>,
                        response: Response<FacultyRiskResponse>
                    ) {

                        RefreshHelper.stop(
                            swipeRefresh
                        )


                        if (
                            !response.isSuccessful
                        ) {

                            showError(
                                "Server error: ${response.code()}"
                            )

                            return
                        }


                        val data =
                            response.body()


                        if (
                            data == null
                        ) {

                            showError(
                                "Empty server response"
                            )

                            return
                        }


                        if (
                            !data.success
                        ) {

                            showError(
                                "Unable to load risk data"
                            )

                            return
                        }


                        displayDashboard(
                            data
                        )
                    }


                    // =====================================
                    // FAILURE
                    // =====================================

                    override fun onFailure(
                        call: Call<FacultyRiskResponse>,
                        t: Throwable
                    ) {

                        RefreshHelper.stop(
                            swipeRefresh
                        )


                        showError(
                            "Unable to load risk data"
                        )


                        Toast.makeText(
                            this@FacultyRiskDashboardActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()


                        t.printStackTrace()
                    }
                }
            )
    }


    // =====================================================
    // DISPLAY DASHBOARD
    // =====================================================

    private fun displayDashboard(
        data: FacultyRiskResponse
    ) {

        tvLoading.visibility =
            View.GONE


        // -------------------------------------------------
        // TITLE
        // -------------------------------------------------

        if (
            data.mode.uppercase() ==
            "SUBJECT"
        ) {

            tvTitle.text =
                data.subject_name
                    ?: data.subject_code
                            ?: "Subject"


            tvSubtitle.text =
                "${data.subject_code ?: ""} • " +
                        data.division_code

        } else {

            tvTitle.text =
                data.division_code


            tvSubtitle.text =
                "Division Risk Overview"
        }


        // -------------------------------------------------
        // STUDENT COUNT
        // -------------------------------------------------

        tvStudentCount.text =
            "${data.total_students} Students"


        // -------------------------------------------------
        // RISK COUNTS
        // -------------------------------------------------

        tvHighRisk.text =
            data.high_risk.toString()


        tvMediumRisk.text =
            data.medium_risk.toString()


        tvLowRisk.text =
            data.low_risk.toString()


        // -------------------------------------------------
        // PERFORMANCE
        // -------------------------------------------------

        tvAverageAttendance.text =
            "${data.average_attendance}%"


        tvAverageAcademic.text =
            "${data.average_academic}%"


        // -------------------------------------------------
        // STORE COMPLETE STUDENT LIST
        // -------------------------------------------------

        allStudents =
            data.students


        selectedRisk = null


        updateRiskCardSelection()


        // -------------------------------------------------
        // DISPLAY ALL STUDENTS
        // -------------------------------------------------

        displayStudents(
            allStudents
        )
    }


    // =====================================================
    // DISPLAY STUDENTS
    // =====================================================

    private fun displayStudents(
        students: List<FacultyRiskStudent>
    ) {

        studentsContainer.removeAllViews()


        // -------------------------------------------------
        // SORT HIGH RISK FIRST
        // -------------------------------------------------

        val sortedStudents =
            students.sortedByDescending {

                it.dropout_risk
            }


        // -------------------------------------------------
        // EMPTY
        // -------------------------------------------------

        if (
            sortedStudents.isEmpty()
        ) {

            val emptyText =
                TextView(this)


            emptyText.text =
                if (
                    selectedRisk != null
                ) {

                    "No ${selectedRisk!!.lowercase()} risk students"

                } else {

                    "No students found"
                }


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


        // -------------------------------------------------
        // ADD STUDENT CARDS
        // -------------------------------------------------

        for (
        student in sortedStudents
        ) {

            addStudentCard(
                student
            )
        }
    }


    // =====================================================
    // STUDENT CARD
    // =====================================================

    private fun addStudentCard(
        student: FacultyRiskStudent
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


        card.isClickable =
            true


        card.isFocusable =
            true


        // -------------------------------------------------
        // NAME
        // -------------------------------------------------

        val name =
            TextView(this)


        name.text =
            "${student.first_name} ${student.last_name}"


        name.textSize =
            18f


        name.setTextColor(
            Color.parseColor(
                "#151528"
            )
        )


        name.setTypeface(
            null,
            Typeface.BOLD
        )


        // -------------------------------------------------
        // ROLL NUMBER
        // -------------------------------------------------

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


        // -------------------------------------------------
        // DROPOUT RISK
        // -------------------------------------------------

        val risk =
            TextView(this)


        risk.text =
            "Dropout Risk: ${student.dropout_risk}%"


        risk.textSize =
            16f


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


        // -------------------------------------------------
        // RISK LEVEL
        // -------------------------------------------------

        val level =
            TextView(this)


        level.text =
            "Risk Level: ${student.risk_level}"


        level.textSize =
            14f


        level.setTextColor(
            getRiskColor(
                student.risk_level
            )
        )


        // -------------------------------------------------
        // ATTENDANCE
        // -------------------------------------------------

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


        // -------------------------------------------------
        // ACADEMIC
        // -------------------------------------------------

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


        // -------------------------------------------------
        // ASSIGNMENTS
        // -------------------------------------------------

        val assignments =
            TextView(this)


        assignments.text =
            "Assignments: ${student.assignments}%"


        assignments.textSize =
            14f


        assignments.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )


        assignments.setPadding(
            0,
            4,
            0,
            0
        )


        // -------------------------------------------------
        // ADD VIEWS
        // -------------------------------------------------

        card.addView(
            name
        )


        card.addView(
            roll
        )


        card.addView(
            risk
        )


        card.addView(
            level
        )


        card.addView(
            attendance
        )


        card.addView(
            academic
        )


        card.addView(
            assignments
        )


        // -------------------------------------------------
        // CARD MARGIN
        // -------------------------------------------------

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


        // -------------------------------------------------
        // STUDENT CARD CLICK
        // -------------------------------------------------

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
        level: String
    ): Int {

        return when (
            level.uppercase()
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

    private fun showError(
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