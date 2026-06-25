package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.CalendarAdapter
import com.example.myapplication.View.Adapter.EventAdapter
import com.example.myapplication.Model.AttendanceDay
import com.example.myapplication.Model.AttendanceRecord
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.UpcomingEvent
import com.example.myapplication.Utils.NavigationUtils
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar

class AttendanceCalendarActivity : AppCompatActivity() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var rvEvents: RecyclerView
    private lateinit var btnBack: ImageView
    
    // Chi tiết điểm danh
    private lateinit var cardAttendanceDetail: CardView
    private lateinit var tvDetailDate: TextView
    private lateinit var tvDetailStatus: TextView
    private lateinit var tvCheckInTime: TextView
    private lateinit var tvCheckOutTime: TextView
    private lateinit var tvPickedUpBy: TextView
    private lateinit var tvCalendarMonth: TextView

    private val daysData = mutableListOf<AttendanceDay>()
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_attendance_calendar)

        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupCalendarRecyclerView()
        setupEventsRecyclerView()
        setupBottomNavigation()

        val studentId = DataManager.studentList.firstOrNull()?.StudentID ?: -1
        if (studentId != -1) {
            fetchAttendanceData(studentId)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun initViews() {
        rvCalendar = findViewById(R.id.rvCalendar)
        rvEvents = findViewById(R.id.rvEvents)
        btnBack = findViewById(R.id.btnBack)
        
        cardAttendanceDetail = findViewById(R.id.cardAttendanceDetail)
        tvDetailDate = findViewById(R.id.tvDetailDate)
        tvDetailStatus = findViewById(R.id.tvDetailStatus)
        tvCheckInTime = findViewById(R.id.tvCheckInTime)
        tvCheckOutTime = findViewById(R.id.tvCheckOutTime)
        tvPickedUpBy = findViewById(R.id.tvPickedUpBy)
        tvCalendarMonth = findViewById(R.id.tvCalendarMonth)
    }

    private fun setupCalendarRecyclerView() {
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        calendarAdapter = CalendarAdapter(daysData) { day ->
            showAttendanceDetail(day)
        }
        rvCalendar.adapter = calendarAdapter
    }

    private fun showAttendanceDetail(day: AttendanceDay) {
        val record = day.record
        if (record != null) {
            cardAttendanceDetail.visibility = View.VISIBLE
            tvDetailDate.text = "Chi tiết ngày ${DateHelper.formatLongToDate(record.attendanceDate)}"
            
            val statusText = when(record.status) {
                "Present" -> "Hiện diện"
                "Absent" -> "Vắng mặt"
                "Leave" -> "Nghỉ có phép"
                else -> record.status
            }
            tvDetailStatus.text = "Trạng thái: $statusText"
            
            // Đổi màu text trạng thái
            if (record.status == "Present") {
                tvDetailStatus.setTextColor(android.graphics.Color.parseColor("#005A36"))
            } else {
                tvDetailStatus.setTextColor(android.graphics.Color.RED)
            }

            tvCheckInTime.text = record.checkInTime?.let { DateHelper.formatLongToTime(it) } ?: "--:--"
            tvCheckOutTime.text = record.checkOutTime?.let { DateHelper.formatLongToTime(it) } ?: "--:--"
            tvPickedUpBy.text = "Người đón: ${record.pickedUpBy?.ifEmpty { "Chưa có thông tin" } ?: "Chưa có thông tin"}"
        } else {
            cardAttendanceDetail.visibility = View.GONE
        }
    }

    private fun fetchAttendanceData(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/attendance")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("ATTENDANCE_API", "Kết quả: $body")
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val records = mutableListOf<AttendanceRecord>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                records.add(AttendanceRecord(
                                    attendanceId = obj.optInt("attendanceId"),
                                    studentId = obj.optInt("studentId"),
                                    attendanceDate = obj.optLong("attendanceDate"),
                                    status = obj.optString("status"),
                                    checkInTime = if (obj.isNull("checkInTime")) null else obj.optLong("checkInTime"),
                                    checkOutTime = if (obj.isNull("checkOutTime")) null else obj.optLong("checkOutTime"),
                                    pickedUpBy = obj.optString("pickedUpBy", "")
                                ))
                            }
                        }
                        
                        runOnUiThread {
                            updateCalendar(records)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ATTENDANCE_API", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun updateCalendar(records: List<AttendanceRecord>) {
        daysData.clear()
        
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        tvCalendarMonth.text = "Tháng $currentMonth, $currentYear"

        // Logic hiển thị ngày trong tháng hiện tại
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (i in 1..maxDay) {
            val dateStr = String.format("%02d", i)
            val record = records.find { 
                val rCal = Calendar.getInstance()
                rCal.timeInMillis = it.attendanceDate * 1000L
                rCal.get(Calendar.DAY_OF_MONTH) == i && (rCal.get(Calendar.MONTH) + 1) == currentMonth
            }
            
            val status = when(record?.status) {
                "Present" -> 1
                "Absent", "Leave" -> 2
                else -> 0
            }
            daysData.add(AttendanceDay(dateStr, status, record))
        }
        calendarAdapter.notifyDataSetChanged()
    }

    private fun setupEventsRecyclerView() {
        rvEvents.layoutManager = LinearLayoutManager(this)
        rvEvents.adapter = EventAdapter(generateDummyEvents())
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationPH(this, "OVERVIEW")
    }

    private fun generateDummyEvents(): List<UpcomingEvent> {
        return listOf(
            UpcomingEvent("15", "5", "Họp phụ huynh đầu năm", "09:00 AM", "Phòng đa năng"),
            UpcomingEvent("20", "5", "Lễ hội bé khỏe bé ngoan", "08:30 AM", "Sân trường chính")
        )
    }
}
