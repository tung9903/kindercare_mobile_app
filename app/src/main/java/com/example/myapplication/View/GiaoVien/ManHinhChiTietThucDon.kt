package com.example.myapplication.View.GiaoVien

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.DataManager
import com.example.myapplication.R

class ManHinhChiTietThucDon : AppCompatActivity() {
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

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        displayMenu()
    }

    private fun displayMenu() {
        val container = findViewById<LinearLayout>(R.id.layoutContainer)
        val menu = DataManager.dailyMenu
        
        findViewById<TextView>(R.id.tvTitle).text = "Thực đơn ngày ${menu.date}"

        menu.meals.forEach { meal ->
            val mealCard = CardView(this).apply {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 32)
                layoutParams = params
                radius = 24f
                cardElevation = 4f
                setContentPadding(32, 32, 32, 32)
            }

            val mealLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            // Meal Header
            val headerText = TextView(this).apply {
                text = meal.MealType
                textSize = 18f
                setTextColor(Color.parseColor("#0F7055"))
                setTypeface(null, Typeface.BOLD)
            }
            mealLayout.addView(headerText)

            // Dishes
            meal.dishes.forEach { dish ->
                val dishView = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 24, 0, 0)
                }

                val dishName = TextView(this).apply {
                    text = "• ${dish.DishName}"
                    textSize = 15f
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                }
                dishView.addView(dishName)

                val details = TextView(this).apply {
                    text = "Chi tiết: ${dish.NutritionalDetails}"
                    textSize = 13f
                    setTextColor(Color.parseColor("#64748B"))
                }
                dishView.addView(details)

                val calories = TextView(this).apply {
                    text = "Năng lượng: ${dish.Calories ?: 0} kcal"
                    textSize = 13f
                    setTextColor(Color.parseColor("#059669"))
                    setPadding(0, 8, 0, 0)
                }
                dishView.addView(calories)

                mealLayout.addView(dishView)
            }

            mealCard.addView(mealLayout)
            container.addView(mealCard)
        }
    }
}
