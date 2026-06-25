package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
import java.util.UUID

class ManHinhHoatDong : AppCompatActivity() {

    private lateinit var rvPosts: RecyclerView
    private lateinit var adapter: ActivityPostAdapter
    private var displayList = mutableListOf<ActivityPost>()
    private var currentFilterType = "All"
    private var selectedMediaUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            Toast.makeText(this, "Đã chọn 1 tệp phương tiện", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Chưa chọn tệp nào", Toast.LENGTH_SHORT).show()
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
        setupPostAction()
        setupNotificationNavigation()
        setupFilters()
        setupSearch()
        setupAvatarNavigation()
        setupMediaPickAction()

        loadPosts()
    }

    private fun setupRecyclerView() {
        rvPosts = findViewById(R.id.rvActivityPosts)
        adapter = ActivityPostAdapter(
            displayList,
            onImageClick = { },
            onCommentClick = { }
        )
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        displayList.clear()
        val filtered = if (currentFilterType == "All") {
            DataManager.activityPosts
        } else {
            DataManager.activityPosts.filter { it.postType == currentFilterType }
        }
        displayList.addAll(filtered.reversed())
        adapter.notifyDataSetChanged()
    }

    private fun setupFilters() {
        val chipAll = findViewById<TextView>(R.id.chipAll)
        val chipMeals = findViewById<TextView>(R.id.chipMeals)
        val chipActivities = findViewById<TextView>(R.id.chipActivities)
        val chipStatus = findViewById<TextView>(R.id.chipStatus)

        val chips = listOf(chipAll, chipMeals, chipActivities, chipStatus)

        chipAll.setOnClickListener { updateFilter("All", chips, it) }
        chipMeals.setOnClickListener { updateFilter("Meal", chips, it) }
        chipActivities.setOnClickListener { updateFilter("Activity", chips, it) }
        chipStatus.setOnClickListener { updateFilter("Status", chips, it) }
    }

    private fun updateFilter(type: String, allChips: List<TextView>, activeChip: View) {
        currentFilterType = type
        allChips.forEach {
            it.setBackgroundResource(R.drawable.bg_chip_unselected)
            it.setTextColor(Color.parseColor("#555555"))
        }
        activeChip.setBackgroundResource(R.drawable.bg_chip_selected)
        (activeChip as TextView).setTextColor(Color.WHITE)
        loadPosts()
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText>(R.id.etSearchPost)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val searchResult = DataManager.activityPosts.filter { 
                    it.contentTitle.lowercase().contains(query) 
                }
                displayList.clear()
                displayList.addAll(searchResult.reversed())
                adapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupPostAction() {
        findViewById<TextView>(R.id.btnPost).setOnClickListener {
            val content = findViewById<EditText>(R.id.etPostContent).text.toString()
            if (content.isNotEmpty()) {
                val newPost = ActivityPost(
                    id = UUID.randomUUID().toString(),
                    teacherName = DataManager.currentTeacher.FullName,
                    role = DataManager.currentTeacher.ProfessionalRank ?: "Giáo viên",
                    dateText = "Vừa xong",
                    contentTitle = content,
                    avatarResId = DataManager.currentTeacher.avatarResId,
                    postType = "Activity"
                )
                DataManager.activityPosts.add(newPost)
                findViewById<EditText>(R.id.etPostContent).text.clear()
                selectedMediaUri = null
                loadPosts()
                Toast.makeText(this, "Đã đăng bài viết thành công!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMediaPickAction() {
        findViewById<LinearLayout>(R.id.btnPickMedia).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
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

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "ACTIVITY")
    }
}
