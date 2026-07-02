package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.*
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.CalendarAdapter
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar

class AttendanceCalendarActivity : AppCompatActivity() {

    private lateinit var rvCalendar: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private val dayList = mutableListOf<AttendanceDay>()
    
    private lateinit var tvCalendarMonth: TextView
    private lateinit var btnBack: ImageView
    private lateinit var tvDetailDate: TextView
    private lateinit var tvDetailStatus: TextView
    private lateinit var tvCheckInTime: TextView
    private lateinit var tvCheckOutTime: TextView
    private lateinit var tvPickedUpBy: TextView
    private lateinit var cardAttendanceDetail: View

    private val calendar = Calendar.getInstance()
    private val attendanceData = mutableMapOf<String, JSONObject>()
    private var currentStudentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_attendance_calendar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupCalendar()

        // Ưu tiên lấy bé đang chọn
        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            currentStudentId = selectedChild.getInt("studentId")
            findViewById<TextView>(R.id.tvStudentName)?.text = selectedChild.optString("fullName")
            findViewById<TextView>(R.id.tvClassName)?.text = selectedChild.optString("className", "N/A")
            fetchAttendanceHistory(currentStudentId)
        } else {
            fetchChildAndLoadAttendance()
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun initViews() {
        rvCalendar = findViewById(R.id.rvCalendar)
        tvCalendarMonth = findViewById(R.id.tvCalendarMonth)
        btnBack = findViewById(R.id.btnBack)
        tvDetailDate = findViewById(R.id.tvDetailDate)
        tvDetailStatus = findViewById(R.id.tvDetailStatus)
        tvCheckInTime = findViewById(R.id.tvCheckInTime)
        tvCheckOutTime = findViewById(R.id.tvCheckOutTime)
        tvPickedUpBy = findViewById(R.id.tvPickedUpBy)
        cardAttendanceDetail = findViewById(R.id.cardAttendanceDetail)
    }

    private fun setupCalendar() {
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        tvCalendarMonth.text = "Tháng $month / $year"

        dayList.clear()
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        
        for (i in 0 until firstDayOfWeek) {
            dayList.add(AttendanceDay("", 0))
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            dayList.add(AttendanceDay(i.toString(), 0))
        }

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        adapter = CalendarAdapter(dayList) { selectedDay ->
            updateDetailSection(selectedDay)
        }
        rvCalendar.adapter = adapter
    }

    private fun fetchChildAndLoadAttendance() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        if (dataArray != null && dataArray.length() > 0) {
                            val child = dataArray.getJSONObject(0)
                            currentStudentId = child.getInt("studentId")
                            runOnUiThread {
                                findViewById<TextView>(R.id.tvStudentName)?.text = child.optString("fullName")
                                findViewById<TextView>(R.id.tvClassName)?.text = child.optString("className", "N/A")
                            }
                            fetchAttendanceHistory(currentStudentId)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun fetchAttendanceHistory(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/attendance")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val dataArray = JSONObject(body).optJSONArray("data")
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                val dateStr = DateHelper.formatLongToDate(obj.optLong("attendanceDate"))
                                attendanceData[dateStr] = obj
                            }
                        }
                        runOnUiThread {
                            updateCalendarColors()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun updateCalendarColors() {
        for (i in 0 until dayList.size) {
            val day = dayList[i]
            if (day.dayNumber.isNotEmpty()) {
                val dateStr = String.format("%02d/%02d/%d", day.dayNumber.toInt(), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
                val data = attendanceData[dateStr]
                if (data != null) {
                    val statusInt = when (data.optString("status")) {
                        "Present" -> 1
                        "Absent" -> 2
                        else -> 0
                    }
                    dayList[i] = day.copy(status = statusInt)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun updateDetailSection(day: AttendanceDay) {
        if (day.dayNumber.isEmpty()) return
        val dateStr = String.format("%02d/%02d/%d", day.dayNumber.toInt(), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
        val data = attendanceData[dateStr]
        
        cardAttendanceDetail.visibility = View.VISIBLE
        tvDetailDate.text = "Chi tiết ngày $dateStr"
        
        if (data != null) {
            tvDetailStatus.text = if (data.optString("status") == "Present") "Trạng thái: Hiện diện" else "Trạng thái: Vắng mặt"
            tvCheckInTime.text = DateHelper.formatLongToTime(data.optLong("checkInTime"))
            tvCheckOutTime.text = if (data.isNull("checkOutTime")) "--:--" else DateHelper.formatLongToTime(data.optLong("checkOutTime"))
            tvPickedUpBy.text = "Người đón: " + data.optString("pickedUpBy", "Chưa rõ")
        } else {
            tvDetailStatus.text = "Trạng thái: Chưa có dữ liệu"
            tvCheckInTime.text = "--:--"
            tvCheckOutTime.text = "--:--"
            tvPickedUpBy.text = "Người đón: --"
        }
    }
}
