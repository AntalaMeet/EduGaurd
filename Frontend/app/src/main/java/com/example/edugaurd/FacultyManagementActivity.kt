package com.example.edugaurd

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.edugaurd.api.FacultyAdmin
import com.example.edugaurd.api.FacultyAdminResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FacultyManagementActivity : AppCompatActivity() {

    private lateinit var facultyContainer: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var tvFacultyCount: TextView

    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_faculty_management
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

        initializeViews()
        setupButtons()
        findViewById<Button>(
            R.id.btnAddFaculty
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AddFacultyActivity::class.java
                )
            )
        }
        swipeRefresh.setOnRefreshListener {

            loadFaculty()
        }
        loadFaculty()
    }


    private fun initializeViews() {

        facultyContainer =
            findViewById(
                R.id.facultyContainer
            )

        tvLoading =
            findViewById(
                R.id.tvLoading
            )

        tvFacultyCount =
            findViewById(
                R.id.tvFacultyCount
            )

        swipeRefresh =
            findViewById(R.id.swipeRefresh)
    }


    private fun setupButtons() {

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {

            finish()
        }
    }


    private fun loadFaculty() {

        RetrofitClient.api
            .getAllFaculty()
            .enqueue(
                object : Callback<FacultyAdminResponse> {

                    override fun onResponse(
                        call: Call<FacultyAdminResponse>,
                        response: Response<FacultyAdminResponse>
                    ) {

                        swipeRefresh.isRefreshing = false

                        if (!response.isSuccessful) {

                            tvLoading.text =
                                "Server error: ${response.code()}"

                            tvLoading.visibility =
                                View.VISIBLE

                            return
                        }

                        val data = response.body()

                        if (data == null) {

                            tvLoading.text =
                                "Empty server response"

                            tvLoading.visibility =
                                View.VISIBLE

                            return
                        }

                        if (!data.success) {

                            tvLoading.text =
                                "Unable to load faculty"

                            tvLoading.visibility =
                                View.VISIBLE

                            return
                        }

                        displayFaculty(
                            data.faculty
                        )
                    }

                    override fun onFailure(
                        call: Call<FacultyAdminResponse>,
                        t: Throwable
                    ) {

                        swipeRefresh.isRefreshing = false

                        tvLoading.text =
                            "Unable to load faculty"

                        tvLoading.visibility =
                            View.VISIBLE

                        Toast.makeText(
                            this@FacultyManagementActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun displayFaculty(
        facultyList: List<FacultyAdmin>
    ) {

        tvLoading.visibility =
            View.GONE

        facultyContainer.removeAllViews()

        tvFacultyCount.text =
            "${facultyList.size} faculty members"


        if (facultyList.isEmpty()) {

            val emptyText =
                TextView(this)

            emptyText.text =
                "No faculty members found."

            emptyText.textSize =
                16f

            emptyText.setTextColor(
                android.graphics.Color.parseColor(
                    "#555568"
                )
            )

            emptyText.setPadding(
                20,
                30,
                20,
                30
            )

            facultyContainer.addView(
                emptyText
            )

            return
        }


        for (faculty in facultyList) {

            addFacultyCard(
                faculty
            )
        }
    }


    private fun addFacultyCard(
        faculty: FacultyAdmin
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


        // -------------------------
        // NAME
        // -------------------------

        val name =
            TextView(this)

        name.text =
            "${faculty.first_name ?: ""} " +
                    "${faculty.last_name ?: ""}"

        name.textSize =
            19f

        name.setTextColor(
            android.graphics.Color.parseColor(
                "#151528"
            )
        )

        name.setTypeface(
            null,
            Typeface.BOLD
        )


        // -------------------------
        // FACULTY ID
        // -------------------------

        val id =
            TextView(this)

        id.text =
            "Faculty ID: ${faculty.faculty_id}"

        id.textSize =
            14f

        id.setTextColor(
            android.graphics.Color.parseColor(
                "#555568"
            )
        )

        id.setPadding(
            0,
            6,
            0,
            0
        )


        // -------------------------
        // EMAIL
        // -------------------------

        val email =
            TextView(this)

        email.text =
            "Email: ${faculty.email ?: "--"}"

        email.textSize =
            14f

        email.setTextColor(
            android.graphics.Color.parseColor(
                "#555568"
            )
        )

        email.setPadding(
            0,
            4,
            0,
            0
        )


        // -------------------------
        // DIVISION
        // -------------------------

        val division =
            TextView(this)

        division.text =
            "Division: ${
                faculty.division_code
                    ?: faculty.division_id?.toString()
                    ?: "--"
            }"

        division.textSize =
            14f

        division.setTextColor(
            android.graphics.Color.parseColor(
                "#555568"
            )
        )

        division.setPadding(
            0,
            4,
            0,
            0
        )

        val assignments = TextView(this)

        assignments.textSize = 14f

        assignments.setTextColor(
            android.graphics.Color.parseColor(
                "#555568"
            )
        )

        assignments.setPadding(
            0,
            12,
            0,
            0
        )
        val assignmentText = StringBuilder()

        if (faculty.assignments.isEmpty()) {

            assignmentText.append(
                "Teaching: No subjects assigned"
            )

        } else {

            assignmentText.append(
                "Teaching:\n"
            )

            var currentDivisionId: Int? = null

            for (assignment in faculty.assignments) {

                if (currentDivisionId != assignment.division_id) {

                    if (currentDivisionId != null) {
                        assignmentText.append("\n")
                    }

                    assignmentText.append(
                        "${assignment.division_code}\n"
                    )

                    currentDivisionId =
                        assignment.division_id
                }

                assignmentText.append(
                    "  • ${assignment.subject_code} - " +
                            "${assignment.subject_name}\n"
                )
            }
        }

        assignments.text =
            assignmentText.toString()

        card.addView(name)
        card.addView(id)
        card.addView(email)
        card.addView(division)
        card.addView(assignments)


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


        facultyContainer.addView(
            card
        )
    }
}