package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.databinding.RouteBusItemViewBinding
import com.example.rijekabusapp.databinding.RouteWalkItemViewBinding
import com.example.rijekabusapp.network.models.Step

class RouteRecyclerAdapter(
    private val ctx: Context,
    private val directionSteps: ArrayList<Step>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @SuppressLint("NotifyDataSetChanged")
    fun setStepList(newList: ArrayList<Step>) {
        directionSteps.clear()
        directionSteps.addAll(newList)
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_WALKING = 1
        private const val VIEW_TYPE_BUS = 2
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_WALKING -> {
                val binding = RouteWalkItemViewBinding.inflate(inflater, parent, false)
                WalkingViewHolder(binding)
            }
            VIEW_TYPE_BUS -> {
                val binding = RouteBusItemViewBinding.inflate(inflater, parent, false)
                BusViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val directionStep = directionSteps[position]
        when (holder) {
            is WalkingViewHolder -> holder.bind(directionStep)
            is BusViewHolder -> holder.bind(directionStep)
            else -> throw IllegalArgumentException("Invalid view holder")
        }
    }

    override fun getItemCount(): Int = directionSteps.size

    override fun getItemViewType(position: Int): Int {
        val directionStep = directionSteps[position]
        return when (directionStep.travelMode) {
            "WALKING" -> VIEW_TYPE_WALKING
            "TRANSIT" -> VIEW_TYPE_BUS
            else -> throw IllegalArgumentException("Invalid travel mode type")
        }
    }

    inner class WalkingViewHolder(private val binding: RouteWalkItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(directionStep: Step) {
            binding.distance.text = directionStep.distance.text
            binding.duration.text = directionStep.duration.text
        }
    }

    inner class BusViewHolder(private val binding: RouteBusItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(directionStep: Step) {
            binding.busTitle.text = directionStep.transitDetails?.line?.name ?: ""
            binding.lineNumber.text = directionStep.transitDetails?.line?.shortName ?: "0"
            binding.depStationName.text = directionStep.transitDetails?.departureStop?.name ?: ""
            binding.tvArriveStation.text = directionStep.transitDetails?.arrivalStop?.name ?: ""
            binding.depTime.text = directionStep.transitDetails?.departureTime?.text ?: ""
            binding.tvArriveTime.text = directionStep.transitDetails?.arrivalTime?.text ?: ""
            binding.noStops.text = "Ride " +
                directionStep.transitDetails?.stops.toString() + " stops"
        }
    }
}
