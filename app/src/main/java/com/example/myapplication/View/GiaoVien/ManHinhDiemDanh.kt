package com.example.myapplication.View.GiaoVien

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.View.Adapter.DiemDanhAdapter
import com.example.myapplication.Model.*
import com.example.myapplication.R
import com.example.myapplication.Utils.NavigationUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
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
    private lateinit var btnSaveAttendance: View

    private var selectedDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    private val studentList = mutableListOf<TeacherStudentResponse>()

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
        setupQRScanner()
        
        refreshData()
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

    private fun setupQRScanner() {
        val btnScanQR = findViewById<FloatingActionButton>(R.id.btnScanQR)
        val scanner = GmsBarcodeScanning.getClient(this)

        btnScanQR.setOnClickListener {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue ?: ""
                    handleScannedData(rawValue)
                }
                .addOnFailureListener { e ->
                    Log.e("QR_SCAN", "Lỗi quét mã: ${e.message}")
                    Toast.makeText(this, "Không thể quét mã QR", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleScannedData(qrToken: String) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("qrToken", qrToken)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val url = "https://web-test.kindercare.app/api/v1/teacher/attendance/scan"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        runOnUiThread {
            Toast.makeText(this, "Đang xử lý mã QR...", Toast.LENGTH_SHORT).show()
        }

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        when (response.code) {
                            200 -> {
                                val msg = JSONObject(resBody ?: "{}").optString("message", "Điểm danh thành công")
                                Toast.makeText(this, "✅ $msg", Toast.LENGTH_LONG).show()
                                refreshData() // Tải lại danh sách để cập nhật UI
                            }
                            400 -> Toast.makeText(this, "❌ Mã QR không hợp lệ hoặc hết hạn", Toast.LENGTH_LONG).show()
                            409 -> {
                                val msg = JSONObject(resBody ?: "{}").optString("message", "Mã đã được sử dụng hoặc bé đã điểm danh")
                                Toast.makeText(this, "⚠️ $msg", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                val msg = JSONObject(resBody ?: "{}").optString("message", "Lỗi máy chủ: ${response.code}")
                                Toast.makeText(this, "❌ $msg", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun setupAvatarNavigation() {
        findViewById<View>(R.id.imgAvatar).setOnClickListener {
            startActivity(Intent(this, ManHinhThongTinTaiKhoanCaNhan::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = DiemDanhAdapter(studentList) {
            updateStatsSummary()
        }
        rvDiemDanh.layoutManager = LinearLayoutManager(this)
        rvDiemDanh.adapter = adapter
    }

    private fun refreshData() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDate.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startOfDayTimestamp = cal.timeInMillis / 1000
        loadAttendanceData(1, startOfDayTimestamp)
    }

    private fun loadAttendanceData(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        tvAttendanceDate.text = dateFormat.format(date * 1000L)

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/students?date=$date"
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
                        
                        val tempStudents = mutableListOf<TeacherStudentResponse>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                tempStudents.add(parseStudent(it.getJSONObject(i)))
                            }
                        }

                        runOnUiThread {
                            studentList.clear()
                            studentList.addAll(tempStudents)
                            
                            // Áp dụng logic trạng thái mặc định
                            applyLeaveLogicToUnmarkedStudents()
                            
                            adapter.updateData(studentList)
                            updateStatsSummary()
                            
                            val today = Calendar.getInstance()
                            val isToday = today.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                                          today.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                            
                            adapter.isReadOnly = !isToday
                            findViewById<View>(R.id.btnScanQR).visibility = if (isToday) View.VISIBLE else View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ATTENDANCE_LIST_API", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun applyLeaveLogicToUnmarkedStudents() {
        studentList.forEach { student ->
            // LOGIC MỚI: 
            // 1. Nếu có đơn nghỉ đã DUYỆT cho ngày này
            if (student.leaveRequest != null && student.leaveRequest.status == "Approved") {
                // 2. VÀ bé CHƯA có giờ vào lớp thực tế (chưa quét QR/chưa nhấn tay)
                if (student.checkInTime == null) {
                    student.status = "Absent" // ÉP BUỘC trạng thái là Nghỉ
                }
                // Nếu checkInTime != null, nghĩa là bé đã đến trường thực tế -> Giữ "Present"
            }
        }
    }

    private fun parseStudent(obj: JSONObject): TeacherStudentResponse {
        val leaveReqJson = obj.optJSONObject("leaveRequest")
        val leaveReq = if (leaveReqJson != null) {
            LeaveRequestShort(
                requestId = leaveReqJson.optInt("requestId"),
                status = leaveReqJson.optString("status"),
                reason = leaveReqJson.optString("reason")
            )
        } else null

        return TeacherStudentResponse(
            studentId = obj.optInt("studentId"),
            fullName = obj.optString("fullName", "Học sinh"),
            avatarUrl = obj.optString("avatarUrl"),
            status = if (obj.isNull("status")) null else obj.optString("status"),
            checkInTime = if (obj.isNull("checkInTime")) null else obj.optLong("checkInTime"),
            checkOutTime = if (obj.isNull("checkOutTime")) null else obj.optLong("checkOutTime"),
            pickedUpBy = if (obj.isNull("pickedUpBy")) null else obj.optString("pickedUpBy"),
            healthNote = obj.optString("healthNote"),
            leaveRequest = leaveReq
        )
    }

    private fun updateStatsSummary() {
        val total = studentList.size
        val present = studentList.count { it.status == "Present" }
        val absent = studentList.count { it.status == "Absent" }
        val excused = studentList.count { it.status == "Absent" && it.leaveRequest?.status == "Approved" }
        val unexcused = absent - excused

        tvPresent.text = String.format(Locale.getDefault(), "%02d", present)
        tvAbsent.text = String.format(Locale.getDefault(), "%02d", absent)
        tvTotal.text = String.format(Locale.getDefault(), "%02d", total)
        tvAbsentDetail.text = "$excused Có phép | $unexcused Không phép"
    }

    private fun setupDatePicker() {
        findViewById<View>(R.id.btnSelectDate).setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                    selectedDate.set(y, m, d)
                    refreshData()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearchAttendance).addTextChangedListener(object : TextWatcher {
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
        findViewById<View>(R.id.btnSaveAttendance).setOnClickListener {
            val pending = studentList.count { it.status == null }
            if (pending > 0) {
                Toast.makeText(this, "Vui lòng hoàn tất điểm danh cho $pending bé", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attendanceItems = studentList.map { 
                AttendanceDataItem(
                    studentId = it.studentId,
                    status = it.status ?: "Absent",
                    checkInTime = it.checkInTime ?: (System.currentTimeMillis() / 1000),
                    checkOutTime = it.checkOutTime,
                    pickedUpBy = it.pickedUpBy
                )
            }
            
            DataManager.currentAttendanceData = QuickAttendanceRequest(
                classId = 1,
                date = selectedDate.timeInMillis / 1000,
                attendanceData = attendanceItems
            )

            val intent = Intent(this, ManHinhXacNhanLuuDiemDanhVaGuiThongBao::class.java).apply {
                putExtra("PRESENT_COUNT", studentList.count { it.status == "Present" })
                putExtra("ABSENT_COUNT", studentList.count { it.status == "Absent" })
                putExtra("TOTAL_COUNT", studentList.size)
            }
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        NavigationUtils.setupBottomNavigationGV(this, "ATTENDANCE")
    }
}
