package com.example.myapplication.View.GiaoVien

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.Adapter.DiemDanhAdapter
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import java.text.SimpleDateFormat
import java.util.*

class ManHinhDiemDanh : AppCompatActivity() {

    private lateinit var rvDiemDanh: RecyclerView
    private lateinit var adapter: DiemDanhAdapter
    private lateinit var tvPresent: TextView
    private lateinit var tvAbsent: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvAbsentDetail: TextView
    private lateinit var tvAttendanceDate: TextView
    private lateinit var btnSaveAttendance: LinearLayout

    private var selectedDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val todayString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_diem_danh)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupSearch()
        setupNotificationNavigation()
        setupSaveButton()
        setupAvatarNavigation()
        setupDatePicker()
        
        // Initial load
        loadAttendanceData(todayString)
    }

    private fun initViews() {
        tvPresent = findViewById(R.id.tvPresentCount)
        tvAbsent = findViewById(R.id.tvAbsentCount)
        tvTotal = findViewById(R.id.tvTotalCount)
        tvAbsentDetail = findViewById(R.id.tvAbsentDetail)
        tvAttendanceDate = findViewById(R.id.tvAttendanceDate)
        rvDiemDanh = findViewById(R.id.revDiemDanh)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)
    }

    private fun setupAvatarNavigation() {
        findViewById<View>(R.id.imgAvatar).setOnClickListener {
            startActivity(Intent(this, ManHinhThongTinTaiKhoanCaNhan::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = DiemDanhAdapter(mutableListOf()) {
            updateStats()
        }
        rvDiemDanh.layoutManager = LinearLayoutManager(this)
        rvDiemDanh.adapter = adapter
    }

    private fun loadAttendanceData(dateStr: String) {
        val isToday = dateStr == todayString
        tvAttendanceDate.text = if (isToday) "Hôm nay, $dateStr" else dateStr

        val historyData = DataManager.getAttendanceForDate(dateStr)
        
        if (historyData != null) {
            adapter.updateData(historyData)
            adapter.isReadOnly = true
            btnSaveAttendance.visibility = View.GONE
        } else if (isToday) {
            DataManager.initializeDailyAttendance()
            adapter.updateData(DataManager.studentList)
            
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour >= 9) {
                adapter.isReadOnly = true
                btnSaveAttendance.visibility = View.GONE
            } else {
                adapter.isReadOnly = false
                btnSaveAttendance.visibility = View.VISIBLE
            }
        } else {
            // Không có dữ liệu cho ngày cũ
            adapter.updateData(emptyList())
            adapter.isReadOnly = true
            btnSaveAttendance.visibility = View.GONE
            Toast.makeText(this, "Không có dữ liệu điểm danh cho ngày này", Toast.LENGTH_SHORT).show()
        }
        updateStats()
    }

    private fun updateStats() {
        val dateStr = dateFormat.format(selectedDate.time)
        val data = if (dateStr == todayString) DataManager.studentList else DataManager.getAttendanceForDate(dateStr) ?: emptyList()

        val stats = DataManager.getAttendanceStats(data)

        tvPresent.text = String.format(Locale.getDefault(), "%02d", stats.present)
        tvAbsent.text = String.format(Locale.getDefault(), "%02d", stats.absent)
        tvTotal.text = String.format(Locale.getDefault(), "%02d", stats.total)
        tvAbsentDetail.text = "${stats.excused} Có phép | ${stats.unexcused} Không phép"
    }

    private fun setupDatePicker() {
        findViewById<View>(R.id.btnSelectDate).setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    val dateStr = dateFormat.format(selectedDate.time)
                    loadAttendanceData(dateStr)
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText>(R.id.etSearchAttendance)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupNotificationNavigation() {
        findViewById<View>(R.id.layoutNotification).setOnClickListener {
            startActivity(Intent(this, ManHinhChucNangThongBao::class.java))
        }
    }

    private fun setupSaveButton() {
        btnSaveAttendance.setOnClickListener {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour >= 9) {
                Toast.makeText(this, "Đã quá 09:00, không thể thay đổi điểm danh", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = DataManager.studentList
            val total = data.size
            val present = data.count { it.attendanceStatus == "Hiện diện" }
            val absent = data.count { it.attendanceStatus == "Nghỉ" }
            val pending = data.count { it.attendanceStatus == "Chưa có mặt" }

            if (pending > 0) {
                Toast.makeText(this, "Vui lòng hoàn tất điểm danh cho $pending bé còn lại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lưu vào lịch sử trước khi chuyển màn hình (giả lập)
            DataManager.saveAttendanceForDate(todayString, data)

            val intent = Intent(this, ManHinhXacNhanLuuDiemDanhVaGuiThongBao::class.java)
            intent.putExtra("PRESENT_COUNT", present)
            intent.putExtra("ABSENT_COUNT", absent)
            intent.putExtra("TOTAL_COUNT", total)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "ATTENDANCE")
    }
}
