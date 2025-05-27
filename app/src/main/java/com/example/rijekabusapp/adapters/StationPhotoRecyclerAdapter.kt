package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.StationPhotoItemViewBinding
import com.example.rijekabusapp.network.models.StationImage

class StationPhotoRecyclerAdapter(
    private val context: Context,
    private val imageList: ArrayList<StationImage>,
) : RecyclerView.Adapter<StationPhotoRecyclerAdapter.StationPhotoViewHolder>() {
    var editSwitch = false
    var deleteCallback: ((StationImage) -> Unit)? = null
    var updateSliderCallback: ((ArrayList<StationImage>) -> Unit)? = null
    var showErrorMessageCallback: ((String) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setImageList(newImageList: ArrayList<StationImage>) {
        imageList.clear()
        imageList.addAll(newImageList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearImageList() {
        imageList.clear()
        notifyDataSetChanged()
    }

    class StationPhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = StationPhotoItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): StationPhotoViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.station_photo_item_view, parent, false)
        return StationPhotoViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: StationPhotoViewHolder,
        position: Int,
    ) {
        val playerImage = imageList[position]

        holder.binding.tvImageTitle.text = playerImage.imageCaption
        holder.binding.ivPlayerImage.load(playerImage.imageUrl)

        if (editSwitch) {
            setViewMargin(holder, 64f)
            holder.binding.editContainer.visibility = View.VISIBLE
            holder.binding.btnDelete.setOnClickListener {
                try {
                    deleteCallback?.invoke(playerImage)
                    imageList.remove(playerImage)
                    updateSliderCallback?.invoke(imageList)
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    showErrorMessageCallback?.invoke(
                        context.getString(R.string.error_deleting_image),
                    )
                }
            }
        } else {
            setViewMargin(holder, 2f)
            holder.binding.editContainer.visibility = View.GONE
        }
    }

    private fun setViewMargin(
        holder: StationPhotoViewHolder,
        value: Float,
    ) {
        val px =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.resources.displayMetrics,
            )
        val param = holder.binding.photoItemContainer.layoutParams as ConstraintLayout.LayoutParams
        param.setMargins(0, 0, px.toInt(), 0)
        holder.binding.photoItemContainer.layoutParams = param
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
