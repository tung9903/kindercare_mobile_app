package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ChatItem
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.ChatAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ListChatActivity : AppCompatActivity() {

    private lateinit var rvChatList: RecyclerView
    private lateinit var adapter: ChatAdapter
    private var displayList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_chat)

        val rootView = findViewById<View>(R.id.main)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        setupRecyclerView()
        setupSearch()
        setupComposeAction()
        setupBottomNavigation()
        setupHeaderActions()
        
        loadChats()
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }

    private fun setupRecyclerView() {
        rvChatList = findViewById(R.id.rvChatList)
        adapter = ChatAdapter(displayList) { item ->
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("CONVERSATION_ID", item.ConversationID)
            startActivity(intent)
        }
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = adapter
    }

    private fun loadChats() {
        displayList.clear()
        // Đảm bảo lấy dữ liệu từ DataManager
        val chats = DataManager.parentChats.sortedByDescending { it.Time }
        displayList.addAll(chats)
        adapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        val edtSearch = findViewById<EditText>(R.id.edtSearchChat)
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = DataManager.parentChats.filter {
                    it.Name.lowercase().contains(query) || it.LastMessage.lowercase().contains(query)
                }.sortedByDescending { it.Time }
                
                displayList.clear()
                displayList.addAll(filtered)
                adapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupComposeAction() {
        findViewById<ExtendedFloatingActionButton>(R.id.fabCompose).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupHeaderActions() {
        findViewById<View>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<View>(R.id.imgAvatarHeader).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "CONTACT")
    }
}
