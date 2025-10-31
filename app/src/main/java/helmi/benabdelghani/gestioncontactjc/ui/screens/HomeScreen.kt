package helmi.benabdelghani.gestioncontactjc.ui.screens

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import helmi.benabdelghani.gestioncontactjc.R
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.Contact
import helmi.benabdelghani.gestioncontactjc.service.TrackingService
import helmi.benabdelghani.gestioncontactjc.ui.components.TrackingRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingSession
import helmi.benabdelghani.gestioncontactjc.data.model.User
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    userId: Int,
    navController: NavController? = null,
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Loading...") }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()
    var isTracking by remember { mutableStateOf(false) }
    var activeSessionStartTime by remember { mutableStateOf<Long?>(null) }  // ADD THIS
    var showCongratsDialog by remember { mutableStateOf(false) }
    var completedSessionData by remember { mutableStateOf<Triple<Double, Int,String>?>(null) }
    var activeSessionDistance by remember { mutableStateOf(0.0) }
    var refreshCounter by remember { mutableStateOf(0) }

    var totalContacts by remember {
        mutableStateOf(0)
    }
    var recentAdded by remember {
        mutableStateOf(0)
    }
    var totalPinned by remember {
        mutableStateOf(0)
    }
    var mostCalled by remember {
        mutableStateOf("")
    }
    var recentContacts by remember { mutableStateOf(emptyList<Contact>()) }


    var pendingCallNumber by remember { mutableStateOf<String?>(null) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pendingCallNumber?.let { number ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            pendingCallNumber = null
        }
    }
    // ADD THIS: Real-time updates when tracking is active
    LaunchedEffect(isTracking, refreshCounter) {
        if (isTracking) {
            while (isTracking) {
                delay(3000) // Update every 3 seconds when tracking
                refreshCounter++ // Force recomposition

                // Also update the distance from database
                scope.launch(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val sessions = db.trackingSessionDao().getAllSessions()
                        val activeSession = sessions.firstOrNull { it.endTime == null && it.userId == userId }

                        withContext(Dispatchers.Main) {
                            if (activeSession != null) {
                                activeSessionDistance = activeSession.totalDistance
                                Log.d("HOME", "Real-time update - Distance: ${activeSession.totalDistance}m")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HOME", "Error in real-time update", e)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        val serviceRunning = runningServices.any {
            it.service.className == TrackingService::class.java.name
        }

        if (serviceRunning) {
            scope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val sessions = db.trackingSessionDao().getAllSessions()
                    val activeSession = sessions.firstOrNull { it.endTime == null && it.userId == userId }

                    withContext(Dispatchers.Main) {
                        if (activeSession != null) {
                            isTracking = true
                            activeSessionStartTime = activeSession.startTime
                            activeSessionDistance = activeSession.totalDistance
                            Log.d("HOME", "Active session found: ID=${activeSession.id}, distance=${activeSession.totalDistance}")
                        } else {
                            isTracking = false
                            activeSessionStartTime = null
                            activeSessionDistance = 0.0
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HOME", "Error checking active session", e)
                }
            }
        } else {
            isTracking = false
            activeSessionStartTime = null
            activeSessionDistance = 0.0
        }

        Log.d("HOME", "Service running check: $isTracking")
    }
    LaunchedEffect(userId) {
        Log.d("HOME", "Looking for user with ID: $userId")
        try {
            val db = AppDatabase.getDatabase(context)
            val user = db.userDao().getUserById(userId)
            Log.d("HOME", "Fetched user: $user")
            if(user!=null) currentUser=user
            userName = user?.name ?: "User not found"
            totalContacts=db.contactDao().countContactsForUser(userId)
            val sevenDaysAgo = System.currentTimeMillis() -  24*60 * 60 * 1000L
            recentAdded = db.contactDao().getRecentContactedCount(userId, sevenDaysAgo)
            totalPinned=db.contactDao().getTotalPinnedContacts(userId)
            val name = db.contactDao().getMostContactedName(userId)
            mostCalled = name ?: "None"
            recentContacts=db.contactDao().getRecentContacts(userId)
        } catch (e: Exception) {
            Log.e("HOME", "Error fetching user", e)
            userName = "Error loading name"
        }
    }
    if (showCongratsDialog && completedSessionData != null) {
        CongratsDialog(
            distance = completedSessionData!!.first,
            points = completedSessionData!!.second,
            duration= completedSessionData!!.third,
            onDismiss = {
                showCongratsDialog = false
                completedSessionData = null
                scope.launch(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val user = db.userDao().getUserById(userId)
                        withContext(Dispatchers.Main) {
                            currentUser = user
                        }
                    } catch (e: Exception) {
                        Log.e("HOME", "Error refreshing user data", e)
                    }
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gestion_contact_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(40.dp)
                )
                Row {
                    Text(
                        text = "Contact Manager ",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pro",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF76A2EE)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .background(Color(0xFF76A2EE), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Points",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text =currentUser?.earnedPoints.toString() + " pts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }


    Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {
            // Profile Section


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.3f),
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            )
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF64B5F6), Color(0xFF42A5F5))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }


                    Text(
                        text = "Welcome back!",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }


            // Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    "Total Contacts",
                    totalContacts.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatsCard("Recent Added", recentAdded.toString(), modifier = Modifier.weight(1f))
                StatsCard("Total Pinned", totalPinned.toString(), modifier = Modifier.weight(1f))
                StatsCard("Most Called", mostCalled, modifier = Modifier.weight(1f))
            }


            TrackingRow(
                isTracking = isTracking,
                sessionStartTime = activeSessionStartTime,
                totalDistance =activeSessionDistance,
                onStartTracking = {
                    val intent = Intent(context.applicationContext, TrackingService::class.java).apply {
                        putExtra("userId", userId)
                    }
                    context.applicationContext.startForegroundService(intent)

                    scope.launch(Dispatchers.IO) {
                        var attempt = 0
                        var activeSession: TrackingSession? = null
                        while (attempt < 10 && activeSession == null) {
                            delay(100) // 100ms per attempt → max 1 second
                            try {
                                val db = AppDatabase.getDatabase(context)
                                val sessions = db.trackingSessionDao().getAllSessions()
                                activeSession = sessions.firstOrNull { it.endTime == null && it.userId == userId }
                            } catch (e: Exception) {
                                Log.e("HOME", "Error fetching session", e)
                                break
                            }
                            attempt++
                        }

                        withContext(Dispatchers.Main) {
                            if (activeSession?.startTime != null) {
                                activeSessionStartTime = activeSession.startTime
                                activeSessionDistance = activeSession.totalDistance  // 👈 ADD THIS LINE
                                isTracking = true
                            } else {
                                // Fallback: stop tracking if session failed to start
                                isTracking = false
                                activeSessionStartTime = null
                                activeSessionDistance = 0.0  // 👈 ADD THIS LINE
                                context.applicationContext.stopService(
                                    Intent(context.applicationContext, TrackingService::class.java)
                                )
                                Log.e("HOME", "Failed to retrieve active session start time")
                            }
                        }
                    }
                },
                onStopTracking = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val sessions = db.trackingSessionDao().getAllSessions()
                            val activeSession = sessions.firstOrNull { it.endTime == null && it.userId == userId }

                            withContext(Dispatchers.Main) {
                                context.applicationContext.stopService(
                                    Intent(context.applicationContext, TrackingService::class.java)
                                )
                            }

                            delay(300)

                            val finalSession = activeSession?.let {
                                db.trackingSessionDao().getSessionById(it.id)
                            }

                            withContext(Dispatchers.Main) {
                                isTracking = false
                                activeSessionStartTime = null

                                if (finalSession != null) {
                                    val totalSeconds = ((finalSession.endTime ?: 0L) - (finalSession.startTime ?: 0L)) / 1000
                                    val minutes = totalSeconds / 60
                                    val seconds = totalSeconds % 60
                                    val formatted = "$minutes min $seconds s"

                                    completedSessionData = Triple(finalSession.totalDistance, finalSession.points, formatted)
                                    showCongratsDialog = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("HOME", "Error stopping tracking", e)
                            withContext(Dispatchers.Main) {
                                isTracking = false
                                activeSessionStartTime = null
                            }
                        }
                    }
                }
            )



            // Action Buttons
            if (recentContacts.isNotEmpty()) {
                Text(
                    text = "Recent Contacts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically

                )
                {
                    items(recentContacts) { contact ->
                        RecentContactItem(
                            contact = contact,
                            onCallClick = {
                                scope.launch {
                                    val db = AppDatabase.getDatabase(context)
                                    val updatedContact =
                                        contact.copy(callCount = contact.callCount + 1)
                                    db.contactDao().update(updatedContact)
                                    val name = db.contactDao().getMostContactedName(userId)
                                    mostCalled = name ?: "None"
                                    recentContacts =
                                        recentContacts.map { if (it.id == contact.id) updatedContact else it }


                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CALL_PHONE
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pendingCallNumber = contact.phoneNumber
                                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                    } else {
                                        val intent = Intent(
                                            Intent.ACTION_CALL,
                                            Uri.parse("tel:${contact.phoneNumber}")
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .padding(8.dp)
                                .clickable { navController?.navigate("viewContacts/$userId") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "View All",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    "View All",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable {
                        (context as? ComponentActivity)?.finishAffinity()
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Quit App",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Footer houni
            Text(
                text = "Contact Manager Pro",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }
    }

}

@Composable
fun StatsCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        }
    }
}

@Composable
fun RecentContactItem(contact: Contact, onCallClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF64B5F6), Color(0xFF42A5F5))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.nom.first().uppercase(),
                color  = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp

            )
        }

        // Trimmed name
        Text(
            text = contact.nom.take(8),
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onCallClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = Color.Green,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                fontWeight = FontWeight.SemiBold,
                color=Color(0xFF010111)
            )
        }
    }
}


@Composable
fun CongratsDialog(
    distance: Double,
    points: Int,
    duration:String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()

                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 64.sp, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        "Congratulations!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        "Tracking Session Completed",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp, top = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format("%.0fm", distance),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text("Distance", fontSize = 14.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                duration,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text("Duration", fontSize = 14.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$points",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF001E)
                            )
                            Text("Points", fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Awesome!", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
