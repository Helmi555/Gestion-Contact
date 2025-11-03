package helmi.benabdelghani.gestioncontactjc.ui.map

import android.widget.Toast
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingPoint
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SessionMapActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_USER_ID = "extra_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
        val userId = intent.getIntExtra(EXTRA_USER_ID, -1)

        if (sessionId == -1) {
            Toast.makeText(this, "Invalid session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            SessionMapScreen(sessionId, userId) { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionMapScreen(sessionId: Int, userId: Int, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val cameraState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()
    var session by remember { mutableStateOf<TrackingSession?>(null) }
    var points by remember { mutableStateOf<List<TrackingPoint>>(emptyList()) }
    var showInfo by remember { mutableStateOf(false) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    LaunchedEffect(sessionId) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            session = db.trackingSessionDao().getSessionById(sessionId)
            points = db.trackingPointDao().getPointsForSession(sessionId)
        }
        if (points.isNotEmpty()) {
            cameraState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(points.first().lat, points.first().lon),
                    16f
                )
            )
        }
    }

    val start = points.firstOrNull()?.let { LatLng(it.lat, it.lon) }
    val end = points.lastOrNull()?.let { LatLng(it.lat, it.lon) }
    val polylinePoints = remember(points) { points.map { LatLng(it.lat, it.lon) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Session ${session?.id ?: "..."} • User ${userId}",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                ),
                actions = {
                    IconButton(onClick = { showInfo = !showInfo }) {
                        Icon(Icons.Default.Info, null, tint = Color.White)
                    }
                    IconButton(onClick = {
                        mapType = when (mapType) {
                            MapType.NORMAL -> MapType.SATELLITE
                            MapType.SATELLITE -> MapType.TERRAIN
                            MapType.TERRAIN -> MapType.HYBRID
                            else -> MapType.NORMAL
                        }
                    }) {
                        Icon(Icons.Default.Map, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(mapType = mapType),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                if (polylinePoints.isNotEmpty())
                    Polyline(points = polylinePoints, color = Color(0xFF2196F3), width = 12f)
                start?.let { Marker(state = MarkerState(it), title = "Start") }
                end?.let { Marker(state = MarkerState(it), title = "End") }
            }

            if (showInfo && session != null) {
                SessionInfoCard(session!!, points)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallActionButton(Icons.Default.ZoomIn) {
                    scope.launch {
                        cameraState.animate(CameraUpdateFactory.zoomIn())
                    }
                }
                SmallActionButton(Icons.Default.ZoomOut) {
                    scope.launch {
                        cameraState.animate(CameraUpdateFactory.zoomOut())
                    }
                }
                SmallActionButton(Icons.Default.MyLocation) {
                    start?.let {
                        scope.launch {
                            cameraState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionInfoCard(session: TrackingSession, points: List<TrackingPoint>) {
    val fmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val startTime = fmt.format(Date(session.startTime))
    val endTime = session.endTime?.let { fmt.format(Date(it)) } ?: "Ongoing"
    val duration = session.endTime?.let { (it - session.startTime) / 1000 } ?: 0

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xCC1565C0))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start: $startTime",
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = "End: $endTime",
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = "Points: ${points.size}",
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = "Distance: ${"%.2f".format(session.totalDistance)} m",
                color = Color.White,
                fontSize = 13.sp
            )
            if (duration > 0) {
                Text(
                    text = "Duration: ${duration}s",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }

}

@Composable
fun SmallActionButton(icon: ImageVector, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        containerColor = Color.White,
        contentColor = Color(0xFF1565C0),
        elevation = FloatingActionButtonDefaults.elevation(6.dp)
    ) {
        Icon(icon, null)
    }
}
