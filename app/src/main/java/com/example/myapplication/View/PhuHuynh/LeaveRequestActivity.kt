package com.example.myapplication.View.PhuHuynh

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.LeaveRequestModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.LeaveHistoryAdapter
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LeaveRequestActivity : AppCompatActivity() {

    private lateinit var tvFromDate: TextView
    private lateinit var tvToDate: TextView
    private lateinit var cbMorning: CheckBox
    private lateinit var cbAfternoon: CheckBox
    private lateinit var spReasonCategory: Spinner
    private lateinit var edtReasonDetail: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView
    
    private lateinit var rvLeaveHistory: RecyclerView
    private lateinit var leaveHistoryAdapter: LeaveHistoryAdapter
    private val leaveHistoryList = mutableListOf<LeaveRequestModel>()

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    private var fromDateLong: Long = 0
    private var toDateLong: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leave_request)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupReasonSpinner()
        setupHistoryRecyclerView()

        val studentId = DataManager.studentList.firstOrNull()?.StudentID ?: -1
        if (studentId != -1) {
            fetchLeaveHistory(studentId)
            
            // Đổ thông tin cơ bản
            val student = DataManager.studentList.firstOrNull()
            findViewById<TextView>(R.id.tvStudentName).text = student?.FullName ?: "N/A"
            findViewById<TextView>(R.id.tvClassName).text = "Mầm non 1"
            findViewById<TextView>(R.id.tvParentName).text = student?.parentName ?: "N/A"
        }

        btnBack.setOnClickListener { finish() }

        tvFromDate.setOnClickListener {
            showDatePickerDialog(tvFromDate, "Từ ngày") { timestamp -> fromDateLong = timestamp }
        }
        tvToDate.setOnClickListener {
            showDatePickerDialog(tvToDate, "Đến ngày") { timestamp -> toDateLong = timestamp }
        }

        btnSubmit.setOnClickListener {
            handleFormSubmit()
        }
    }

    private fun initViews() {
        tvFromDate = findViewById(R.id.tvFromDate)
        tvToDate = findViewById(R.id.tvToDate)
        cbMorning = findViewById(R.id.cbMorning)
        cbAfternoon = findViewById(R.id.cbAfternoon)
        spReasonCategory = findViewById(R.id.spReasonCategory)
        edtReasonDetail = findViewById(R.id.edtReasonDetail)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
        rvLeaveHistory = findViewById(R.id.rvLeaveHistory)
    }

    private fun setupReasonSpinner() {
        val reasons = listOf("Chọn lý do xin nghỉ", "Bị ốm/Sốt", "Đi du lịch cùng gia đình", "Có việc gia đình riêng", "Khác")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spReasonCategory.adapter = adapter

        // Thêm sự kiện lắng nghe thay đổi item trong Spinner
        spReasonCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedReason = reasons[position]
                // Nếu chọn "Khác" thì hiện EditText, ngược lại ẩn đi
                if (selectedReason == "Khác") {
                    edtReasonDetail.visibility = android.view.View.VISIBLE
                } else {
                    edtReasonDetail.visibility = android.view.View.GONE
                    edtReasonDetail.setText("") // Xóa nội dung khi ẩn
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                edtReasonDetail.visibility = android.view.View.GONE
            }
        }
    }

    private fun setupHistoryRecyclerView() {
        rvLeaveHistory.layoutManager = LinearLayoutManager(this)
        leaveHistoryAdapter = LeaveHistoryAdapter(leaveHistoryList)
        rvLeaveHistory.adapter = leaveHistoryAdapter
    }

    private fun fetchLeaveHistory(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/leave-requests")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("LEAVE_API", "Kết quả trả về: $body")
                    
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempHistory = mutableListOf<LeaveRequestModel>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                tempHistory.add(LeaveRequestModel(
                                    RequestID = obj.optInt("requestId"),
                                    StudentID = obj.optInt("studentId"),
                                    ParentID = obj.optInt("parentId"),
                                    FromDate = obj.optLong("fromDate"),
                                    ToDate = obj.optLong("toDate"),
                                    Reason = obj.optString("reason", ""),
                                    EvidenceURL = obj.optString("evidenceUrl", ""),
                                    Status = obj.optString("status", "Pending"),
                                    ApproverID = if (obj.isNull("approverId")) null else obj.optInt("approverId"),
                                    IsMealFeeDeducted = obj.optBoolean("isMealFeeDeducted"),
                                    ParentNotes = obj.optString("parentNotes", ""),
                                    CreatedAt = obj.optLong("createdAt")
                                ))
                            }
                        }
                        
                        runOnUiThread {
                            leaveHistoryList.clear()
                            leaveHistoryList.addAll(tempHistory)
                            leaveHistoryAdapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LEAVE_API", "Lỗi: ${e.message}")
            }
        }.start()
    }

    private fun handleFormSubmit() {
        val selectedReason = spReasonCategory.selectedItem.toString()
        if (selectedReason == "Chọn lý do xin nghỉ") {
            Toast.makeText(this, "Vui lòng chọn lý do nghỉ học!", Toast.LENGTH_SHORT).show()
            return
        }

        if (fromDateLong == 0L || toDateLong == 0L) {
            Toast.makeText(this, "Vui lòng chọn ngày nghỉ!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạm thời chỉ giả lập gửi thành công
        Toast.makeText(this, "Đơn xin nghỉ đã được gửi đi. Đang chờ duyệt.", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showDatePickerDialog(textView: TextView, titlePrefix: String, onDateSelected: (Long) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val timestamp = selectedCalendar.timeInMillis / 1000
                onDateSelected(timestamp)
                textView.text = dateFormat.format(selectedCalendar.time)
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}
