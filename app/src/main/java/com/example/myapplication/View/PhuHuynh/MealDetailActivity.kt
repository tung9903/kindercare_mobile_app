package com.example.myapplication.View.PhuHuynh

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Model.DataManager
import com.example.myapplication.Model.DailyMenu
import com.example.myapplication.Model.MenuItem
import com.example.myapplication.R
import com.example.myapplication.View.Adapter.DishAdapter
import com.example.myapplication.databinding.ActivityMealDetailBinding
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar

class MealDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealDetailBinding
    private val dishList = mutableListOf<MenuItem>()
    private lateinit var adapter: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMealDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupActionListeners()

        // Ưu tiên lấy bé đang chọn
        val selectedChild = DataManager.selectedChild
        if (selectedChild != null) {
            fetchDailyMenu(selectedChild.getInt("studentId"))
        } else {
            fetchChildAndLoadMenu()
        }
    }

    private fun fetchChildAndLoadMenu() {
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
                            fetchDailyMenu(realStudentId)
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun setupRecyclerView() {
        adapter = DishAdapter(dishList)
        binding.rvDishes.layoutManager = LinearLayoutManager(this)
        binding.rvDishes.adapter = adapter
    }

    private fun setupActionListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchDailyMenu(studentId: Int) {
        val pref = getSharedPreferences("KinderCarePref", MODE_PRIVATE)
        val token = pref.getString("token", null)
        if (token.isNullOrEmpty()) return

        val client = DataManager.okHttpClient
        val request = Request.Builder()
            .url("https://web-test.kindercare.app/api/v1/parent/children/$studentId/daily-menu")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val jsonResponse = JSONObject(body)
                        val data = jsonResponse.optJSONObject("data")
                        
                        if (data != null) {
                            val menuDate = data.optString("date")
                            val mealsArray = data.optJSONArray("meals")
                            
                            val tempDishes = mutableListOf<MenuItem>()
                            var totalCals = 0
                            
                            if (mealsArray != null) {
                                for (i in 0 until mealsArray.length()) {
                                    val mealObj = mealsArray.getJSONObject(i)
                                    val dishesArray = mealObj.optJSONArray("dishes")
                                    if (dishesArray != null) {
                                        for (j in 0 until dishesArray.length()) {
                                            val dishObj = dishesArray.getJSONObject(j)
                                            val dish = MenuItem(
                                                DishName = dishObj.optString("dishName"),
                                                NutritionalDetails = dishObj.optString("nutritionalDetails"),
                                                Calories = dishObj.optInt("calories")
                                            )
                                            tempDishes.add(dish)
                                            totalCals += dish.Calories ?: 0
                                        }
                                    }
                                }
                            }

                            runOnUiThread {
                                binding.tvHeaderTitle.text = "Thực đơn ngày $menuDate"
                                dishList.clear()
                                dishList.addAll(tempDishes)
                                adapter.notifyDataSetChanged()
                                binding.tvTotalCalories.text = "Tổng năng lượng: ${totalCals} Kcal"
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }
}
