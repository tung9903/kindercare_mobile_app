package com.example.myapplication.View.GiaoVien

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R
import okhttp3.Request
import org.json.JSONObject

class ManHinhChucNangHocPhi : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chuc_nang_hoc_phi)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val studentId = intent.getIntExtra("STUDENT_ID", -1)
        if (studentId != -1) {
            fetchStudentInfo(studentId)
        }
    }

    private fun fetchStudentInfo(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/teacher/students/$studentId"
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
                                findViewById<TextView>(R.id.tvHeaderTitle).text = "Học phí: ${data.optString("fullName")}"
                            }
                        }
                    }
                    return@use
                }
            } catch (e: Exception) {
                Log.e("TUITION_STUDENT", "Error: ${e.message}")
            }
        }.start()
    }
}
