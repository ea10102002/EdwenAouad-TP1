package com.usj.news_application.views

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.usj.news_application.R
import com.usj.news_application.viewmodels.SignUpViewModel
import java.util.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmailSignUp)
        val etPassword = findViewById<EditText>(R.id.etPasswordSignUp)
        val etBirthDate = findViewById<EditText>(R.id.etBirthDate)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnGoToLogin = findViewById<Button>(R.id.btnGoToLogin)

        btnGoToLogin.setOnClickListener {
            finish() // Since LoginActivity started SignUpActivity, just finishing this activity goes back
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

        // Add TextWatcher for the birthdate field
        etBirthDate.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                isEditing = true
                val input = s.toString()
                val formatted = formatBirthDate(input)
                etBirthDate.setText(formatted)
                etBirthDate.setSelection(formatted.length)
                isEditing = false
            }

            private fun formatBirthDate(input: String): String {
                val sanitized = input.replace("/", "")
                val sb = StringBuilder()

                for (i in sanitized.indices) {
                    when (i) {
                        0, 1, 2, 3 -> sb.append(sanitized[i]) // Year
                        4 -> sb.append("/").append(sanitized[i]) // Add slash before month
                        5 -> sb.append(sanitized[i])
                        6 -> sb.append("/").append(sanitized[i]) // Add slash before day
                        7 -> sb.append(sanitized[i])
                    }
                }

                return sb.toString()
            }
        })

        btnSignUp.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val birthdate = etBirthDate.text.toString().trim()

            if (!validateInputs(firstName, lastName, email, password, birthdate)) {
                return@setOnClickListener
            }

            if (viewModel.registerUser(this, firstName, lastName, email, password, birthdate)) {
                // Save the new user's email in SharedPreferences
                val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("loggedInEmail", email)
                editor.apply()

                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                // Redirect to News Page (MainActivity)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to create account. Email might already exist.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        birthdate: String
    ): Boolean {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || birthdate.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Pattern.matches("^[a-zA-Z]+$", firstName)) {
            Toast.makeText(this, "First Name should contain only letters.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Pattern.matches("^[a-zA-Z]+$", lastName)) {
            Toast.makeText(this, "Last Name should contain only letters.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!validateEmail(email)) {
            Toast.makeText(this, "Email must end with @gmail.com or @hotmail.com.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password should be at least 8 characters long.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!validateBirthDate(birthdate)) {
            Toast.makeText(this, "Invalid birthdate or too young (must be 15+).", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@(gmail|hotmail)\\.com$"
        return Pattern.matches(emailRegex, email)
    }

    private fun validateBirthDate(birthdate: String): Boolean {
        val regex = "\\d{4}/\\d{2}/\\d{2}" // YYYY/MM/DD format
        if (!Pattern.matches(regex, birthdate)) {
            return false
        }

        val parts = birthdate.split("/")
        val year = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val day = parts[2].toIntOrNull() ?: return false

        if (year < 1925 || month !in 1..12 || day !in 1..31) {
            return false
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (currentYear - year < 15) {
            return false
        }

        return true
    }
}
