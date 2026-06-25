package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class ChatDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        val rootView = findViewById<View>(R.id.layoutHeader).parent as View
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val conversationId = intent.getIntExtra("CONVERSATION_ID", -1)
        val chatItem = DataManager.parentChats.find { it.ConversationID == conversationId }

        if (chatItem == null) {
            Toast.makeText(this, "Không tìm thấy nội dung tin nhắn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI(chatItem)
    }

    private fun setupUI(item: com.example.myapplication.Model.ChatItem) {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            DataManager.parentChats.remove(item)
            Toast.makeText(this, "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<TextView>(R.id.tvSenderName).text = item.Name
        findViewById<TextView>(R.id.tvTime).text = DateHelper.formatLongToTime(item.Time) + " • " + DateHelper.formatLongToDate(item.Time)
        findViewById<TextView>(R.id.tvMessageBody).text = item.LastMessage
        findViewById<ShapeableImageView>(R.id.imgSenderAvatar).setImageResource(item.avatarResId)

        findViewById<MaterialButton>(R.id.btnReply).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }
}
