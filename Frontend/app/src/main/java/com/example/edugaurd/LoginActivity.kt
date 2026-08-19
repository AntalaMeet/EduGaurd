package com.example.edugaurd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugaurd.api.LoginRequest
import com.example.edugaurd.api.LoginResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_login)

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

        // Check existing login
        checkExistingLogin()
    }

    private fun checkExistingLogin() {

        val prefs = getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )

        val isLoggedIn =
            prefs.getBoolean(
                "isLoggedIn",
                false
            )

        if (!isLoggedIn) {

            initializeViews()
            setupLogin()
            return
        }

        val role =
            prefs.getString(
                "role",
                ""
            )?.uppercase()

        when (role) {

            // -------------------------
            // FACULTY
            // -------------------------

            "FACULTY" -> {

                val facultyId =
                    prefs.getInt(
                        "faculty_id",
                        -1
                    )

                if (facultyId != -1) {

                    openTeacherDashboard(
                        facultyId
                    )

                } else {

                    clearLogin()

                    initializeViews()
                    setupLogin()
                }
            }


            // -------------------------
            // STUDENT
            // -------------------------

            "STUDENT" -> {

                val studentId =
                    prefs.getInt(
                        "student_id",
                        -1
                    )

                if (studentId != -1) {

                    openStudentDashboard(
                        studentId
                    )

                } else {

                    clearLogin()

                    initializeViews()
                    setupLogin()
                }
            }


            // -------------------------
            // ADMIN
            // -------------------------

            "ADMIN" -> {

                openAdminDashboard()
            }


            // -------------------------
            // UNKNOWN ROLE
            // -------------------------

            else -> {

                clearLogin()

                initializeViews()
                setupLogin()
            }
        }
    }

    private fun initializeViews() {

        etUsername =
            findViewById(R.id.etUsername)

        etPassword =
            findViewById(R.id.etPassword)

        btnLogin =
            findViewById(R.id.btnLogin)

        tvForgotPassword =
            findViewById(R.id.tvForgotPassword)
    }

    private fun setupLogin() {

        btnLogin.setOnClickListener {

            val username =
                etUsername.text
                    .toString()
                    .trim()

            val password =
                etPassword.text
                    .toString()

            if (username.isEmpty()) {

                etUsername.error =
                    "Enter username"

                etUsername.requestFocus()

                return@setOnClickListener
            }

            if (password.isEmpty()) {

                etPassword.error =
                    "Enter password"

                etPassword.requestFocus()

                return@setOnClickListener
            }

            login(
                username,
                password
            )
        }

        tvForgotPassword.setOnClickListener {

            Toast.makeText(
                this,
                "Contact your administrator to reset your password.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun login(
        username: String,
        password: String
    ) {

        btnLogin.isEnabled = false
        btnLogin.text = "LOGGING IN..."

        val request =
            LoginRequest(
                username = username,
                password = password
            )

        RetrofitClient.api
            .login(request)
            .enqueue(
                object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {

                        btnLogin.isEnabled = true
                        btnLogin.text = "LOGIN"

                        if (!response.isSuccessful) {

                            Toast.makeText(
                                this@LoginActivity,
                                "Invalid username or password",
                                Toast.LENGTH_SHORT
                            ).show()

                            return
                        }

                        val result =
                            response.body()

                        if (result?.success != true) {

                            Toast.makeText(
                                this@LoginActivity,
                                result?.message
                                    ?: "Login failed",
                                Toast.LENGTH_SHORT
                            ).show()

                            return
                        }

                        handleLogin(result)
                    }

                    override fun onFailure(
                        call: Call<LoginResponse>,
                        t: Throwable
                    ) {

                        btnLogin.isEnabled = true
                        btnLogin.text = "LOGIN"

                        Toast.makeText(
                            this@LoginActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        t.printStackTrace()
                    }
                }
            )
    }

    private fun handleLogin(
        result: LoginResponse
    ) {

        when (
            result.role?.uppercase()
        ) {

            "STUDENT" -> {

                val studentId =
                    result.student_id

                if (studentId == null) {

                    Toast.makeText(
                        this,
                        "Student ID missing",
                        Toast.LENGTH_LONG
                    ).show()

                    return
                }

                saveStudentLogin(
                    studentId
                )

                openStudentDashboard(
                    studentId
                )
            }

            "FACULTY" -> {

                val facultyId =
                    result.faculty_id

                if (facultyId == null) {

                    Toast.makeText(
                        this,
                        "Faculty ID missing",
                        Toast.LENGTH_LONG
                    ).show()

                    return
                }

                saveFacultyLogin(
                    facultyId
                )

                openTeacherDashboard(
                    facultyId
                )
            }

            "ADMIN" -> {

                saveAdminLogin()

                openAdminDashboard()
            }

            else -> {

                Toast.makeText(
                    this,
                    "Unknown user role",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveFacultyLogin(
        facultyId: Int
    ) {

        getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                "isLoggedIn",
                true
            )
            .putString(
                "role",
                "FACULTY"
            )
            .putInt(
                "faculty_id",
                facultyId
            )
            .apply()
    }

    private fun saveStudentLogin(
        studentId: Int
    ) {

        getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                "isLoggedIn",
                true
            )
            .putString(
                "role",
                "STUDENT"
            )
            .putInt(
                "student_id",
                studentId
            )
            .apply()
    }

    private fun openTeacherDashboard(
        facultyId: Int
    ) {

        val intent =
            Intent(
                this,
                TeacherDashboardActivity::class.java
            )

        intent.putExtra(
            "faculty_id",
            facultyId
        )

        startActivity(intent)

        finish()
    }

    private fun openStudentDashboard(
        studentId: Int
    ) {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.putExtra(
            "student_id",
            studentId
        )

        startActivity(intent)

        finish()
    }

    private fun clearLogin() {

        getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()
    }
    private fun saveAdminLogin() {

        getSharedPreferences(
            "EduGuardPrefs",
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                "isLoggedIn",
                true
            )
            .putString(
                "role",
                "ADMIN"
            )
            .apply()
    }
    private fun openAdminDashboard() {

        val intent =
            Intent(
                this,
                AdminDashboardActivity::class.java
            )

        startActivity(intent)

        finish()
    }
}