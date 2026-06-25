package com.example.myapplication.View.GiaoVien

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import java.util.Locale

class ManHinhBangDieuKhien : AppCompatActivity() {
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
        setupAttendanceCard()
        setupMenuAction()
    }

    override fun onResume() {
        super.onResume()
        updateCurrentMealDisplay()
        updateAttendanceDisplay()
        updateScheduleTimeline()
    }

    private fun updateCurrentMealDisplay() {
        val currentMeal = DataManager.getCurrentMeal()
        val menuDate = DataManager.dailyMenu.date
        
        findViewById<TextView>(R.id.tvMenuDate).text = "Thực đơn ngày $menuDate"
        findViewById<TextView>(R.id.tvMealTime).text = "Bữa ăn"
        findViewById<TextView>(R.id.tvMealType).text = currentMeal.MealType
        
        val dishesText = currentMeal.dishes.joinToString(", ") { it.DishName }
        findViewById<TextView>(R.id.tvMealDishes).text = dishesText
    }

    private fun updateAttendanceDisplay() {
        val stats = DataManager.getAttendanceStats(DataManager.studentList)
        val pendingLeave = DataManager.leaveRequests.count { it.Status == "Pending" }

        findViewById<TextView>(R.id.tvPresentCountNum).text = String.format(Locale.getDefault(), "%02d", stats.present)
        findViewById<TextView>(R.id.tvTotalCountNum).text = String.format(Locale.getDefault(), "/%02d", stats.total)

        val progressPresent = findViewById<View>(R.id.viewProgressPresent)
        val progressAbsent = findViewById<View>(R.id.viewProgressAbsent)
        
        val paramsPresent = progressPresent.layoutParams as LinearLayout.LayoutParams
        val paramsAbsent = progressAbsent.layoutParams as LinearLayout.LayoutParams
        
        if (stats.total > 0) {
            paramsPresent.weight = stats.present.toFloat()
            paramsAbsent.weight = stats.absent.toFloat()
        } else {
            paramsPresent.weight = 0f
            paramsAbsent.weight = 0f
        }
        
        progressPresent.layoutParams = paramsPresent
        progressAbsent.layoutParams = paramsAbsent

        findViewById<TextView>(R.id.tvPresentDash).text = "● Có mặt: ${String.format(Locale.getDefault(), "%02d", stats.present)}"
        findViewById<TextView>(R.id.tvAbsentDash).text = "● Vắng: ${stats.absent} (${stats.excused} phép)"
        findViewById<TextView>(R.id.tvClassSizeDash).text = "${stats.total} bé"

        findViewById<TextView>(R.id.tvPendingLeaveCount).text = "Có $pendingLeave đơn xin nghỉ đang chờ duyệt"
    }

    private fun updateScheduleTimeline() {
        val container = findViewById<LinearLayout>(R.id.layoutScheduleTimeline)
        container.removeAllViews()
        
        val schedules = DataManager.getMockSchedules()
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

            tvTime.text = DateHelper.formatLongToTime(item.StartTime)
            tvTitle.text = "${item.ActivityName} | ${item.getTimeRange()}"
            
            val status: String
            val statusColor: Int
            val isCurrent: Boolean

            when {
                currentTime < item.StartTime -> {
                    status = "CHƯA DIỄN RA"
                    statusColor = Color.GRAY
                    isCurrent = false
                }
                currentTime in item.StartTime..item.EndTime -> {
                    status = "ĐANG DIỄN RA"
                    statusColor = Color.parseColor("#1976D2")
                    isCurrent = true
                }
                else -> {
                    status = "XONG"
                    statusColor = Color.parseColor("#00B16A")
                    isCurrent = false
                }
            }

            tvStatus.text = status
            tvStatus.setTextColor(statusColor)

            if (isCurrent) {
                cardContent.setBackgroundResource(R.drawable.bg_rounded_light_blue)
                indicatorInner.setBackgroundResource(R.drawable.bg_circle_blue_dark)
                indicatorOuter.visibility = View.VISIBLE
            } else if (status == "XONG") {
                cardContent.setBackgroundResource(R.drawable.bg_white_card)
                indicatorInner.setBackgroundResource(R.drawable.bg_circle_blue_dark) 
                indicatorOuter.visibility = View.INVISIBLE
            } else {
                cardContent.setBackgroundResource(R.drawable.bg_white_card)
                indicatorInner.setBackgroundResource(R.drawable.bg_circle_gray)
                indicatorOuter.visibility = View.INVISIBLE
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

    private fun setupAttendanceCard() {
        findViewById<Button>(R.id.btnProcessAttendance).setOnClickListener {
            startActivity(Intent(this, ManHinhDiemDanh::class.java))
        }
        findViewById<LinearLayout>(R.id.layoutPendingLeave).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangQuanLyDonXinNghi::class.java))
        }
        findViewById<LinearLayout>(R.id.layoutPillReminder).setOnClickListener {
            startActivity(Intent(this, ManHinhDanThuoc::class.java))
        }
    }

    private fun setupFeatureGrid() {
        findViewById<LinearLayout>(R.id.btnFeatureDiemDanhGrid).setOnClickListener {
            startActivity(Intent(this, ManHinhDiemDanh::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureDanhSachLopGrid).setOnClickListener {
            startActivity(Intent(this, ManHinhDanhSachLop::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureHoatDongGrid).setOnClickListener {
            startActivity(Intent(this, ManHinhHoatDong::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureChatGrid).setOnClickListener {
            startActivity(Intent(this, ManHinhChatGiaoVien::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureDuyetDonGrid).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangQuanLyDonXinNghi::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFeatureSucKhoeGrid).setOnClickListener {
            startActivity(Intent(this, ManHinhDanThuoc::class.java))
        }
    }

    private fun setupMenuAction() {
        findViewById<TextView>(R.id.btnViewDetailMenu).setOnClickListener {
            startActivity(Intent(this, ManHinhChiTietThucDon::class.java))
        }
        findViewById<Button>(R.id.btnUpdateMealPortion).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangCapNhatKhauPhanAn::class.java))
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
