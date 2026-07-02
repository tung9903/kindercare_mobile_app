package com.example.myapplication.View

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.UserProfileResponse
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.UserDirectoryAdapter

class DirectoryActivity : AppCompatActivity() {

    private lateinit var rvDirectory: RecyclerView
    private lateinit var adapter: UserDirectoryAdapter
    private val userList = mutableListOf<UserProfileResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_directory)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        rvDirectory = findViewById(R.id.rvDirectory)
        adapter = UserDirectoryAdapter(userList)
        rvDirectory.layoutManager = LinearLayoutManager(this)
        rvDirectory.adapter = adapter

        loadMockData()
    }

    private fun loadMockData() {
        userList.add(UserProfileResponse(1, "Nguyễn Văn A", "Hiệu trưởng", "0912345678", null))
        userList.add(UserProfileResponse(2, "Trần Thị B", "Giáo viên", "0987654321", null))
        adapter.notifyDataSetChanged()
    }
}
