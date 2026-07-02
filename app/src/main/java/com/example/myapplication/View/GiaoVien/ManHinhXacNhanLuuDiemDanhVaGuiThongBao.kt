package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ManHinhXacNhanLuuDiemDanhVaGuiThongBao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_xac_nhan_luu_diem_danh_va_gui_thong_bao)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val present = intent.getIntExtra("PRESENT_COUNT", 0)
        val absent = intent.getIntExtra("ABSENT_COUNT", 0)
        val total = intent.getIntExtra("TOTAL_COUNT", 0)

        findViewById<TextView>(R.id.tvPresentConfirm).text = String.format("%02d", present)
        findViewById<TextView>(R.id.tvAbsentConfirm).text = String.format("%02d", absent)
        findViewById<TextView>(R.id.tvTotalConfirm).text = String.format("%02d", total)

        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnConfirm).setOnClickListener { sendAttendanceToServer() }
    }

    private fun sendAttendanceToServer() {
        val data = DataManager.currentAttendanceData ?: return
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val json = JSONObject().apply {
            put("classId", data.classId)
            put("date", data.date)
            
            val array = JSONArray()
            data.attendanceData.forEach { item ->
                val obj = JSONObject().apply {
                    put("studentId", item.studentId)
                    put("status", item.status)
                    put("checkInTime", item.checkInTime ?: JSONObject.NULL)
                    put("checkOutTime", item.checkOutTime ?: JSONObject.NULL)
                    put("pickedUpBy", item.pickedUpBy ?: JSONObject.NULL)
                }
                array.put(obj)
            }
            put("attendanceData", array)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/teacher/attendance/quick")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        btnConfirm.isEnabled = false
        btnConfirm.text = "Đang gửi..."

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val resBody = response.body?.string()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Điểm danh lớp đã được lưu thành công!", Toast.LENGTH_LONG).show()
                            val intent = android.content.Intent(this, ManHinhBangDieuKhien::class.java)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        } else {
                            btnConfirm.isEnabled = true
                            btnConfirm.text = "Xác nhận & Gửi thông báo"
                            Log.e("ATTENDANCE_POST", "Lỗi: $resBody")
                            Toast.makeText(this, "Lỗi khi lưu dữ liệu lên server", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { 
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Xác nhận & Gửi thông báo"
                    Toast.makeText(this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show() 
                }
            }
        }.start()
    }
}
