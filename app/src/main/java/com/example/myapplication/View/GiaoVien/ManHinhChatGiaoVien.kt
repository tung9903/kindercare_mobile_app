package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.Adapter.ChatGiaoVienAdapter
import com.example.myapplication.Model.ChatItem
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils

class ManHinhChatGiaoVien : AppCompatActivity() {
    private lateinit var adapter: ChatGiaoVienAdapter
    private var chatDisplayList = mutableListOf<ChatItem>()
    private var originalChatList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chat_giao_vien)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupBottomNavigation()
        setupSearch()
        setupNotificationNavigation()
        setupAvatarNavigation()

        loadChatsFromDataManager()
        handleIncomingChatRequest()
    }

    private fun loadChatsFromDataManager() {
        val now = System.currentTimeMillis() / 1000
        originalChatList.clear()
        
        DataManager.studentList.forEachIndexed { index, student ->
            originalChatList.add(
                ChatItem(
                    ConversationID = student.StudentID,
                    Name = "Phụ huynh: ${student.parentName}",
                    LastMessage = "Trao đổi về tình hình bé ${student.FullName}",
                    Time = now - (index * 3600),
                    avatarResId = student.avatarResId,
                    isOnline = index % 2 == 0,
                    hasNewMessage = index == 0
                )
            )
        }
        
        chatDisplayList.clear()
        chatDisplayList.addAll(originalChatList)
        adapter.notifyDataSetChanged()
    }

    private fun handleIncomingChatRequest() {
        val parentName = intent.getStringExtra("PARENT_NAME")
        val studentName = intent.getStringExtra("STUDENT_NAME")
        if (parentName != null) {
            Toast.makeText(this, "💬 Đang mở cuộc trò chuyện với PH bé $studentName", Toast.LENGTH_LONG).show()
            
            val query = parentName.lowercase()
            val filtered = originalChatList.filter { it.Name.lowercase().contains(query) }
            chatDisplayList.clear()
            chatDisplayList.addAll(filtered)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupAvatarNavigation() {
        findViewById<View>(R.id.imgAvatar).setOnClickListener {
            startActivity(Intent(this, ManHinhThongTinTaiKhoanCaNhan::class.java))
        }
    }

    private fun setupNotificationNavigation() {
        findViewById<View>(R.id.layoutNotification).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangThongBao::class.java))
        }
    }

    private fun setupRecyclerView() {
        val revChat = findViewById<RecyclerView>(R.id.revChatGiaoVien)
        adapter = ChatGiaoVienAdapter(chatDisplayList)
        revChat.layoutManager = LinearLayoutManager(this)
        revChat.adapter = adapter
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText>(R.id.etSearchChat)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                chatDisplayList.clear()
                if (query.isEmpty()) {
                    chatDisplayList.addAll(originalChatList)
                } else {
                    val filtered = originalChatList.filter { 
                        it.Name.lowercase().contains(query) || it.LastMessage.lowercase().contains(query)
                    }
                    chatDisplayList.addAll(filtered)
                }
                adapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "CONTACT")
    }
}
