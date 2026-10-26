package com.usj.news_application.models

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val birthdate: String,
    val password: String
)


