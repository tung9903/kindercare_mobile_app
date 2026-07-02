package com.example.myapplication.View.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.Model.FeatureModel
import com.example.myapplication.Model.HomeItem

class PhuHuynhAdapter(
    private val items: List<HomeItem>,
    private val onFeatureClick: (FeatureModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_FEATURES = 1
        private const val TYPE_MOMENT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeItem.HeaderTitle -> TYPE_HEADER
            is HomeItem.FeaturesBlock -> TYPE_FEATURES
            is HomeItem.MomentItem -> TYPE_MOMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_home_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_FEATURES -> {
                val view = inflater.inflate(R.layout.item_features_container, parent, false)
                FeaturesBlockViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_moment_timeline_ph, parent, false)
                MomentViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeItem.HeaderTitle -> {
                val hHolder = holder as HeaderViewHolder
                hHolder.tvTitle.text = item.title
            }
            is HomeItem.FeaturesBlock -> {
                val fHolder = holder as FeaturesBlockViewHolder
                fHolder.rvFeaturesChild.layoutManager = GridLayoutManager(holder.itemView.context, 3)
                fHolder.rvFeaturesChild.adapter = FeatureAdapter(item.list, onFeatureClick)
            }
            is HomeItem.MomentItem -> {
                val mHolder = holder as MomentViewHolder
                val currentMoment = item.moment

                mHolder.tvDateTitle.text = currentMoment.dateText
                if (currentMoment.isToday) {
                    mHolder.tvDateTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_green_dot, 0, 0, 0)
                } else {
                    mHolder.tvDateTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    mHolder.tvDateTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
                }

                // Lấy danh sách ảnh từ cả URL (API) và Resource (Mock)
                val imageUrls = currentMoment.imageUrls
                val imageResIds = currentMoment.imageResIds
                
                val totalImages = imageUrls.size + imageResIds.size

                mHolder.imgBigLeft.visibility = View.VISIBLE
                mHolder.imgSmallTopRight.visibility = View.VISIBLE
                mHolder.layoutMoreImages.visibility = View.VISIBLE
                mHolder.viewOverlay.visibility = View.GONE
                mHolder.tvMoreCount.visibility = View.GONE

                fun loadImage(view: ImageView, pos: Int) {
                    if (pos < imageUrls.size) {
                        Glide.with(holder.itemView.context).load(imageUrls[pos]).placeholder(R.drawable.logo).into(view)
                    } else if (pos - imageUrls.size < imageResIds.size) {
                        view.setImageResource(imageResIds[pos - imageUrls.size])
                    }
                }

                when {
                    totalImages == 1 -> {
                        loadImage(mHolder.imgBigLeft, 0)
                        mHolder.imgSmallTopRight.visibility = View.GONE
                        mHolder.layoutMoreImages.visibility = View.GONE
                    }
                    totalImages == 2 -> {
                        loadImage(mHolder.imgBigLeft, 0)
                        loadImage(mHolder.imgSmallTopRight, 1)
                        val params = mHolder.imgSmallTopRight.layoutParams
                        params.height = mHolder.imgBigLeft.layoutParams.height
                        mHolder.imgSmallTopRight.layoutParams = params
                        mHolder.layoutMoreImages.visibility = View.GONE
                    }
                    totalImages >= 3 -> {
                        val params = mHolder.imgSmallTopRight.layoutParams
                        params.height = (73 * holder.itemView.resources.displayMetrics.density).toInt()
                        mHolder.imgSmallTopRight.layoutParams = params

                        loadImage(mHolder.imgBigLeft, 0)
                        loadImage(mHolder.imgSmallTopRight, 1)
                        loadImage(mHolder.imgSmallBottomRight, 2)

                        if (totalImages > 3) {
                            mHolder.viewOverlay.visibility = View.VISIBLE
                            mHolder.tvMoreCount.visibility = View.VISIBLE
                            mHolder.tvMoreCount.text = "+${totalImages - 3}"
                        }
                    }
                    else -> {
                        mHolder.imgBigLeft.visibility = View.GONE
                        mHolder.imgSmallTopRight.visibility = View.GONE
                        mHolder.layoutMoreImages.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvHomeHeaderTitle)
    }

    class FeaturesBlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rvFeaturesChild: RecyclerView = view.findViewById(R.id.rvFeaturesChild)
    }

    class MomentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateTitle: TextView = view.findViewById(R.id.tvDateTitle)
        val imgBigLeft: ImageView = view.findViewById(R.id.imgBigLeft)
        val imgSmallTopRight: ImageView = view.findViewById(R.id.imgSmallTopRight)
        val imgSmallBottomRight: ImageView = view.findViewById(R.id.imgSmallBottomRight)
        val layoutMoreImages: RelativeLayout = view.findViewById(R.id.layoutMoreImages)
        val viewOverlay: View = view.findViewById(R.id.viewOverlay)
        val tvMoreCount: TextView = view.findViewById(R.id.tvMoreCount)
    }
}
