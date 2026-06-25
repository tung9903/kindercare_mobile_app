package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ActivityPost
import com.example.myapplication.databinding.ItemActivityPostBinding

class ActivityPostAdapter(
    private val postList: List<ActivityPost>,
    private val onImageClick: (ActivityPost) -> Unit,   // Thêm callback click vào ảnh bài viết
    private val onCommentClick: (ActivityPost) -> Unit  // Callback click vào nút comment
) : RecyclerView.Adapter<ActivityPostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemActivityPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemActivityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        with(holder.binding) {
            tvTeacherName.text = post.teacherName
            tvRole.text = post.role
            tvDate.text = post.dateText
            tvContentTitle.text = post.contentTitle

            post.imageResId?.let { imgPostContent.setImageResource(it) }
            post.avatarResId?.let { imgAvatar.setImageResource(it) }

            // 1. Xử lý sự kiện khi click vào hình ảnh bài viết
            imgPostContent.setOnClickListener {
                onImageClick(post)
            }

            // 2. Xử lý sự kiện khi click vào nút bình luận
            btnComment.setOnClickListener {
                onCommentClick(post)
            }
        }
    }

    override fun getItemCount(): Int = postList.size
}