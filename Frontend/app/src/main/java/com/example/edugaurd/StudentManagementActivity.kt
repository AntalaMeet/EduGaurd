package com.example.edugaurd

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edugaurd.api.AdminStudent
import com.example.edugaurd.api.AdminStudentResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentManagementActivity : AppCompatActivity() {

    private lateinit var studentsContainer: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var etSearch: EditText
    private lateinit var spDivision: Spinner
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var allStudents: List<AdminStudent> = emptyList()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_student_management
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
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
        setupDivisionSpinner()
        setupButtons()

        RefreshHelper.setup(
            swipeRefresh
        ) {
            loadStudents()
        }

        loadStudents()
    }


    private fun initializeViews() {

        studentsContainer =
            findViewById(
                R.id.studentsContainer
            )

        tvLoading =
            findViewById(
                R.id.tvLoading
            )

        tvStudentCount =
            findViewById(
                R.id.tvStudentCount
            )

        etSearch =
            findViewById(
                R.id.etSearch
            )

        spDivision =
            findViewById(
                R.id.spDivision
            )

        swipeRefresh =
            findViewById(
                R.id.swipeRefresh
            )
    }


    private fun setupDivisionSpinner() {

        val divisions =
            listOf(
                "All Divisions",
                "DIV-1",
                "DIV-2",
                "DIV-3",
                "DIV-4",
                "DIV-5",
                "DIV-6",
                "DIV-7",
                "DIV-8"
            )

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                divisions
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spDivision.adapter =
            adapter

        spDivision.onItemSelectedListener =
            object :
                android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    filterStudents()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }
    }


    private fun setupButtons() {

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {

            finish()
        }


        etSearch.addTextChangedListener(
            object :
                android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    filterStudents()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {
                }
            }
        )
    }


    private fun loadStudents() {

        RetrofitClient.api
            .getAllStudents()
            .enqueue(
                object :
                    Callback<AdminStudentResponse> {

                    override fun onResponse(
                        call: Call<AdminStudentResponse>,
                        response: Response<AdminStudentResponse>
                    ) {

                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        if (!response.isSuccessful) {

                            tvLoading.text =
                                "Server error: ${response.code()}"

                            tvLoading.visibility =
                                View.VISIBLE

                            return
                        }

                        val data =
                            response.body()

                        if (data == null) {

                            tvLoading.text =
                                "Empty server response"

                            tvLoading.visibility =
                                View.VISIBLE

                            return
                        }

                        if (!data.success) {

                            tvLoading.text =
                                "Unable to load students"

                            tvLoading.visibility =
                                View.VISIBLE

                            return
                        }

                        allStudents =
                            data.students

                        tvLoading.visibility =
                            View.GONE

                        filterStudents()
                    }


                    override fun onFailure(
                        call: Call<AdminStudentResponse>,
                        t: Throwable
                    ) {

                        RefreshHelper.stop(
                            swipeRefresh
                        )

                        tvLoading.text =
                            "Unable to load students"

                        tvLoading.visibility =
                            View.VISIBLE

                        Toast.makeText(
                            this@StudentManagementActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }


    private fun filterStudents() {

        if (allStudents.isEmpty()) {
            return
        }

        val search =
            etSearch.text
                .toString()
                .trim()
                .lowercase()

        val selectedDivision =
            spDivision.selectedItem
                ?.toString()
                ?: "All Divisions"


        val filtered =
            allStudents.filter { student ->

                val name =
                    "${student.first_name} ${student.last_name}"
                        .lowercase()

                val enrollment =
                    student.enrollment_number
                        .lowercase()

                val matchesSearch =
                    search.isEmpty() ||
                            name.contains(search) ||
                            enrollment.contains(search)

                val matchesDivision =
                    selectedDivision == "All Divisions" ||
                            student.division_code
                                ?.equals(
                                    selectedDivision,
                                    ignoreCase = true
                                ) == true

                matchesSearch &&
                        matchesDivision
            }


        displayStudents(
            filtered
        )
    }


    private fun displayStudents(
        students: List<AdminStudent>
    ) {

        studentsContainer.removeAllViews()

        tvStudentCount.text =
            "${students.size} students"


        if (students.isEmpty()) {

            val emptyText =
                TextView(this)

            emptyText.text =
                "No students found."

            emptyText.textSize =
                16f

            emptyText.setTextColor(
                Color.parseColor(
                    "#555568"
                )
            )

            emptyText.gravity =
                android.view.Gravity.CENTER

            emptyText.setPadding(
                20,
                40,
                20,
                40
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


    private fun addStudentCard(
        student: AdminStudent
    ) {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setPadding(
            20,
            18,
            20,
            18
        )

        card.setBackgroundResource(
            R.drawable.bg_dashboard_card
        )

        card.isClickable =
            true

        card.isFocusable =
            true


        // NAME

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


        // ENROLLMENT

        val enrollment =
            TextView(this)

        enrollment.text =
            "Roll No: ${student.enrollment_number}"

        enrollment.textSize =
            14f

        enrollment.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        enrollment.setPadding(
            0,
            5,
            0,
            0
        )


        // DIVISION

        val division =
            TextView(this)

        division.text =
            "Division: ${
                student.division_code
                    ?: student.division_id?.toString()
                    ?: "--"
            }"

        division.textSize =
            14f

        division.setTextColor(
            Color.parseColor(
                "#555568"
            )
        )

        division.setPadding(
            0,
            4,
            0,
            0
        )


        card.addView(
            name
        )

        card.addView(
            enrollment
        )

        card.addView(
            division
        )


        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            0,
            0,
            12
        )

        card.layoutParams =
            params


        // OPEN DETAILS

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
}