package com.example.myapplication.View.Adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ActivityPost
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemActivityPostBinding

class ActivityPostAdapter(
    private var postList: List<ActivityPost>,
    private var selectedUri: Uri? = null,
    private val onPostClick: (String, Uri?) -> Unit,
    private val onPickMedia: () -> Unit,
    private val onRemoveMedia: () -> Unit,
    private val onImageClick: (ActivityPost) -> Unit,
    private val onCommentClick: (ActivityPost) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_POST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_POST
    }

    inner class PostViewHolder(val binding: ItemActivityPostBinding) : RecyclerView.ViewHolder(binding.root)
    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val etContent: EditText = view.findViewById(R.id.etPostContent)
        val layoutPreview: View = view.findViewById(R.id.layoutImagePreview)
        val imgPreview: ImageView = view.findViewById(R.id.imgPreview)
        val btnRemove: View = view.findViewById(R.id.btnRemoveImage)
        val btnPick: View = view.findViewById(R.id.btnPickMedia)
        val btnPost: View = view.findViewById(R.id.btnPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_header_post_creation, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemActivityPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PostViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.layoutPreview.visibility = if (selectedUri != null) View.VISIBLE else View.GONE
            selectedUri?.let { holder.imgPreview.setImageURI(it) }

            holder.btnPick.setOnClickListener { onPickMedia() }
            holder.btnRemove.setOnClickListener { onRemoveMedia() }
            holder.btnPost.setOnClickListener {
                onPostClick(holder.etContent.text.toString(), selectedUri)
            }
        } else if (holder is PostViewHolder) {
            val post = postList[position - 1]
            with(holder.binding) {
                tvTeacherName.text = post.teacherName
                tvRole.text = post.role
                tvDate.text = post.dateText
                tvContentTitle.text = post.contentTitle

                if (post.imageResId != null) {
                    imgPostContent.visibility = View.VISIBLE
                    imgPostContent.setImageResource(post.imageResId)
                } else if (post.imageUri != null) {
                    imgPostContent.visibility = View.VISIBLE
                    imgPostContent.setImageURI(Uri.parse(post.imageUri))
                } else {
                    imgPostContent.visibility = View.GONE
                }

                post.avatarResId?.let { imgAvatar.setImageResource(it) }
                imgPostContent.setOnClickListener { onImageClick(post) }
                btnComment.setOnClickListener { onCommentClick(post) }
            }
        }
    }

    override fun getItemCount(): Int = postList.size + 1

    fun updateMedia(uri: Uri?) {
        this.selectedUri = uri
        notifyItemChanged(0)
    }

    fun updatePosts(newList: List<ActivityPost>) {
        this.postList = newList
        notifyDataSetChanged()
    }
}
