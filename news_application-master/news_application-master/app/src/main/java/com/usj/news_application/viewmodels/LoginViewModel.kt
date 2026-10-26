package com.usj.news_application.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.usj.news_application.DatabaseHelper
import com.usj.news_application.repositories.UserRepository

class LoginViewModel : ViewModel() {

    fun validateUser(context: Context, email: String, password: String): Boolean {
        val dbHelper = DatabaseHelper(context)
        val repository = UserRepository(dbHelper)
        return repository.validateUser(email, password)
    }
}
