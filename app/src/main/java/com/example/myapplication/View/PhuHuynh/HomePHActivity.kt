package com.example.myapplication.View.PhuHuynh

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.myapplication.Model.*
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.example.myapplication.View.Adapter.PhuHuynhAdapter
import com.example.myapplication.View.Adapter.StudentSwitcherAdapter
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomePHActivity : AppCompatActivity() {

    private lateinit var rvStudentSwitcher: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val studentList = mutableListOf<JSONObject>()
    private lateinit var switcherAdapter: StudentSwitcherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_phactivity)
        
        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initSwitcher()
        fetchAllChildren()
        
        setupUtilities()
        setupBottomNavigation()
        setupHeaderActions()
    }

    private fun initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshAllData()
        }
    }

    private fun refreshAllData() {
        fetchAllChildren()
        checkBadgesStatus()
    }

    override fun onResume() {
        super.onResume()
        checkBadgesStatus()
    }

    private fun initSwitcher() {
        rvStudentSwitcher = findViewById(R.id.rvStudentSwitcher)
        rvStudentSwitcher.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        switcherAdapter = StudentSwitcherAdapter(studentList, -1) { selectedChild ->
            // Khi chuyển bé
            DataManager.selectedChild = selectedChild
            val selectedId = selectedChild.optInt("studentId")
            switcherAdapter.updateSelectedId(selectedId)

            bindStudentToUI(selectedChild)
            checkBadgesStatus()
            setupMomentsTimeline()
        }
        rvStudentSwitcher.adapter = switcherAdapter
    }

    private fun fetchAllChildren() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempStudents = mutableListOf<JSONObject>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                tempStudents.add(dataArray.getJSONObject(i))
                            }
                        }

                        runOnUiThread {
                            swipeRefreshLayout.isRefreshing = false
                            studentList.clear()
                            studentList.addAll(tempStudents)
                            switcherAdapter.notifyDataSetChanged()

                            // Nếu chưa chọn bé nào, mặc định chọn bé đầu tiên
                            if (tempStudents.isNotEmpty()) {
                                if (DataManager.selectedChild == null) {
                                    DataManager.selectedChild = tempStudents[0]
                                } else {
                                    // Kiểm tra xem bé đã chọn còn trong danh sách mới không
                                    val currentId = DataManager.selectedChild?.optInt("studentId")
                                    val found = tempStudents.find { it.optInt("studentId") == currentId }
                                    if (found != null) {
                                        DataManager.selectedChild = found
                                    } else {
                                        DataManager.selectedChild = tempStudents[0]
                                    }
                                }
                                
                                val selectedId = DataManager.selectedChild?.optInt("studentId") ?: -1
                                switcherAdapter.updateSelectedId(selectedId)
                                bindStudentToUI(DataManager.selectedChild!!)
                                setupMomentsTimeline()
                            }
                            
                            // Ẩn switcher nếu chỉ có 1 bé
                            rvStudentSwitcher.visibility = if (studentList.size > 1) View.VISIBLE else View.GONE
                        }
                    } else {
                        runOnUiThread { swipeRefreshLayout.isRefreshing = false }
                    }
                }
            } catch (e: Exception) {
                Log.e("HOME_API", "Error: ${e.message}")
                runOnUiThread { swipeRefreshLayout.isRefreshing = false }
            }
        }.start()
    }

    private fun bindStudentToUI(child: JSONObject) {
        findViewById<TextView>(R.id.tvStudentName)?.text = child.optString("fullName")
        findViewById<TextView>(R.id.tvClassName)?.text = "Lớp: " + child.optString("className", "N/A")
        
        val avatarUrl = child.optString("avatarUrl")
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
        if (avatarUrl.isNotEmpty() && avatarUrl != "null") {
            Glide.with(this).load(avatarUrl).placeholder(R.drawable.logo).into(imgAvatar)
        } else {
            imgAvatar.setImageResource(R.drawable.logo)
        }

        findViewById<TextView>(R.id.tvStudyStatus)?.text = 
            if (child.optString("enrollmentStatus") == "Active") "ĐANG HỌC TẠI TRƯỜNG" else "NGỪNG HỌC"
            
        findViewById<View>(R.id.cardStudent).setOnClickListener {
            startActivity(Intent(this, ChildProfileActivity::class.java))
        }
    }

    private fun checkBadgesStatus() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient

        // 1. Kiểm tra thông báo chưa đọc (Dùng chung cho tất cả các con)
        val notiRequest = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/notifications")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                client.newCall(notiRequest).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        var hasUnread = false
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                if (!it.getJSONObject(i).optBoolean("isRead", true)) {
                                    hasUnread = true
                                    break
                                }
                            }
                        }
                        runOnUiThread {
                            findViewById<View>(R.id.viewNotiBadge)?.visibility = if (hasUnread) View.VISIBLE else View.GONE
                        }
                    }
                }
            } catch (e: Exception) { }
        }.start()

        // 2. Kiểm tra học phí cho BÉ ĐANG CHỌN
        val selectedStudentId = DataManager.selectedChild?.optInt("studentId") ?: -1
        if (selectedStudentId != -1) {
            val feeRequest = Request.Builder()
                .url("https://web-test.kindercare.app/api/v1/parent/children/$selectedStudentId/invoices")
                .addHeader("Authorization", "Bearer $token")
                .get().build()
            
            Thread {
                try {
                    client.newCall(feeRequest).execute().use { feeResponse ->
                        val feeBody = feeResponse.body?.string()
                        if (feeResponse.isSuccessful && feeBody != null) {
                            val fees = JSONObject(feeBody).optJSONArray("data")
                            var hasUnpaid = false
                            fees?.let {
                                for (i in 0 until it.length()) {
                                    if (it.getJSONObject(i).optString("paymentStatus") == "Unpaid") {
                                        hasUnpaid = true
                                        break
                                    }
                                }
                            }
                            runOnUiThread {
                                findViewById<View>(R.id.viewFeeBadge)?.visibility = if (hasUnpaid) View.VISIBLE else View.GONE
                            }
                        }
                    }
                } catch (e: Exception) { }
            }.start()
        }
    }

    private fun setupUtilities() {
        findViewById<LinearLayout>(R.id.btnFeatureMedication).setOnClickListener {
             startActivity(Intent(this, MedicationRequestPHActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeaturePickUp).setOnClickListener {
             startActivity(Intent(this, PickUpActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureBMI).setOnClickListener {
             startActivity(Intent(this, HealthActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureLeave).setOnClickListener {
             startActivity(Intent(this, LeaveRequestActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureMenu).setOnClickListener {
             startActivity(Intent(this, MealDetailActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureAttendance).setOnClickListener {
             startActivity(Intent(this, AttendanceCalendarActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureAssessment).setOnClickListener {
             startActivity(Intent(this, AssessmentPHActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureFee).setOnClickListener {
             startActivity(Intent(this, FeeActivity::class.java))
        }


        findViewById<LinearLayout>(R.id.btnFeatureSchedule).setOnClickListener {
             startActivity(Intent(this, PlayActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureLessons).setOnClickListener {
             startActivity(Intent(this, DailyLessonsActivity::class.java))
        }
    }

    private fun setupMomentsTimeline() {
        val rvMoments = findViewById<RecyclerView>(R.id.rvMoments)
        val selectedId = DataManager.selectedChild?.optInt("studentId") ?: -1
        
        if (selectedId == -1) return

        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        // Mặc định lấy album hôm nay
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$selectedId/daily-albums?date=$today"
        val request = Request.Builder()
            .url(url)
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
                        val momentItems = mutableListOf<HomeItem>()
                        
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val album = it.getJSONObject(i)
                                val photosArray = album.optJSONArray("photos")
                                val imageUrls = mutableListOf<String>()
                                if (photosArray != null) {
                                    for (j in 0 until photosArray.length()) {
                                        imageUrls.add(photosArray.getJSONObject(j).optString("photoUrl"))
                                    }
                                }
                                
                                val dateText = DateHelper.formatLongToDate(album.optLong("albumDate"))
                                val caption = album.optString("caption", "Hoạt động lớp học")
                                
                                // Chuyển đổi sang Model hiển thị của Adapter
                                momentItems.add(HomeItem.MomentItem(
                                    MomentModel(
                                        dateText = dateText,
                                        isToday = i == 0,
                                        imageResIds = emptyList(),
                                        imageUrls = imageUrls,
                                        caption = caption
                                    )
                                ))
                            }
                        }

                        runOnUiThread {
                            rvMoments.layoutManager = LinearLayoutManager(this@HomePHActivity)
                            if (momentItems.isEmpty()) {
                                // Nếu không có khoảnh khắc nào, có thể hiển thị một thông báo hoặc để trống
                                rvMoments.adapter = PhuHuynhAdapter(emptyList()) { }
                            } else {
                                rvMoments.adapter = PhuHuynhAdapter(momentItems) { }
                            }
                        }
                    } else {
                        runOnUiThread {
                            rvMoments.adapter = PhuHuynhAdapter(emptyList()) { }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ALBUM_API", "Error: ${e.message}")
                runOnUiThread {
                    findViewById<RecyclerView>(R.id.rvMoments).adapter = PhuHuynhAdapter(emptyList()) { }
                }
            }
        }.start()
    }

    private fun setupHeaderActions() {
        findViewById<View>(R.id.btn_thongbao).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<View>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "OVERVIEW")
    }
}
