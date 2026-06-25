package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Model.FeatureModel

class FeatureAdapter(
    private val features: List<FeatureModel>,
    private val onFeatureClick: (FeatureModel) -> Unit
) : RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature_single, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val feature = features[position]

        holder.tvFeatureName.text = feature.title
        holder.imgFeatureIcon.setImageResource(feature.iconRes ?: R.drawable.ic_health)

        holder.itemView.setOnClickListener {
            onFeatureClick(feature)
        }
    }

    override fun getItemCount(): Int = features.size

    class FeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFeatureIcon: ImageView = view.findViewById(R.id.imgFeatureIcon)
        val tvFeatureName: TextView = view.findViewById(R.id.tvFeatureName)
    }
}
