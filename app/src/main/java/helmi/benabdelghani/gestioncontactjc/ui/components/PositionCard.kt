package helmi.benabdelghani.gestioncontactjc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helmi.benabdelghani.gestioncontactjc.data.model.Position
import helmi.benabdelghani.gestioncontactjc.ui.screens.AddContactFAB

@Composable
fun PositionCard(
    position: Position,
    onClick: () -> Unit = {},
    onDelete:() -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            // Accent bar (optional but stylish)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Location icon
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFFB3E5FC),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = position.pseudo.uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "📱 ${position.numero}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "📍 ${position.longitude.toShortString()}, ${position.latitude.toShortString()}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )


            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
                    .clip(CircleShape),

            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFD02E2A),
                    modifier = Modifier.size(26.dp)
                )
            }


        }

    }
}

// Helper extension to shorten coordinates
private fun Double.toShortString(): String {
    return if (this == 0.0) "0.0" else "%.4f".format(this)
}
