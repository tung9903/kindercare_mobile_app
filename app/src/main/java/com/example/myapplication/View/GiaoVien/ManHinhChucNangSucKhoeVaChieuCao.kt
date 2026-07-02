package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import okhttp3.Request
import org.json.JSONObject

class ManHinhChucNangSucKhoeVaChieuCao : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_suc_khoe_va_chieu_cao)
        
        val root = findViewById<View>(R.id.layout_main_health)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        if (studentId != -1) {
            fetchHealthData(studentId)
        }
    }

    private fun fetchHealthData(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/teacher/students/$studentId/health"
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
                        val json = JSONObject(body)
                        val data = json.optJSONObject("data")
                        if (data != null) {
                            runOnUiThread {
                                updateUI(data)
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun updateUI(data: JSONObject) {
        findViewById<TextView>(R.id.tvCurrentHeight).text = String.format("%s cm", data.optString("height"))
        findViewById<TextView>(R.id.tvCurrentWeight).text = String.format("%s kg", data.optString("weight"))
        findViewById<TextView>(R.id.tvCurrentBMI).text = data.optString("bmi")
    }
}
