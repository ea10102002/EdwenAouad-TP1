package com.usj.news_application.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.usj.news_application.DatabaseHelper
import com.usj.news_application.models.User
import com.usj.news_application.repositories.UserRepository

class AccountInfoViewModel : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    fun loadUsers(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val repository = UserRepository(dbHelper)
        _users.value = repository.getAllUsers()
    }
}
