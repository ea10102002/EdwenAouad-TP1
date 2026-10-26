package com.usj.news_application.views

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.usj.news_application.DatabaseHelper
import com.usj.news_application.R
import com.usj.news_application.adapters.UserAdapter

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        databaseHelper = DatabaseHelper(this)

        // Views
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        val btnUpdateUser = findViewById<Button>(R.id.btnUpdateUser)
        val btnDeleteUser = findViewById<Button>(R.id.btnDeleteUser)

        val etUserId = findViewById<EditText>(R.id.etUserId)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etBirthDate = findViewById<EditText>(R.id.etBirthDate)

        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(databaseHelper.getAllUsers())
        recyclerViewUsers.adapter = adapter

        btnAddUser.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val birthDate = etBirthDate.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDate.isEmpty()) {
                Toast.makeText(this, "All fields are required to add a user.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.insertUser(firstName, lastName, email, password, birthDate)) {
                Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show()
                refreshUserList()
                clearInputs()
            } else {
                Toast.makeText(this, "Failed to add user. Email might already exist.", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdateUser.setOnClickListener {
            val userId = etUserId.text.toString().toIntOrNull()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (userId == null) {
                Toast.makeText(this, "User ID is required to update a user.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (firstName.isEmpty() && lastName.isEmpty() && email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "At least one field must be updated.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = databaseHelper.getUserById(userId)
            if (user == null) {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedFirstName = if (firstName.isNotEmpty()) firstName else user.firstName
            val updatedLastName = if (lastName.isNotEmpty()) lastName else user.lastName
            val updatedEmail = if (email.isNotEmpty()) email else user.email
            val updatedPassword = if (password.isNotEmpty()) password else user.password

            if (databaseHelper.updateUserWithEmail(userId, updatedFirstName, updatedLastName, updatedPassword, updatedEmail)) {
                Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show()
                refreshUserList()
                clearInputs()
            } else {
                Toast.makeText(this, "Failed to update user.", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteUser.setOnClickListener {
            val userId = etUserId.text.toString().toIntOrNull()

            if (userId == null) {
                Toast.makeText(this, "User ID is required to delete a user.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.deleteUser(userId)) {
                Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show()
                refreshUserList()
                clearInputs()
            } else {
                Toast.makeText(this, "Failed to delete user. User ID might not exist.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshUserList() {
        val users = databaseHelper.getAllUsers()
        adapter.updateUsers(users)
    }

    private fun clearInputs() {
        findViewById<EditText>(R.id.etUserId).text.clear()
        findViewById<EditText>(R.id.etFirstName).text.clear()
        findViewById<EditText>(R.id.etLastName).text.clear()
        findViewById<EditText>(R.id.etEmail).text.clear()
        findViewById<EditText>(R.id.etPassword).text.clear()
        findViewById<EditText>(R.id.etBirthDate).text.clear()
    }
}
