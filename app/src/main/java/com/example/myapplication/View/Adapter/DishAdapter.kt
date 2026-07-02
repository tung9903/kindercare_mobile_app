package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.MenuItem
import com.example.myapplication.R

class DishAdapter(private val dishList: List<MenuItem>) :
    RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDishName: TextView = view.findViewById(R.id.tvDishName)
        val tvDishCalories: TextView = view.findViewById(R.id.tvDishCalories)
        val tvDishNutrition: TextView = view.findViewById(R.id.tvDishNutrition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]
        holder.tvDishName.text = dish.DishName
        holder.tvDishCalories.text = "${dish.Calories ?: 0} Kcal"
        holder.tvDishNutrition.text = dish.NutritionalDetails ?: "Không có chi tiết dinh dưỡng"
    }

    override fun getItemCount() = dishList.size
}
