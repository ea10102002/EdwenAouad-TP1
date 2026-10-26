package com.usj.news_application.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.usj.news_application.DatabaseHelper
import com.usj.news_application.R
import java.util.regex.Pattern

class EditUserInfoActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_info)

        databaseHelper = DatabaseHelper(this)

        // Initialize views
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Retrieve logged-in user email from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("loggedInEmail", null) ?: run {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val user = databaseHelper.getUserByEmail(loggedInEmail)
        if (user != null) {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etEmail.setText(user.email)
        }

        btnSave.setOnClickListener {
            val newFirstName = etFirstName.text.toString()
            val newLastName = etLastName.text.toString()
            val newEmail = etEmail.text.toString()
            val newPassword = etNewPassword.text.toString()

            // Initialize flags for validation
            var hasValidationError = false

            // Validate first name only if it has been changed
            if (newFirstName != user?.firstName) {
                if (!Pattern.matches("^[a-zA-Z]+$", newFirstName)) {
                    Toast.makeText(this, "First name must contain only letters.", Toast.LENGTH_SHORT).show()
                    hasValidationError = true
                }
            }

            // Validate last name only if it has been changed
            if (newLastName != user?.lastName) {
                if (!Pattern.matches("^[a-zA-Z]+$", newLastName)) {
                    Toast.makeText(this, "Last name must contain only letters.", Toast.LENGTH_SHORT).show()
                    hasValidationError = true
                }
            }

            // Validate email only if it has been changed
            if (newEmail != user?.email) {
                val emailRegex = "^[a-zA-Z0-9._%+-]+@(gmail|hotmail)\\.com$"
                if (!Pattern.matches(emailRegex, newEmail)) {
                    Toast.makeText(this, "Email must end with @gmail.com or @hotmail.com.", Toast.LENGTH_SHORT).show()
                    hasValidationError = true
                }
            }

            // Validate password only if it has been changed
            if (newPassword.isNotEmpty() && newPassword != user?.password) {
                if (newPassword.length < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters long.", Toast.LENGTH_SHORT).show()
                    hasValidationError = true
                }
            }

            if (hasValidationError) return@setOnClickListener

            // Prepare updated values
            val updatedFirstName = if (newFirstName != user?.firstName) newFirstName else user?.firstName
            val updatedLastName = if (newLastName != user?.lastName) newLastName else user?.lastName
            val updatedEmail = if (newEmail != user?.email) newEmail else user?.email
            val updatedPassword = if (newPassword.isNotEmpty()) newPassword else user?.password

            // Update user in the database
            val isUpdated = databaseHelper.updateUserWithEmail(
                user!!.id,
                updatedFirstName!!,
                updatedLastName!!,
                updatedPassword!!,
                updatedEmail!!
            )

            if (isUpdated) {
                Toast.makeText(this, "User information updated successfully.", Toast.LENGTH_SHORT).show()

                // Pass updated user info back to AccountInfoActivity
                val resultIntent = Intent()
                resultIntent.putExtra("updatedEmail", updatedEmail)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Failed to update user information.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
