package com.usj.news_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.usj.news_application.R
import com.usj.news_application.models.User

class UserAdapter(private var users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvFirstName: TextView = itemView.findViewById(R.id.tvFirstName)
        val tvLastName: TextView = itemView.findViewById(R.id.tvLastName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvPassword: TextView = itemView.findViewById(R.id.tvPassword)
        val tvBirthDate: TextView = itemView.findViewById(R.id.tvBirthDate) // Birthday TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserId.text = "ID: ${user.id}"
        holder.tvFirstName.text = "First Name: ${user.firstName}"
        holder.tvLastName.text = "Last Name: ${user.lastName}"
        holder.tvEmail.text = "Email: ${user.email}"
        holder.tvPassword.text = "Password: ${user.password}"
        holder.tvBirthDate.text = "Birthdate: ${user.birthdate}" // Display Birthday
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
