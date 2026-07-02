package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.ActivityPost
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.ActivityPostAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ManHinhHoatDong : AppCompatActivity() {

    private lateinit var rvPosts: RecyclerView
    private lateinit var adapter: ActivityPostAdapter
    private var displayList = mutableListOf<ActivityPost>()
    private var currentFilterType = "All"
    private var selectedMediaUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            adapter.updateMedia(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_hoat_dong)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupBottomNavigation()
        setupFilters()
        setupSearch()

        fetchPosts()
    }

    private fun setupRecyclerView() {
        rvPosts = findViewById(R.id.rvActivityPosts)
        adapter = ActivityPostAdapter(
            displayList,
            selectedMediaUri,
            onPostClick = { content, uri ->
                if (content.isNotEmpty()) {
                    submitPost(content, uri)
                } else {
                    Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show()
                }
            },
            onPickMedia = {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemoveMedia = {
                selectedMediaUri = null
                adapter.updateMedia(null)
            },
            onImageClick = { },
            onCommentClick = { }
        )
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter
    }

    private fun fetchPosts() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/posts")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val dataArray = json.optJSONArray("data")
                        val temp = mutableListOf<ActivityPost>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                temp.add(ActivityPost(
                                    id = obj.optString("postId"),
                                    teacherName = obj.optString("teacherName", "Giáo viên"),
                                    role = obj.optString("role", "Chuyên môn"),
                                    dateText = obj.optString("postedAt", "Vừa xong"),
                                    contentTitle = obj.optString("content"),
                                    imageUri = if (obj.isNull("mediaUrl")) null else obj.optString("mediaUrl"),
                                    avatarResId = R.drawable.avatar
                                ))
                            }
                        }
                        runOnUiThread {
                            displayList.clear()
                            displayList.addAll(temp.reversed())
                            adapter.updatePosts(displayList)
                        }
                    }
                    return@use
                }
            } catch (e: Exception) { Log.e("FETCH_POSTS", e.message ?: "") }
        }.start()
    }

    private fun submitPost(content: String, uri: Uri?) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        Toast.makeText(this, "Đang tải bài viết lên...", Toast.LENGTH_SHORT).show()
        
        val json = JSONObject().apply {
            put("content", content)
            put("mediaUrl", uri?.toString() ?: "")
            put("classId", 1) 
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/posts")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Đã đăng bài viết thành công!", Toast.LENGTH_SHORT).show()
                            selectedMediaUri = null
                            adapter.updateMedia(null)
                            fetchPosts()
                        }
                    }
                    return@use
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun setupFilters() {
        val chipAll = findViewById<TextView>(R.id.chipAll)
        val chips = listOf(chipAll, findViewById(R.id.chipMeals), findViewById(R.id.chipActivities), findViewById(R.id.chipStatus))
        
        chipAll.setOnClickListener { updateFilter("All", chips, it) }
        findViewById<View>(R.id.chipMeals).setOnClickListener { updateFilter("Meal", chips, it) }
        findViewById<View>(R.id.chipActivities).setOnClickListener { updateFilter("Activity", chips, it) }
        findViewById<View>(R.id.chipStatus).setOnClickListener { updateFilter("Status", chips, it) }
    }

    private fun updateFilter(type: String, allChips: List<TextView>, activeChip: View) {
        currentFilterType = type
        allChips.forEach {
            it.setBackgroundResource(R.drawable.bg_chip_unselected)
            it.setTextColor(Color.parseColor("#555555"))
        }
        activeChip.setBackgroundResource(R.drawable.bg_chip_selected)
        (activeChip as TextView).setTextColor(Color.WHITE)
        fetchPosts()
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearchPost).addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "ACTIVITY")
    }
}
