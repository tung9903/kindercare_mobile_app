package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.UserProfileResponse
import com.example.myapplication.R

class UserDirectoryAdapter(private val userList: List<UserProfileResponse>) :
    RecyclerView.Adapter<UserDirectoryAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgUserAvatar)
        val tvFullName: TextView = view.findViewById(R.id.tvUserFullName)
        val tvRole: TextView = view.findViewById(R.id.tvUserRole)
        val tvContact: TextView = view.findViewById(R.id.tvUserContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_directory, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvFullName.text = user.fullName ?: user.username
        holder.tvRole.text = user.roleName
        holder.tvContact.text = user.phoneNumber ?: user.email ?: "Không có thông tin"
        
        // Bạn có thể dùng thư viện Glide hoặc Coil để load ảnh từ user.avatarUrl
        // Tạm thời để mặc định
        holder.imgAvatar.setImageResource(R.drawable.avatar)
    }

    override fun getItemCount(): Int = userList.size
}