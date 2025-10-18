package com.example.gestioncontactjc.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationUtils {
    private const val TAG = "LocationUtils"

    @SuppressLint("MissingPermission")
    fun getFreshLocation(context: Context, onResult: (Location?) -> Unit) {
        val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        var handled = false

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (!handled) {
                handled = true
                Log.e(TAG, "Timeout - using last location")
                client.lastLocation.addOnSuccessListener { onResult(it) }
            }
        }, 15000)

        Log.d(TAG, "Requesting fresh location")
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (!handled) {
                    handled = true
                    handler.removeCallbacksAndMessages(null)
                    Log.d(TAG, "Got location: $location")
                    onResult(location)
                }
            }
            .addOnFailureListener { ex ->
                if (!handled) {
                    handled = true
                    handler.removeCallbacksAndMessages(null)
                    Log.e(TAG, "Failed: ${ex.message}")
                    client.lastLocation.addOnSuccessListener { onResult(it) }
                }
            }
    }
}