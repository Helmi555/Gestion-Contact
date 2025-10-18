package com.example.gestioncontactjc.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

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
                // Check permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Cannot show notification: POST_NOTIFICATIONS permission not granted")
                        return
                    }
                }

                ensureChannel(context)

                val label = sender ?: "Location"
                val geoUri = "geo:$lat,$lon?q=$lat,$lon($label)".toUri()

                val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    `package` = "com.google.android.apps.maps"
                }

                val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                var pendingIntent = PendingIntent.getActivity(context, (lat.hashCode() xor lon.hashCode()), mapsIntent, piFlags)

                try {
                    context.packageManager.resolveActivity(mapsIntent, 0) ?: run {
                        val generic = Intent(Intent.ACTION_VIEW, geoUri)
                        pendingIntent = PendingIntent.getActivity(context, (lat.hashCode() xor lon.hashCode()), generic, piFlags)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error resolving map intent: ${e.message}")
                }

                val title = "Location from ${sender ?: "contact"}"
                val text = "Open location in Maps"

                val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(com.example.gestioncontactjc.R.drawable.ic_launcher_foreground)
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