package com.example.gestioncontactjc.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestioncontactjc.data.model.Contact


@Composable
fun ContactCard(
    contact: Contact,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin:()->Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
.padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val gradientPalettes = listOf(
                    listOf(Color(0xFF64B5F6), Color(0xFF42A5F5)),
                    listOf(Color(0xFF81C784), Color(0xFF4CAF50)),
                   // listOf(Color(0xFFFF8A65), Color(0xFFFF5722)),
                 //   listOf(Color(0xFFBA68C8), Color(0xFF9C27B0)),
                    listOf(Color(0xFFFFD54F), Color(0xFFFFC107))
                )

                val gradientIndex = (contact.nom.hashCode() and 0x7FFFFFFF) % gradientPalettes.size
                val gradient = gradientPalettes[gradientIndex]

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = gradient
                            )
                        ),
                    contentAlignment = Alignment.Center
                )  {
                    Text(
                        text = contact.nom.firstOrNull()?.uppercase() ?:contact.pseudo.firstOrNull()?.uppercase() ?: "#",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = contact.nom,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = contact.pseudo,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = contact.phoneNumber,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        // RIgth pannel ici

           Column(
               modifier = Modifier
               .fillMaxHeight(),
               horizontalAlignment = Alignment.End,
               verticalArrangement = Arrangement.SpaceBetween
           ) {
               Box(
                   modifier = Modifier
                       .size(34.dp)
                       .clip(CircleShape)

                       .clickable(onClick = {Log.d("CONTACTS","Pinned ${contact.isPinned}")
                       onPin()}),
                   contentAlignment = Alignment.TopStart,

               ) {
                   Icon(
                       imageVector = if (contact.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                       contentDescription = if (contact.isPinned) "Pinned" else "Not pinned",
                       tint = if (contact.isPinned) Color.Yellow else Color.White.copy(alpha = 0.6f),
                       modifier = Modifier.size(34.dp)
                   )
               }

               Row(
                   verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.spacedBy(6.dp)
               ) {
                   ActionIconButton(
                       icon = Icons.Default.Call,
                       onClick = onCall,
                       backgroundColor = Color(0xFF66BB6A),
                       contentDescription = "Call"
                   )
                   ActionIconButton(
                       icon = Icons.Default.Edit,
                       onClick = onEdit,
                       backgroundColor = Color(0xFF42A5F5),
                       contentDescription = "Edit"
                   )
                   ActionIconButton(
                       icon = Icons.Default.Close,
                       onClick = onDelete,
                       backgroundColor = Color(0xFFEF5350),
                       contentDescription = "Delete"
                   )
               }
           }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}