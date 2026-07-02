package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.HealthRecord
import com.example.myapplication.Model.VaccineModel
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.HealthRecordAdapter
import com.example.myapplication.View.Adapter.VaccineAdapter
import okhttp3.Request
import org.json.JSONObject

class HealthActivity : AppCompatActivity() {

    private lateinit var rvHealthHistory: RecyclerView
    private lateinit var healthAdapter: HealthRecordAdapter
    private val healthRecords = mutableListOf<HealthRecord>()
    
    private lateinit var btnBack: ImageView
    private lateinit var tvAllergy: TextView
    private lateinit var tvBloodType: TextView
    private lateinit var tvCurrentHeight: TextView
    private lateinit var tvCurrentWeight: TextView
    private lateinit var tvCurrentBMI: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupHealthHistoryRecyclerView()

        // Sử dụng bé đang chọn từ DataManager
        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            tvAllergy.text = selectedChild.optString("allergies", "Không có thông tin")
            // tvBloodType không còn trong layout mới, nếu cần hiển thị có thể bỏ comment ở layout
            fetchHealthRecords(selectedChild.getInt("studentId"))
        } else {
            fetchChildAndLoadHealth()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchChildAndLoadHealth() {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children")
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
                        if (dataArray != null && dataArray.length() > 0) {
                            val child = dataArray.getJSONObject(0)
                            val realStudentId = child.getInt("studentId")
                            
                            runOnUiThread {
                                tvAllergy.text = child.optString("allergies", "Không có thông tin")
                            }
                            
                            fetchHealthRecords(realStudentId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HEALTH_API", "Lỗi lấy ID bé: ${e.message}")
            }
        }.start()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rvHealthHistory = findViewById(R.id.rvHealthHistory)
        tvAllergy = findViewById(R.id.tvAllergy)
        tvCurrentHeight = findViewById(R.id.tvCurrentHeight)
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight)
        tvCurrentBMI = findViewById(R.id.tvCurrentBMI)
    }

    private fun setupHealthHistoryRecyclerView() {
        rvHealthHistory.layoutManager = LinearLayoutManager(this)
        healthAdapter = HealthRecordAdapter(healthRecords)
        rvHealthHistory.adapter = healthAdapter
    }

    private fun fetchHealthRecords(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val url = "https://web-test.kindercare.app/api/v1/parent/children/$studentId/health-records"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("HEALTH_API", "Dữ liệu trả về: $body")

                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempRecords = mutableListOf<HealthRecord>()
                        dataArray?.let {
                            for (i in 0 until it.length()) {
                                val obj = it.getJSONObject(i)
                                tempRecords.add(
                                    HealthRecord(
                                        recordId = obj.optInt("recordId"),
                                        studentId = obj.optInt("studentId"),
                                        termPeriod = obj.optString("termPeriod", "N/A"),
                                        height = obj.optDouble("height", 0.0),
                                        weight = obj.optDouble("weight", 0.0),
                                        bmi = obj.optDouble("bmi", 0.0)
                                    )
                                )
                            }
                        }

                        runOnUiThread {
                            healthRecords.clear()
                            healthRecords.addAll(tempRecords)
                            healthAdapter.notifyDataSetChanged()
                            
                            // Cập nhật chỉ số hiện tại từ bản ghi mới nhất
                            if (tempRecords.isNotEmpty()) {
                                val latest = tempRecords.last()
                                tvCurrentHeight.text = "${latest.height} cm"
                                tvCurrentWeight.text = "${latest.weight} kg"
                                tvCurrentBMI.text = String.format("%.1f", latest.bmi)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HEALTH_API", "Ngoại lệ: ${e.message}")
            }
        }.start()
    }
}
