package com.usj.news_application.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.usj.news_application.R
import com.usj.news_application.viewmodels.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmailLogin)
        val etPassword = findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoToSignUp = findViewById<Button>(R.id.btnGoToSignUp)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (viewModel.validateUser(this, email, password)) { // Pass the context for DB operations
                // Save the logged-in user's email in SharedPreferences
                val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("loggedInEmail", email) // Store the logged-in user's email
                editor.apply()

                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity (which hosts NewsPage)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
