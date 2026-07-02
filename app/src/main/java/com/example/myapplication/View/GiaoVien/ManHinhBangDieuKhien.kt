package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.*
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

class ManHinhBangDieuKhien : AppCompatActivity() {

    private var currentClassId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_bang_dieu_khien)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        DataManager.initializeDailyAttendance()

        setupBottomNavigation()
        setupNotificationNavigation()
        setupFeatureGrid()
        setupAttendanceCardActions()
        setupMenuAction()
    }

    override fun onResume() {
        super.onResume()
        updateTeacherHeader()
        updateCurrentMealDisplay()
        fetchDashboardStats()
        
        // Schedule and reminders will be fetched after dashboard gives us the classId
    }

    private fun updateTeacherHeader() {
        findViewById<TextView>(R.id.tvTeacherName).text = "${DataManager.currentTeacher.fullName} 👋"
    }

    private fun checkUnreadNotifications() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return
        
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/notifications")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
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
    }

    private fun checkMedicationReminders(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/classes/$classId/medical-requests?date=$date")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        var hasPendingMed = false
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                if (it.getJSONObject(i).optString("status") != "Completed") {
                                    hasPendingMed = true
                                    break
                                }
                            }
                        }
                        runOnUiThread {
                            findViewById<View>(R.id.viewPillBadge)?.visibility = if (hasPendingMed) View.VISIBLE else View.GONE
                            findViewById<TextView>(R.id.tvPillReminder)?.text = 
                                if (hasPendingMed) "Có bé cần uống thuốc chưa xác nhận" else "Đã hoàn thành dặn thuốc hôm nay"
                        }
                    }
                }
            } catch (e: Exception) { }
        }.start()
    }

    private fun fetchDashboardStats() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/dashboard")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataObj = jsonResponse.optJSONObject("data")
                        
                        if (dataObj != null) {
                            val classesArray = dataObj.optJSONArray("classes")
                            if (classesArray != null && classesArray.length() > 0) {
                                val firstClass = classesArray.getJSONObject(0)
                                currentClassId = firstClass.optInt("classId")
                                val className = firstClass.optString("className")
                                val stats = firstClass.optJSONObject("stats")
                                val attendance = stats?.optJSONObject("attendance")
                                
                                val total = stats?.optInt("totalStudents") ?: 0
                                val present = attendance?.optInt("present") ?: 0
                                val absent = attendance?.optInt("absent") ?: 0
                                val excused = attendance?.optInt("excused") ?: 0
                                val noAttendance = attendance?.optInt("noAttendance") ?: 0
                                val unexcused = absent - excused
                                val pendingLeave = stats?.optInt("pendingLeavesCount") ?: 0

                                runOnUiThread {
                                    updateAttendanceUI(className, total, present, absent, excused, unexcused, pendingLeave, noAttendance)
                                }
                                
                                // Fetch schedule and meds for this class
                                val todayTimestamp = Calendar.getInstance().timeInMillis / 1000
                                fetchClassSchedule(currentClassId, todayTimestamp)
                                checkMedicationReminders(currentClassId, todayTimestamp)
                                checkUnreadNotifications()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD_API", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun updateAttendanceUI(className: String, total: Int, present: Int, absent: Int, excused: Int, unexcused: Int, pendingLeave: Int, noAttendance: Int) {
        findViewById<TextView>(R.id.tvClassLabel).text = "TÌNH HÌNH LỚP $className"
        findViewById<TextView>(R.id.tvPresentCountNum).text = String.format(Locale.getDefault(), "%02d", present)
        findViewById<TextView>(R.id.tvTotalCountNum).text = String.format(Locale.getDefault(), "/%02d", total)

        val progressPresent = findViewById<View>(R.id.viewProgressPresent)
        val progressAbsent = findViewById<View>(R.id.viewProgressAbsent)
        
        val totalWeight = total.toFloat().coerceAtLeast(1f)
        val paramsPresent = progressPresent.layoutParams as LinearLayout.LayoutParams
        val paramsAbsent = progressAbsent.layoutParams as LinearLayout.LayoutParams
        
        paramsPresent.weight = (present.toFloat() / totalWeight) * 100
        paramsAbsent.weight = (absent.toFloat() / totalWeight) * 100
        
        progressPresent.layoutParams = paramsPresent
        progressAbsent.layoutParams = paramsAbsent

        findViewById<TextView>(R.id.tvPresentDash).text = "● Có mặt: ${String.format(Locale.getDefault(), "%02d", present)}"
        findViewById<TextView>(R.id.tvAbsentDash).text = "● Vắng: ${String.format(Locale.getDefault(), "%02d", absent)} ($excused phép)"
        findViewById<TextView>(R.id.tvClassSizeDash).text = "$total bé ($noAttendance chưa điểm danh)"

        findViewById<TextView>(R.id.tvPendingLeaveCount).text = "$pendingLeave đơn xin nghỉ chưa duyệt"
        findViewById<View>(R.id.viewLeaveBadge)?.visibility = if (pendingLeave > 0) View.VISIBLE else View.GONE
    }

    private fun fetchClassSchedule(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/schedule?date=$date"
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
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val scheduleList = mutableListOf<DailySchedule>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                scheduleList.add(DailySchedule(
                                    dailyScheduleId = obj.optInt("dailyScheduleId"),
                                    classId = obj.optInt("classId"),
                                    scheduleDate = obj.optLong("scheduleDate"),
                                    startTime = obj.optLong("startTime"),
                                    endTime = obj.optLong("endTime"),
                                    activityName = obj.optString("activityName"),
                                    details = obj.optString("details"),
                                    location = obj.optString("location"),
                                    activityType = obj.optString("activityType"),
                                    status = obj.optString("status")
                                ))
                            }
                        }

                        runOnUiThread {
                            updateScheduleTimeline(scheduleList)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SCHEDULE_API", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun updateScheduleTimeline(schedules: List<DailySchedule>) {
        val container = findViewById<LinearLayout>(R.id.layoutScheduleTimeline)
        container.removeAllViews()
        
        if (schedules.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Không có lịch trình cho ngày này"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 40, 0, 40)
            }
            container.addView(emptyText)
            return
        }

        val inflater = LayoutInflater.from(this)
        val currentTime = System.currentTimeMillis() / 1000

        schedules.forEachIndexed { index, item ->
            val itemView = inflater.inflate(R.layout.item_timeline_schedule, container, false)
            
            val tvTime = itemView.findViewById<TextView>(R.id.tv_time)
            val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
            val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)
            val lineTop = itemView.findViewById<View>(R.id.line_top)
            val lineBottom = itemView.findViewById<View>(R.id.line_bottom)
            val indicatorInner = itemView.findViewById<View>(R.id.indicator_inner)
            val indicatorOuter = itemView.findViewById<View>(R.id.indicator_outer)
            val cardContent = itemView.findViewById<View>(R.id.card_content)

            tvTime.text = DateHelper.formatLongToTime(item.startTime)
            tvTitle.text = "${item.activityName} | ${item.getTimeRange()}"
            
            val status: String
            val statusColor: Int
            val isCurrent: Boolean

            when {
                item.status == "Xong" -> {
                    status = "XONG"
                    statusColor = Color.parseColor("#00B16A")
                    isCurrent = false
                }
                currentTime < item.startTime -> {
                    status = "CHƯA DIỄN RA"
                    statusColor = Color.GRAY
                    isCurrent = false
                }
                currentTime in item.startTime..item.endTime -> {
                    status = "ĐANG DIỄN RA"
                    statusColor = Color.parseColor("#1976D2")
                    isCurrent = true
                }
                else -> {
                    status = "QUÁ GIỜ"
                    statusColor = Color.parseColor("#B91C1C")
                    isCurrent = false
                }
            }

            tvStatus.text = status
            tvStatus.setTextColor(statusColor)

            if (item.status == "Xong") {
                cardContent.setBackgroundResource(R.drawable.bg_white_card)
                indicatorInner.setBackgroundResource(R.drawable.bg_circle_blue_dark) 
                indicatorOuter.visibility = View.INVISIBLE
                cardContent.alpha = 0.6f
            } else if (isCurrent) {
                cardContent.setBackgroundResource(R.drawable.bg_rounded_light_blue)
                indicatorInner.setBackgroundResource(R.drawable.bg_circle_blue_dark)
                indicatorOuter.visibility = View.VISIBLE
                cardContent.alpha = 1.0f
            } else {
                cardContent.setBackgroundResource(R.drawable.bg_white_card)
                indicatorInner.setBackgroundResource(R.drawable.bg_circle_gray)
                indicatorOuter.visibility = View.INVISIBLE
                cardContent.alpha = 1.0f
            }

            cardContent.setOnClickListener {
                if (item.status != "Xong") {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Xác nhận hoạt động")
                        .setMessage("Bạn đã hoàn thành hoạt động '${item.activityName}'?")
                        .setPositiveButton("Hoàn thành") { _, _ ->
                            updateScheduleStatus(item.classId, item.dailyScheduleId, true)
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Thay đổi trạng thái")
                        .setMessage("Chuyển hoạt động '${item.activityName}' về trạng thái chờ?")
                        .setPositiveButton("Đồng ý") { _, _ ->
                            updateScheduleStatus(item.classId, item.dailyScheduleId, false)
                        }
                        .setNegativeButton("Hủy", null)
                        .show()
                }
            }

            if (index == 0) lineTop.visibility = View.INVISIBLE
            if (index == schedules.size - 1) lineBottom.visibility = View.INVISIBLE
            
            if (status == "XONG" || isCurrent) {
                lineTop.setBackgroundColor(Color.parseColor("#00B16A"))
            } else {
                lineTop.setBackgroundColor(Color.parseColor("#E0E0E0"))
            }
            
            if (status == "XONG") {
                lineBottom.setBackgroundColor(Color.parseColor("#00B16A"))
            } else {
                lineBottom.setBackgroundColor(Color.parseColor("#E0E0E0"))
            }

            container.addView(itemView)
        }
    }

    private fun updateScheduleStatus(classId: Int, scheduleId: Int, isCompleted: Boolean) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val json = JSONObject().apply {
            put("completed", isCompleted)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/schedule/$scheduleId/status"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .put(body)
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show()
                            fetchDashboardStats() // Để load lại classId và schedule mới
                        } else {
                            Toast.makeText(this, "Lỗi cập nhật lịch trình", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun updateCurrentMealDisplay() {
        val currentMeal = DataManager.getCurrentMeal()
        val menuDate = DataManager.dailyMenu.date
        
        findViewById<TextView>(R.id.tvMenuDate).text = "Thực đơn ngày $menuDate"
        findViewById<TextView>(R.id.tvMealType).text = currentMeal.MealType
        
        val dishesText = currentMeal.dishes.joinToString(", ") { it.DishName }
        findViewById<TextView>(R.id.tvMealDishes).text = dishesText
    }

    private fun setupAttendanceCardActions() {
        findViewById<View>(R.id.layoutPendingLeave).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangQuanLyDonXinNghi::class.java))
        }
        findViewById<View>(R.id.layoutPillReminder).setOnClickListener {
            startActivity(Intent(this, ManHinhDanThuoc::class.java))
        }
    }

    private fun setupFeatureGrid() {
        setupFeatureItem(
            R.id.btnFeatureDiemDanhGrid,
            "Điểm danh",
            R.drawable.ic_attendance,
            R.color.primary_green
        ) { startActivity(Intent(this, ManHinhDiemDanh::class.java)) }

        setupFeatureItem(
            R.id.btnFeatureDanhSachLopGrid,
            "Lớp học",
            R.drawable.ic_class_list,
            R.color.secondary_blue
        ) { startActivity(Intent(this, ManHinhDanhSachLop::class.java)) }

        setupFeatureItem(
            R.id.btnFeatureHoatDongGrid,
            "Hoạt động",
            R.drawable.ic_activity,
            R.color.secondary_orange
        ) { startActivity(Intent(this, ManHinhHoatDong::class.java)) }

        setupFeatureItem(
            R.id.btnFeatureChatGrid,
            "Hòm thư",
            R.drawable.ic_mail,
            R.color.teal_700
        ) { startActivity(Intent(this, ManHinhHomThuGiaoVien::class.java)) }

        setupFeatureItem(
            R.id.btnFeatureDuyetDonGrid,
            "Duyệt đơn",
            R.drawable.ic_bell,
            R.color.success
        ) { startActivity(Intent(this, ManHinhChucNangQuanLyDonXinNghi::class.java)) }

        setupFeatureItem(
            R.id.btnFeatureSucKhoeGrid,
            "Dặn thuốc",
            R.drawable.ic_pill,
            R.color.error
        ) { startActivity(Intent(this, ManHinhDanThuoc::class.java)) }

        // 7. Nhật ký (MỚI)
        setupFeatureItem(
            R.id.btnFeatureNhatKyGrid,
            "Nhật ký",
            R.drawable.ic_edit,
            R.color.dark_green_text
        ) { startActivity(Intent(this, ManHinhChucNangCapNhatKhauPhanAn::class.java)) }
    }

    private fun setupFeatureItem(viewId: Int, label: String, iconRes: Int, colorRes: Int, onClick: () -> Unit) {
        val view = findViewById<View>(viewId)
        view.findViewById<TextView>(R.id.tvFeatureLabel).text = label
        
        val iconBg = view.findViewById<View>(R.id.viewFeatureIconBg)
        iconBg.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
        
        val iconView = view.findViewById<ImageView>(R.id.imgFeatureIcon)
        iconView.setImageResource(iconRes)
        
        view.setOnClickListener { onClick() }
    }

    private fun setupMenuAction() {
        findViewById<TextView>(R.id.btnViewDetailMenu).setOnClickListener {
            startActivity(Intent(this, ManHinhChiTietThucDon::class.java))
        }
    }

    private fun setupNotificationNavigation() {
        findViewById<View>(R.id.layoutNotification).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangThongBao::class.java))
        }
        findViewById<View>(R.id.imgAvatar).setOnClickListener {
            startActivity(Intent(this, ManHinhThongTinTaiKhoanCaNhan::class.java))
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "DASHBOARD")
    }
}
