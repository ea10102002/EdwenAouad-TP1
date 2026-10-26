package com.usj.news_application.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.usj.news_application.DatabaseHelper
import com.usj.news_application.repositories.UserRepository

class SignUpViewModel : ViewModel() {

    fun registerUser(
        context: Context,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        birthdate: String
    ): Boolean {
        val dbHelper = DatabaseHelper(context)
        val repository = UserRepository(dbHelper)
        return repository.insertUser(firstName, lastName, email, password, birthdate)
    }
}
