package com.example.edugaurd

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.edugaurd.api.AddFacultyRequest
import com.example.edugaurd.api.AddFacultyResponse
import com.example.edugaurd.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddFacultyActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    private lateinit var spDivision: Spinner
    private lateinit var btnAddFaculty: Button

    private val divisionIds =
        listOf(1, 2, 3, 4, 5, 6, 7, 8)

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_add_faculty
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
    }


    private fun initializeViews() {

        etFirstName =
            findViewById(R.id.etFirstName)

        etLastName =
            findViewById(R.id.etLastName)

        etEmail =
            findViewById(R.id.etEmail)

        etUsername =
            findViewById(R.id.etUsername)

        etPassword =
            findViewById(R.id.etPassword)

        spDivision =
            findViewById(R.id.spDivision)

        btnAddFaculty =
            findViewById(R.id.btnAddFaculty)
    }


    private fun setupDivisionSpinner() {

        val divisions =
            divisionIds.map {
                "DIV-$it"
            }

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
    }


    private fun setupButtons() {

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {

            finish()
        }


        btnAddFaculty.setOnClickListener {

            addFaculty()
        }
    }


    private fun addFaculty() {

        val firstName =
            etFirstName.text
                .toString()
                .trim()

        val lastName =
            etLastName.text
                .toString()
                .trim()

        val email =
            etEmail.text
                .toString()
                .trim()

        val username =
            etUsername.text
                .toString()
                .trim()

        val password =
            etPassword.text
                .toString()

        val divisionId =
            divisionIds[
                spDivision.selectedItemPosition
            ]


        if (firstName.isEmpty()) {

            etFirstName.error =
                "Enter first name"

            etFirstName.requestFocus()

            return
        }

        if (lastName.isEmpty()) {

            etLastName.error =
                "Enter last name"

            etLastName.requestFocus()

            return
        }

        if (email.isEmpty()) {

            etEmail.error =
                "Enter email"

            etEmail.requestFocus()

            return
        }

        if (username.isEmpty()) {

            etUsername.error =
                "Enter username"

            etUsername.requestFocus()

            return
        }

        if (password.isEmpty()) {

            etPassword.error =
                "Enter password"

            etPassword.requestFocus()

            return
        }


        btnAddFaculty.isEnabled =
            false

        btnAddFaculty.text =
            "ADDING..."


        val request =
            AddFacultyRequest(
                first_name = firstName,
                last_name = lastName,
                email = email,
                division_id = divisionId,
                username = username,
                password = password
            )


        RetrofitClient.api
            .addFaculty(request)
            .enqueue(
                object :
                    Callback<AddFacultyResponse> {

                    override fun onResponse(
                        call: Call<AddFacultyResponse>,
                        response: Response<AddFacultyResponse>
                    ) {

                        btnAddFaculty.isEnabled =
                            true

                        btnAddFaculty.text =
                            "ADD FACULTY"


                        if (response.isSuccessful) {

                            val result =
                                response.body()

                            if (
                                result?.success == true
                            ) {

                                Toast.makeText(
                                    this@AddFacultyActivity,
                                    "Faculty added successfully",
                                    Toast.LENGTH_LONG
                                ).show()

                                finish()

                            } else {

                                Toast.makeText(
                                    this@AddFacultyActivity,
                                    result?.message
                                        ?: "Unable to add faculty",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {

                            Toast.makeText(
                                this@AddFacultyActivity,
                                "Server error: ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


                    override fun onFailure(
                        call: Call<AddFacultyResponse>,
                        t: Throwable
                    ) {

                        btnAddFaculty.isEnabled =
                            true

                        btnAddFaculty.text =
                            "ADD FACULTY"

                        Toast.makeText(
                            this@AddFacultyActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }
}