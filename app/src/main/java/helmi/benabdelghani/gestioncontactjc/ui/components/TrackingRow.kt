package helmi.benabdelghani.gestioncontactjc.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow


@SuppressLint("DefaultLocale")
@Composable
fun TrackingRow(
    isTracking: Boolean,
    sessionStartTime: Long?,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    totalDistance: Double = 0.0,
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // ADD THIS: Log when distance updates
    LaunchedEffect(totalDistance) {
        if (isTracking) {
            Log.d("TRACKING_ROW", "Distance updated: ${totalDistance}m")
        }
    }

    LaunchedEffect(isTracking, sessionStartTime) {
        if (isTracking && sessionStartTime != null) {
            while (true) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }
    val rawElapsedSeconds = if (isTracking && sessionStartTime != null) {
        ((currentTime - sessionStartTime) / 1000).toInt()
    } else {
        0
    }
    val elapsedSeconds = if (rawElapsedSeconds < 1) 1 else rawElapsedSeconds

    // Gradient animation
    val infiniteTransition = rememberInfiniteTransition()
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)),
        start = Offset(0f, 0f),
        end = Offset(gradientShift, gradientShift)
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .background(brush = gradientBrush, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = if (isTracking) "Tracking in progress..." else "Try the new Tracking Feature!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            if (isTracking) {
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                val hours = minutes / 60
                val display = when {
                    hours > 0 -> String.format("%02dh %02dm %02ds", hours, minutes % 60, seconds)
                    minutes > 0 -> String.format("%02dm %02ds", minutes, seconds)
                    else -> String.format("%02ds", seconds)
                }
                Column  {
                    Text(
                        text = "Elapsed: $display",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Distance: ${String.format("%.0f", totalDistance)}m",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { if (isTracking) onStopTracking() else onStartTracking() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTracking) Color(0xFFFF5252) else Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(if (isTracking) "Stop" else "Start", color = Color.White, fontSize = 14.sp)
        }
    }
}