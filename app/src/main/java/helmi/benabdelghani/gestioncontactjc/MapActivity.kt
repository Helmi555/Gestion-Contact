package helmi.benabdelghani.gestioncontactjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import helmi.benabdelghani.gestioncontactjc.ui.theme.GestionContactJCTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove


class MapActivity : ComponentActivity()
{

    companion object {
                const val EXTRA_LATITUDE = "extra_latitude"
                const val EXTRA_LONGITUDE = "extra_longitude"
                const val EXTRA_SENDER = "extra_sender"
            }

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                val sender = intent.getStringExtra(EXTRA_SENDER) ?: "Unknown"

                setContent {
                    GestionContactJCTheme {
                        ModernMapScreen(
                            latitude = latitude,
                            longitude = longitude,
                            sender = sender,
                            onBackClick = { finish() }
                        )
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ModernMapScreen(
            latitude: Double,
            longitude: Double,
            sender: String,
            onBackClick: () -> Unit
        ) {
            val location = LatLng(latitude, longitude)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(location, 15f)
            }

            var mapType by remember { mutableStateOf(MapType.NORMAL) }
            var showInfo by remember { mutableStateOf(true) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Location from $sender", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Lat: ${"%.6f".format(latitude)}, Lng: ${"%.6f".format(longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showInfo = !showInfo }) {
                                Icon(
                                    if (showInfo) Icons.Default.Info else Icons.Default.Info,
                                    "Toggle info"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Map
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
                        )
                    ) {
                        Marker(
                            state = MarkerState(position = location),
                            title = sender,
                            snippet = "Tap for details"
                        )
                    }

                    if (showInfo) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .fillMaxWidth(0.9f),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📍 Location Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "From: $sender",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Coordinates: ${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                cameraPositionState.position = CameraPosition.Builder(cameraPositionState.position)
                                    .zoom(cameraPositionState.position.zoom + 1)
                                    .build()
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, "Zoom in")
                        }

                        FloatingActionButton(
                            onClick = {
                                cameraPositionState.position = CameraPosition.Builder(cameraPositionState.position)
                                    .zoom(cameraPositionState.position.zoom - 1)
                                    .build()
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(48.dp)
                        ) {
                        Icon(Icons.Default.Remove, "Zoom out")                        }
                    }

                    //bottom controls
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Default.Layers, "Change map type")
                        }

                        FloatingActionButton(
                            onClick = {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(Icons.Default.MyLocation, "Recenter")
                        }

                        FloatingActionButton(
                            onClick = {
                                // a faire lunch nav ou bien share position ?
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(Icons.Default.Navigation, "Navigate")
                        }
                    }
                }
            }
        }