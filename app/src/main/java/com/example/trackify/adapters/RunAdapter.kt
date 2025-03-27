package com.example.trackify.adapters

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trackify.R
import com.example.trackify.data.RunEntity
import com.example.trackify.util.TrackingUtility

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    val differCallBack = object : DiffUtil.ItemCallback<RunEntity>() {
        override fun areItemsTheSame(
            oldItem: RunEntity,
            newItem: RunEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: RunEntity,
            newItem: RunEntity
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)

    fun submitList(list: List<RunEntity>) = differ.submitList(list)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(
        holder: RunViewHolder,
        position: Int
    ) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(findViewById(R.id.ivRunImage))
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = java.text.SimpleDateFormat("dd.MM.yy", java.util.Locale.getDefault())
            findViewById<android.widget.TextView>(R.id.tvDate).text =
                dateFormat.format(calendar.time)
            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            findViewById<android.widget.TextView>(R.id.tvAvgSpeed).text = avgSpeed
            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            findViewById<android.widget.TextView>(R.id.tvDistance).text = distanceInKm
            findViewById<android.widget.TextView>(R.id.tvTime).text =
                TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
            val caloriesBurned = "${run.caloriesBurned}kcal"
            findViewById<android.widget.TextView>(R.id.tvCalories).text = caloriesBurned


        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}