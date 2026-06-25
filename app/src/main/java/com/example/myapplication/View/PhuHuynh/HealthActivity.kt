package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    private lateinit var rvVaccine: RecyclerView
    private lateinit var vaccineAdapter: VaccineAdapter
    private lateinit var rvHealthHistory: RecyclerView
    private lateinit var healthAdapter: HealthRecordAdapter
    private val healthRecords = mutableListOf<HealthRecord>()
    
    private lateinit var btnBack: ImageView
    private lateinit var tvHeightValue: TextView
    private lateinit var tvWeightValue: TextView
    private lateinit var tvBMIValue: TextView
    private lateinit var tvAllergy: TextView
    private lateinit var tvBloodType: TextView
    private lateinit var tvHealthTitle: TextView

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
        setupVaccineRecyclerView()
        setupHealthHistoryRecyclerView()

        // Lấy thông tin học sinh từ DataManager để hiển thị Dị ứng/Nhóm máu
        val student = DataManager.studentList.firstOrNull()
        student?.let {
            tvAllergy.text = "• Dị ứng: ${it.Allergies ?: "Không"}"
            tvBloodType.text = "• Nhóm máu: ${it.BloodType ?: "Chưa xác định"}"
        }

        val studentId = student?.StudentID ?: -1
        if (studentId != -1) {
            fetchHealthRecords(studentId)
        } else {
            // Dữ liệu mẫu nếu không tìm thấy học sinh
            healthRecords.add(HealthRecord(1, 1, "Học kỳ 1 - 2025", 110.5, 18.2, 14.9))
            updateTopIndicators(healthRecords[0])
            healthAdapter.notifyDataSetChanged()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rvVaccine = findViewById(R.id.rvVaccine)
        rvHealthHistory = findViewById(R.id.rvHealthHistory)
        
        tvAllergy = findViewById(R.id.tvAllergy)
        tvBloodType = findViewById(R.id.tvBloodType)
        tvHealthTitle = findViewById(R.id.tvHealthTitle)

        // Ánh xạ các hộp chỉ số (Indicator boxes)
        val layoutHeight = findViewById<View>(R.id.layoutHeight)
        val layoutWeight = findViewById<View>(R.id.layoutWeight)
        val layoutBMI = findViewById<View>(R.id.layoutBMI)

        layoutHeight.findViewById<TextView>(R.id.tvBoxLabel).text = "CHIỀU CAO"
        layoutWeight.findViewById<TextView>(R.id.tvBoxLabel).text = "CÂN NẶNG"
        layoutBMI.findViewById<TextView>(R.id.tvBoxLabel).text = "BMI"

        tvHeightValue = layoutHeight.findViewById(R.id.tvBoxValue)
        tvWeightValue = layoutWeight.findViewById(R.id.tvBoxValue)
        tvBMIValue = layoutBMI.findViewById(R.id.tvBoxValue)
    }

    private fun setupVaccineRecyclerView() {
        rvVaccine.layoutManager = LinearLayoutManager(this)
        val vaccineList = listOf(
            VaccineModel("Sởi – Quai bị – Rubella (MMR)", "Đã tiêm", true),
            VaccineModel("Cúm mùa (Influenza)", "Sắp tới hẹn", false)
        )
        vaccineAdapter = VaccineAdapter(vaccineList)
        rvVaccine.adapter = vaccineAdapter
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

        val client = DataManager.okHttpClient
        val url = "https://web-test.kindercare.app/api/v1/parent/children/$studentId/health-records"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    Log.d("HEALTH_API", "Kết quả trả về: $body")

                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val dataArray = jsonResponse.optJSONArray("data")
                        
                        val tempRecords = mutableListOf<HealthRecord>()
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                tempRecords.add(
                                    HealthRecord(
                                        RecordID = obj.optInt("recordId"),
                                        StudentID = obj.optInt("studentId"),
                                        TermPeriod = obj.optString("termPeriod", "N/A"),
                                        Height = obj.optDouble("height", 0.0),
                                        Weight = obj.optDouble("weight", 0.0),
                                        BMI = obj.optDouble("bmi", 0.0)
                                    )
                                )
                            }
                        }

                        runOnUiThread {
                            healthRecords.clear()
                            healthRecords.addAll(tempRecords)
                            healthAdapter.notifyDataSetChanged()

                            if (healthRecords.isNotEmpty()) {
                                updateTopIndicators(healthRecords[0]) // Hiển thị bản ghi mới nhất lên đầu
                            }
                        }
                    } else {
                        Log.e("HEALTH_API", "Lỗi HTTP: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("HEALTH_API", "Ngoại lệ: ${e.message}")
            }
        }.start()
    }

    private fun updateTopIndicators(record: HealthRecord) {
        tvHealthTitle.text = "Chỉ số thể trạng (${record.TermPeriod})"
        tvHeightValue.text = "${record.Height} cm"
        tvWeightValue.text = "${record.Weight} kg"
        tvBMIValue.text = "${record.BMI}"
    }
}
