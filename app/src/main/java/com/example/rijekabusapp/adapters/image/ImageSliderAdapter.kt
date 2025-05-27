package com.example.rijekabusapp.adapters.image

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import coil.load
import com.example.rijekabusapp.R
import com.example.rijekabusapp.network.models.StationImage

class ImageSliderAdapter(
    private val context: Context,
    private val imageList: ArrayList<StationImage>,
) : PagerAdapter() {
    fun setImageList(newImageList: ArrayList<StationImage>) {
        imageList.clear()
        imageList.addAll(newImageList)
        notifyDataSetChanged()
    }

    fun updateImageList(image: StationImage) {
        imageList.add(image)
        notifyDataSetChanged()
    }

    fun clearImageList(imagesToRemove: ArrayList<StationImage>) {
        imageList.removeAll(imagesToRemove)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any,
    ): Boolean {
        return view === `object`
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(
        container: ViewGroup,
        position: Int,
    ): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.station_image_slider_item, null)
        val ivImages = view.findViewById<ImageView>(R.id.ivImages)

        imageList[position].imageUrl.let {
            ivImages.load(it)
        }

        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any,
    ) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }
}
