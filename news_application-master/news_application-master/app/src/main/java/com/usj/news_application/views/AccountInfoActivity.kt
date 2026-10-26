package com.usj.news_application.views

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.usj.news_application.DatabaseHelper
import com.usj.news_application.R

class AccountInfoActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)

        val toolbar = findViewById<Toolbar>(R.id.toolbarAccount)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        databaseHelper = DatabaseHelper(this)

        val btnEditInfo = findViewById<Button>(R.id.btnEditInfo)
        val btnAdminDashboard = findViewById<Button>(R.id.btnAdminDashboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvFirstName = findViewById<TextView>(R.id.tvFirstName)
        val tvLastName = findViewById<TextView>(R.id.tvLastName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvBirthDate = findViewById<TextView>(R.id.tvBirthDate)

        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("loggedInEmail", null)

        // Populate user info
        if (loggedInEmail != null) {
            val user = databaseHelper.getUserByEmail(loggedInEmail)
            if (user != null) {
                tvFirstName.text = "First Name: ${user.firstName}"
                tvLastName.text = "Last Name: ${user.lastName}"
                tvEmail.text = "Email: ${user.email}"
                tvBirthDate.text = "Birthdate: ${user.birthdate}"
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnEditInfo.setOnClickListener {
            val intent = Intent(this, EditUserInfoActivity::class.java)
            startActivity(intent)
        }

        btnAdminDashboard.setOnClickListener {
            showAdminAuthDialog()
        }
        
        btnLogout.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showAdminAuthDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_admin_auth, null)

        val etUsername = view.findViewById<TextView>(R.id.etAdminUsername)
        val etPassword = view.findViewById<TextView>(R.id.etAdminPassword)

        builder.setView(view)
        builder.setTitle("Admin Authentication")
        builder.setPositiveButton("Login") { dialog, _ ->
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username == "admin" && password == "admin") {
                dialog.dismiss()
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
