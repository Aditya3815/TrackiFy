package com.example.trackify.util

import android.content.Context
import android.os.Build
import android.Manifest
import android.location.Location
import androidx.annotation.RequiresApi
import com.example.trackify.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import kotlin.math.round

object TrackingUtility{
    fun hasLocationPermissions(context: Context): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return EasyPermissions.hasPermissions(context, *permissions.toTypedArray())
    }

    fun calculatePolylineLength(polyline: Polyline): Float{
        var distance = 0f
        for(i in 0..polyline.size - 2){
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]
            val result = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
        return distance
    }

    fun calculateAvgSpeed(distance: Int, timeInMillis: Long): Float{
        return round((distance / 1000f) / (timeInMillis / 1000f / 60 / 60) * 10) / 10f
    }

    fun calculateCaloriesBurned(weight: Float, avgSpeed: Float, timeInMillis: Long): Int{
        // MET - Metabolic Equivalent of Task
        // MET * weight in kg * time in hours = calories burned
        return ((avgSpeed * 1000 / 60 / 60) * weight * timeInMillis / 1000 / 60 / 60).toInt()
    }

    fun getFormattedStopWatchTime(ms:Long, includeMillis:Boolean = false):String{
        var milliseconds = ms
        val hours = (milliseconds / 1000) / 3600 // 1 hour = 3600 seconds
        milliseconds -= hours * 3600 * 1000
        val minutes = (milliseconds / 1000) / 60
        milliseconds -= minutes * 60 * 1000
        val seconds = (milliseconds / 1000) % 60
        milliseconds -= seconds * 1000
        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }
}