package com.example.gestioncontactjc.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gestioncontactjc.MapActivity
import com.example.gestioncontactjc.R

object NotificationUtils {
    private const val CHANNEL_ID = "location_channel"
    private const val CHANNEL_NAME = "Location messages"
    private const val TAG = "NotificationUtils"

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Notifications for received shared locations"
        nm.createNotificationChannel(channel)
    }

    fun showLocationNotification(context: Context, sender: String?, lat: Double, lon: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Cannot show notification: POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        ensureChannel(context)

        val mapIntent = Intent(context, MapActivity::class.java).apply {
            putExtra(MapActivity.EXTRA_LATITUDE, lat)
            putExtra(MapActivity.EXTRA_LONGITUDE, lon)
            putExtra(MapActivity.EXTRA_SENDER, sender)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, (lat.hashCode() xor lon.hashCode()), mapIntent, piFlags)

        val title = "Location from ${sender ?: "contact"}"
        val text = "Tap to view in app"

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify((lat.hashCode() xor lon.hashCode()), notif)
            Log.d(TAG, "Notification shown for location: $lat,$lon from $sender")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification: ${e.message}", e)
        }
    }
}