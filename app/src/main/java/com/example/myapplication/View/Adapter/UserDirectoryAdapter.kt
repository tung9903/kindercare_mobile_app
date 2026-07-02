package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.UserProfileResponse
import com.example.myapplication.R

class UserDirectoryAdapter(private val userList: List<UserProfileResponse>) :
    RecyclerView.Adapter<UserDirectoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUserFullName)
        val tvRole: TextView = view.findViewById(R.id.tvUserRole)
        val tvContact: TextView = view.findViewById(R.id.tvUserContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_directory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.tvName.text = user.fullName
        holder.tvRole.text = user.role
        holder.tvContact.text = user.phoneNumber ?: "N/A"
    }

    override fun getItemCount() = userList.size
}
