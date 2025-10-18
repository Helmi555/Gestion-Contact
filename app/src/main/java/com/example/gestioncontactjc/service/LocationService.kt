package com.example.gestioncontactjc.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.gestioncontactjc.R
import com.example.gestioncontactjc.util.LocationUtils
import com.example.gestioncontactjc.util.MessageFormat
import com.example.gestioncontactjc.util.SmsUtils

class LocationService : Service() {

    companion object {
        const val EXTRA_SENDER = "extra_sender"
        private const val TAG = "LocationService"
        private const val CHANNEL_ID = "location_channel"
        private const val NOTIF_ID = 1001
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocSvc::WL")
        wakeLock?.acquire(60000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sender = intent?.getStringExtra(EXTRA_SENDER)
        if (sender.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No location permission")
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIF_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIF_ID, createNotification())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Foreground failed: ${e.message}")
        }

        LocationUtils.getFreshLocation(this) { loc ->
            if (loc != null) {
                val msg = MessageFormat.locationResponse(loc.latitude, loc.longitude)
                SmsUtils.sendSms(this, sender, msg)
                Log.d(TAG, "Sent location to $sender")
            } else {
                Log.e(TAG, "No location")
            }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let { if (it.isHeld) it.release() }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(CHANNEL_ID, "Location", NotificationManager.IMPORTANCE_LOW)
                nm.createNotificationChannel(ch)
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sharing location")
            .setContentText("Sending GPS...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}