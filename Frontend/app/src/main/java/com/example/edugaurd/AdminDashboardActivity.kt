package com.example.edugaurd

import android.content.Intent
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
import com.example.edugaurd.api.AdminDashboardResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalStudents: TextView
    private lateinit var tvTotalFaculty: TextView
    private lateinit var tvTotalDivisions: TextView
    private lateinit var tvTotalUsers: TextView

    private lateinit var btnLogout: ImageButton
    private lateinit var cardFacultyManagement: LinearLayout

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var cardStudents: LinearLayout


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_admin_dashboard
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

        // Pull-to-refresh
        RefreshHelper.setup(
            swipeRefresh
        ) {
            loadAdminDashboard()
        }

        // Initial loading
        loadAdminDashboard()
    }


    private fun initializeViews() {

        tvTotalStudents =
            findViewById(
                R.id.tvTotalStudents
            )

        tvTotalFaculty =
            findViewById(
                R.id.tvTotalFaculty
            )

        tvTotalDivisions =
            findViewById(
                R.id.tvTotalDivisions
            )

        tvTotalUsers =
            findViewById(
                R.id.tvTotalUsers
            )


        btnLogout =
            findViewById(
                R.id.btnLogout
            )


        cardFacultyManagement =
            findViewById(
                R.id.cardFacultyManagement
            )


        swipeRefresh =
            findViewById(
                R.id.swipeRefresh
            )

        cardStudents =
            findViewById(
                R.id.cardStudents
            )
    }


    private fun setupButtons() {

        // -------------------------
        // LOGOUT
        // -------------------------

        btnLogout.setOnClickListener {

            logout()
        }


        // -------------------------
        // FACULTY MANAGEMENT
        // -------------------------

        cardFacultyManagement.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    FacultyManagementActivity::class.java
                )
            )
        }
        cardStudents.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StudentManagementActivity::class.java
                )
            )
        }
    }


    private fun loadAdminDashboard() {

        RetrofitClient.api
            .getAdminDashboard()
            .enqueue(
                object :
                    Callback<AdminDashboardResponse> {

                    override fun onResponse(
                        call: Call<AdminDashboardResponse>,
                        response: Response<AdminDashboardResponse>
                    ) {

                        // Stop refresh spinner
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
                                "Empty response"
                            )

                            return
                        }


                        tvTotalStudents.text =
                            data.total_students.toString()


                        tvTotalFaculty.text =
                            data.total_faculty.toString()


                        tvTotalDivisions.text =
                            data.total_divisions.toString()


                        tvTotalUsers.text =
                            data.total_users.toString()
                    }


                    override fun onFailure(
                        call: Call<AdminDashboardResponse>,
                        t: Throwable
                    ) {

                        // Stop refresh spinner
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


        startActivity(intent)

        finish()
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