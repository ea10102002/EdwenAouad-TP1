package com.usj.news_application

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.usj.news_application.models.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_BIRTHDATE = "birthdate"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FIRST_NAME TEXT,
                $COLUMN_LAST_NAME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_BIRTHDATE TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun insertUser(firstName: String, lastName: String, email: String, password: String, birthdate: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_BIRTHDATE, birthdate)
        }

        return try {
            db.insertOrThrow(TABLE_USERS, null, values) != -1L
        } catch (e: Exception) {
            false
        }
    }




    fun updateUserWithEmail(userId: Int, firstName: String, lastName: String, password: String, email: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }

        return db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(userId.toString())) > 0
    }

    fun deleteUser(userId: Int): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_USERS, "$COLUMN_ID = ?", arrayOf(userId.toString())) > 0
    }

    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?
        """
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }

    fun getUserById(userId: Int): User? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDATE))

            cursor.close()
            return User(id, firstName, lastName, email, birthdate, password)
        }

        cursor.close()
        return null
    }


    fun getAllUsers(): List<User> {
        val userList = mutableListOf<User>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
                val lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
                val birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDATE))

                val user = User(id, firstName, lastName, email, birthdate, password)
                userList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return userList
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDATE))

            cursor.close()
            return User(id, firstName, lastName, email, birthdate, password)
        }

        cursor.close()
        return null
    }
}
