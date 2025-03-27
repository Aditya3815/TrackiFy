package com.example.trackify.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
    (tableName = "running_table")
data class RunEntity(

    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var distanceInMeters: Int = 0,
    var caloriesBurned: Int = 0,
    var avgSpeedInKMH: Float = 0f,
    var timeInMillis: Long = 0L
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
