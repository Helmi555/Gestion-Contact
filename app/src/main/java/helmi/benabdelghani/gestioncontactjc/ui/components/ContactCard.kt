package helmi.benabdelghani.gestioncontactjc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helmi.benabdelghani.gestioncontactjc.data.model.Contact


@Composable
fun ContactCard(
    contact: helmi.benabdelghani.gestioncontactjc.data.model.Contact,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onGetLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )                )
            )
            .padding(12.dp)
    ) {
        // Top-right pin/star
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .clickable { onPin() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (contact.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (contact.isPinned) "Pinned" else "Not pinned",
                tint = if (contact.isPinned) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val palettes = listOf(
                listOf(Color(0xFF64B5F6), Color(0xFF42A5F5)),
                listOf(Color(0xFF81C784), Color(0xFF4CAF50)),
                listOf(Color(0xFFFFD54F), Color(0xFFFFC107)),
                listOf(Color(0xFFCE93D8), Color(0xFFAB47BC))
            )
            val idx = (contact.nom.hashCode() and 0x7FFFFFFF) % palettes.size
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = palettes[idx],
                            center = Offset(0.3f, 0.3f)
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.nom.firstOrNull()?.uppercase() ?: contact.pseudo.firstOrNull()?.uppercase() ?: "#",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Spacer between avatar and content
            Spacer(modifier = Modifier.width(12.dp))

            // Main content: name, pseudo/number and bottom centered buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top area: name + subtitle
                Column {
                    Text(
                        text = contact.nom,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.76f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contact.pseudo,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            text = contact.phoneNumber,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }

                // Bottom area: centered action buttons row
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(bottom = 6.dp)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallCircleButton(
                            icon = Icons.Default.Call,
                            tint = Color.White,
                            background = Color(0xFF16A34A),
                            onClick = onCall,
                            contentDescription = "Call"
                        )
                        SmallCircleButton(
                            icon = Icons.Default.Edit,
                            tint = Color.White,
                            background = Color(0xFF0EA5E9),
                            onClick = onEdit,
                            contentDescription = "Edit"
                        )
                        SmallCircleButton(
                            icon = Icons.Default.LocationOn,
                            tint = Color.White,
                            background = Color(0xFF06B6D4),
                            onClick = onGetLocation,
                            contentDescription = "Location"
                        )
                        SmallCircleButton(
                            icon = Icons.Default.Close,
                            tint = Color.White,
                            background = Color(0xFFEF4444),
                            onClick = onDelete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    background: Color,
    tint: Color = Color.White,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}