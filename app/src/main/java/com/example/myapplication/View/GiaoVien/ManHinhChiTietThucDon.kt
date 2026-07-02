package com.example.myapplication.View.GiaoVien

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DateHelper
import com.example.myapplication.R
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

class ManHinhChiTietThucDon : AppCompatActivity() {

    private lateinit var layoutContainer: LinearLayout
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chi_tiet_thuc_don)
        
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        layoutContainer = findViewById(R.id.layoutContainer)
        tvTitle = findViewById(R.id.tvTitle)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000
        
        tvTitle.text = "Thực đơn ngày " + DateHelper.formatLongToDate(today * 1000)

        // Lấy classId từ intent hoặc mặc định là 1
        val classId = intent.getIntExtra("CLASS_ID", 1)
        fetchClassMenu(classId, today)
    }

    private fun fetchClassMenu(classId: Int, date: Long) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null) ?: return

        val url = "https://web-test.kindercare.app/api/v1/teacher/classes/$classId/menu?date=$date"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        Thread {
            try {
                DataManager.okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val data = json.optJSONObject("data")
                        if (data != null) {
                            runOnUiThread {
                                displayMenu(data)
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun displayMenu(data: JSONObject) {
        layoutContainer.removeAllViews()
        val meals = data.optJSONArray("meals")
        val inflater = LayoutInflater.from(this)

        if (meals == null || meals.length() == 0) {
            val tvEmpty = TextView(this).apply {
                text = "Không có thông tin thực đơn cho ngày này"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 50, 0, 0)
            }
            layoutContainer.addView(tvEmpty)
            return
        }

        for (i in 0 until meals.length()) {
            val meal = meals.getJSONObject(i)
            
            // Header bữa ăn (Bữa trưa, Bữa xế...)
            val headerView = inflater.inflate(R.layout.item_child_detail_box, layoutContainer, false)
            headerView.findViewById<TextView>(R.id.tvBoxLabel).text = "BỮA ĂN"
            headerView.findViewById<TextView>(R.id.tvBoxValue).text = meal.optString("mealType")
            headerView.setBackgroundColor(Color.parseColor("#F1F5F9"))
            layoutContainer.addView(headerView)

            val dishes = meal.optJSONArray("dishes")
            if (dishes != null) {
                for (j in 0 until dishes.length()) {
                    val dish = dishes.getJSONObject(j)
                    val dishView = inflater.inflate(R.layout.item_dish, layoutContainer, false)
                    
                    dishView.findViewById<TextView>(R.id.tvDishName).text = dish.optString("dishName")
                    dishView.findViewById<TextView>(R.id.tvDishCalories).text = dish.optInt("calories").toString() + " Kcal"
                    dishView.findViewById<TextView>(R.id.tvDishNutrition).text = dish.optString("nutritionalDetails")
                    
                    layoutContainer.addView(dishView)
                }
            }
        }
    }
}
