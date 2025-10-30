package helmi.benabdelghani.gestioncontactjc

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.Contact
import helmi.benabdelghani.gestioncontactjc.data.model.Position
import helmi.benabdelghani.gestioncontactjc.repository.PositionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapActivity : ComponentActivity() {

    companion object {
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MODE = "extra_mode"               // "view" or "add"
        const val EXTRA_USERID = "extra_userid"           // needed in add mode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 36.8065) // Default Tunis
        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 10.1815)
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: "Unknown"
        val mode = intent.getStringExtra(EXTRA_MODE) ?: "view"
        val userId = intent.getIntExtra(EXTRA_USERID, -1)

        setContent {
            ModernMapScreen(
                latitude = latitude,
                longitude = longitude,
                sender = sender,
                mode = mode,
                userId = userId,
                onBackClick = { finish() },
                onSavedAndClose = { success ->
                    if (success) setResult(RESULT_OK)
                    finish()

                }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernMapScreen(
    latitude: Double,
    longitude: Double,
    sender: String,
    mode: String = "view",
    userId: Int = -1,
    onBackClick: () -> Unit,
    onSavedAndClose: (Boolean) -> Unit
) {
    val location = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showContactSelector by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var showContactSheet by remember { mutableStateOf(mode == "add") }

    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showInfo by remember { mutableStateOf(mode=="view") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(mode, userId) {
        if (mode == "add" && userId >= 0) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                db.contactDao().getContactsForUserSorted(userId).also {
                    contacts = it
                }
            }
            if (selectedContact == null) showContactSelector = true
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E88E5),
                                Color(0xFF1565C0)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(22.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (mode == "add") "Add Position" else "Location from $sender",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (mode == "add") {
                                selectedContact?.let { "${it.pseudo} • ${it.phoneNumber}" } ?: "Tap to select contact"
                            } else {
                                "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}"
                            },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }

                    if (mode == "add") {
                        IconButton(
                            onClick = { showContactSheet = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.PersonAdd, "Select", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    IconButton(
                        onClick = { showInfo = !showInfo },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Info, "Info", tint = Color.White)
                    }
                }
            }
        },
        floatingActionButton = {
            if (mode == "add") {
                FloatingActionButton(
                    onClick = {
                        val chosen = selectedLatLng
                        val contact = selectedContact
                        if (contact == null) {
                            Toast.makeText(context, "Select contact first", Toast.LENGTH_SHORT).show()
                            showContactSheet = true
                            return@FloatingActionButton
                        }
                        if (chosen == null) {
                            Toast.makeText(context, "Tap on map to select position", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }
                        coroutineScope.launch {
                            saving = true
                            val ok = withContext(Dispatchers.IO) {
                                PositionRepository.addPosition(
                                    pseudo = contact.pseudo,
                                    userId = userId,
                                    numero = contact.phoneNumber,
                                    longitude = chosen.longitude,
                                    latitude = chosen.latitude
                                )
                            }
                            saving = false
                            Toast.makeText(context, if (ok) "Saved" else "Save failed", Toast.LENGTH_SHORT).show()
                            onSavedAndClose(ok)
                        }
                    },
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp))
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            strokeWidth = 3.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Check, "Save", modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isBuildingEnabled = true,
                    isIndoorEnabled = true,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    rotationGesturesEnabled = true,
                    compassEnabled = true,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapClick = { latLng ->
                    if (mode == "add") {
                        selectedLatLng = latLng
                    }
                }
            ) {
                if (mode == "add" && selectedLatLng != null) {
                    Marker(
                        state = MarkerState(position = selectedLatLng!!),
                        title = "Selected position",
                        snippet = "Tap map to change"
                    )
                } else if (mode == "view") {
                    Marker(
                        state = MarkerState(position = location),
                        title = sender,
                        snippet = "Tap for details"
                    )
                }
            }

            if (showInfo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5).copy(alpha = 0.80f),
                                    Color(0xFF1565C0).copy(alpha = 0.95f),
                                )
                            )
                        )
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Location Details",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(12.dp)
                        ) {
                            Column {
                                if (mode == "add" && selectedLatLng != null) {
                                    Text(
                                        text = "📍 ${"%.6f".format(selectedLatLng!!.latitude)}, ${"%.6f".format(selectedLatLng!!.longitude)}",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else if (mode == "add") {
                                    Text(
                                        text = "👆 Tap on map to select position",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "📍 ${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (mode == "add") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.White.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedContact?.let { "👤 ${it.pseudo} • ${it.phoneNumber}" }
                                            ?: "Select contact to assign",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        cameraPositionState.position = CameraPosition.Builder(cameraPositionState.position)
                            .zoom(cameraPositionState.position.zoom + 1)
                            .build()
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E88E5),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Default.Add, "Zoom in", modifier = Modifier.size(24.dp))
                }

                FloatingActionButton(
                    onClick = {
                        cameraPositionState.position = CameraPosition.Builder(cameraPositionState.position)
                            .zoom(cameraPositionState.position.zoom - 1)
                            .build()
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E88E5),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Default.Remove, "Zoom out", modifier = Modifier.size(24.dp))
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = {
                        mapType = when (mapType) {
                            MapType.NORMAL -> MapType.SATELLITE
                            MapType.SATELLITE -> MapType.HYBRID
                            MapType.HYBRID -> MapType.TERRAIN
                            else -> MapType.NORMAL
                        }
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E88E5),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Default.Layers, "Map type", modifier = Modifier.size(26.dp))
                }

                FloatingActionButton(
                    onClick = {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 10f)
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E88E5),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Default.MyLocation, "Recenter", modifier = Modifier.size(26.dp))
                }

                FloatingActionButton(
                    onClick = {
                        if (mode == "add") showContactSheet = true
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E88E5),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Default.PersonSearch, "Contacts", modifier = Modifier.size(26.dp))
                }
            }

            ContactSelectSheet(
                visible = showContactSheet,
                userId = userId,
                onSelect = {
                    selectedContact = it
                    showContactSheet = false
                },
                onDismiss = {
                    showContactSheet = false
                }
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ContactSelectSheet(
    visible: Boolean,
    userId: Int,
    onSelect: (Contact) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val modalHeight = screenHeight * 0.65f
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }

    LaunchedEffect(userId) {
        val db = AppDatabase.getDatabase(context)
        contacts = db.contactDao().getContactsForUserSorted(userId)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(340)
        ),
        exit = fadeOut(tween(220)) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(240)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        onClick = onDismiss,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(modalHeight)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1976D2),
                                Color(0xFF1565C0)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Select Contact",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(contacts) { contact ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .clickable { onSelect(contact) }
                                    .padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.pseudo.firstOrNull()?.uppercase() ?: "?",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            contact.pseudo,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            contact.phoneNumber,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp
                                        )
                                    }

                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
