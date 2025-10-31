package helmi.benabdelghani.gestioncontactjc.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import helmi.benabdelghani.gestioncontactjc.MainActivity
import helmi.benabdelghani.gestioncontactjc.R
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingPoint
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class TrackingService : Service(), LocationListener {
    private var userId: Int = -1
    private var sessionId: Long = -1
    private var sessionStartTime: Long = 0L
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val sessionDao by lazy { db.trackingSessionDao() }
    private val pointDao by lazy { db.trackingPointDao() }
    private var lastLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private val svcScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var totalDistance = 0.0
    private var pointCount = 0

    companion object {
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "STOP_TRACKING"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Start periodic notification updates
        svcScope.launch {
            while (isActive) {
                delay(5000)
                updateNotification()
            }
        }

        Log.d("TRACKING", "Service created and foreground notification started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Log.d("TRACKING", "Stop action received")
            stopSelf()
            return START_NOT_STICKY
        }

        val userId = intent?.getIntExtra("userId", -1) ?: -1
        Log.d("TRACKING", "Received userId: $userId")

        if (userId == -1) {
            Log.e("TRACKING", "Invalid userId -> stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        this.userId = userId

        if (sessionId <= 0) {
            runBlocking {
                val now = System.currentTimeMillis()
                val id = sessionDao.insertSession(
                    TrackingSession(
                        userId = userId,
                        startTime = now,
                        totalDistance = 0.0
                    )
                )
                sessionId = id
                sessionStartTime = now
                Log.d("TRACKING", "New tracking session created with ID: $sessionId, startTime: $now")
            }
            startLocationUpdates()
        } else {
            Log.d("TRACKING", "Session already exists with ID: $sessionId")
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val elapsedText = if (sessionStartTime > 0) {
            val elapsedSec = ((System.currentTimeMillis() - sessionStartTime) / 1000).coerceAtLeast(1)
            val minutes = elapsedSec / 60
            val seconds = elapsedSec % 60
            val hours = minutes / 60
            val displayTime = when {
                hours > 0 -> "%02dh %02dm %02ds".format(hours, minutes % 60, seconds)
                minutes > 0 -> "%02dm %02ds".format(minutes, seconds)
                else -> "%02ds".format(seconds)
            }
            "Time: $displayTime | Dist: ${String.format("%.0f", totalDistance)}m"
        } else {
            "Dist: ${String.format("%.0f", totalDistance)}m"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText(elapsedText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.d("TRACKING", "GPS enabled: $gpsEnabled, Network enabled: $netEnabled")

            // Focus on GPS only for better accuracy
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000L,  // 2 seconds
                2f,     // 2 meters
                this
            )

            // Also use network but with lower priority
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000L,  // 5 seconds
                10f,    // 10 meters
                this
            )

            Log.d("TRACKING", "Location updates started - GPS focused")
        } catch (e: SecurityException) {
            Log.e("TRACKING", "Location permission missing: ${e.message}")
            stopSelf()
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("TRACKING", "New location: ${location.latitude}, ${location.longitude}, acc=${location.accuracy}m, provider=${location.provider}")

        if (sessionId <= 0L) {
            Log.e("TRACKING", "Invalid sessionId, skipping insert")
            return
        }

        svcScope.launch {
            try {
                val point = TrackingPoint(
                    sessionId = sessionId.toInt(),
                    lat = location.latitude,
                    lon = location.longitude,
                    timestamp = System.currentTimeMillis()
                )
                pointDao.insertPoint(point)
                pointCount++

                if (lastLocation != null) {
                    val distance = lastLocation!!.distanceTo(location)

                    // Simple distance calculation - accept any movement > 1 meter
                    if (distance >= 1.0) {
                        totalDistance += distance
                        Log.d("TRACKING", "Movement detected: +${"%.1f".format(distance)}m | Total=${"%.1f".format(totalDistance)}m")

                        // Update session in database - MORE ROBUST UPDATE
                        try {
                            // Get fresh session from database
                            val session = sessionDao.getSessionById(sessionId.toInt())
                            // Update with current totalDistance
                            val updatedSession = session.copy(totalDistance = totalDistance)
                            sessionDao.updateSession(updatedSession)
                            Log.d("TRACKING", "Database updated: ${totalDistance}m")
                        } catch (e: Exception) {
                            Log.e("TRACKING", "Error updating session distance", e)
                            // Try alternative update method
                            try {
                                sessionDao.updateSessionDistance(sessionId.toInt(), totalDistance)
                            } catch (e2: Exception) {
                                Log.e("TRACKING", "Alternative update also failed", e2)
                            }
                        }

                        updateNotification()
                    }
                } else {
                    Log.d("TRACKING", "First location received - setting as reference")
                }

                // Always update lastLocation to current location
                lastLocation = location

            } catch (e: Exception) {
                Log.e("TRACKING", "Error in onLocationChanged: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        Log.d("TRACKING", "Service onDestroy called")

        runBlocking {
            try {
                if (sessionId > 0L) {
                    val session = sessionDao.getSessionById(sessionId.toInt())
                    val finalDistance = totalDistance
                    val points = (finalDistance / 100).toInt().coerceAtLeast(0)

                    val updatedSession = session.copy(
                        endTime = System.currentTimeMillis(),
                        totalDistance = finalDistance,
                        points = points
                    )
                    sessionDao.updateSession(updatedSession)

                    if (points > 0) {
                        val userDao = db.userDao()
                        userDao.addPoints(userId, points)
                    }

                    Log.d("TRACKING", "Session ended. Distance: ${String.format("%.1f", finalDistance)}m, Points: $points")
                }
                else{

                }
            } catch (e: Exception) {
                Log.e("TRACKING", "Error closing session", e)
            }
        }

        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            Log.e("TRACKING", "Error removing location updates", e)
        }

        svcScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Shows location tracking status"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}