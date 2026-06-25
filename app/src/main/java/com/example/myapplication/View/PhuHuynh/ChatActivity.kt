package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.ChatItem
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityChatBinding
import kotlin.random.Random

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rootView = findViewById<View>(R.id.main)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        setupRecipientSpinner()
        setupActions()
    }

    private fun setupRecipientSpinner() {
        val recipients = listOf("Cô Lan Anh (Chủ nhiệm)", "Thầy Minh (Thể dục)", "Cô Hồng (Âm nhạc)", "Ban Giám Hiệu")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipients)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTo.adapter = adapter

        binding.spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateLimitWarning()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateLimitWarning() {
        // Giả lập số lượt đã dùng (ngẫu nhiên cho mục đích demo)
        val used = Random.nextInt(0, 3)
        binding.tvLimitWarning.text = "⚠️ Bạn đã dùng $used/2 lượt gửi tin cho giáo viên này hôm nay."
        
        if (used >= 2) {
            binding.tvLimitWarning.setTextColor(android.graphics.Color.RED)
            binding.btnSend.isEnabled = false
            binding.btnSend.alpha = 0.5f
        } else {
            binding.tvLimitWarning.setTextColor(android.graphics.Color.parseColor("#D97706"))
            binding.btnSend.isEnabled = true
            binding.btnSend.alpha = 1.0f
        }
    }

    private fun setupActions() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            val subject = binding.etSubject.text.toString().trim()
            val body = binding.etBody.text.toString().trim()
            
            if (subject.isEmpty()) {
                binding.etSubject.error = "Vui lòng nhập chủ đề"
                return@setOnClickListener
            }

            if (body.isEmpty()) {
                binding.etBody.error = "Vui lòng nhập nội dung"
                return@setOnClickListener
            }

            val recipient = binding.spinnerTo.selectedItem.toString()

            // Tạo item mới
            val newChat = ChatItem(
                ConversationID = Random.nextInt(1000, 9999),
                Name = recipient,
                LastMessage = "$subject: $body",
                Time = System.currentTimeMillis() / 1000,
                avatarResId = R.drawable.avatar,
                isOnline = true
            )
            
            // Lưu vào danh sách chung
            DataManager.parentChats.add(newChat)

            Toast.makeText(this, "Đã gửi tin nhắn thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
