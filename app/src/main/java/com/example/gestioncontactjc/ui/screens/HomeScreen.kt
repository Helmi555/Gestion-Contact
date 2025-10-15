package com.example.gestioncontactjc.ui.screens

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.navigation.NavController
import com.example.gestioncontactjc.R
import com.example.gestioncontactjc.data.database.AppDatabase
import com.example.gestioncontactjc.ui.components.PremiumActionButton

@Composable
fun HomeScreen(
    userId: Int,
    navController: NavController? = null,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Loading...") }
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

    LaunchedEffect(userId) {
        Log.d("HOME", "Looking for user with ID: $userId")
        try {
            val db = AppDatabase.getDatabase(context)
            val user = db.userDao().getUserById(userId)
            Log.d("HOME", "Fetched user: $user")
            userName = user?.name ?: "User not found"
            totalContacts=db.contactDao().countContactsForUser(userId)
            val sevenDaysAgo = System.currentTimeMillis() -  24*60 * 60 * 1000L
            recentAdded = db.contactDao().getRecentContactedCount(userId, sevenDaysAgo)
            totalPinned=db.contactDao().getTotalPinnedContacts(userId)
            val name = db.contactDao().getMostContactedName(userId)
            mostCalled = name ?: "None"
        } catch (e: Exception) {
            Log.e("HOME", "Error fetching user", e)
            userName = "Error loading name"
        }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp, start = 4.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically

                )
            {
                Image(
                    painter = painterResource(id = R.drawable.gestion_contact_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(40.dp)
                )
                Row {
                    Text(
                        text = "Contact Manager ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color  = Color(0xFFFFFFFF),

                        )
                    Text(
                        text = "Pro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color  = Color(0xFF76A2EE),

                        )
                }

            }

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
                StatsCard("Total Contacts", totalContacts.toString(), modifier = Modifier.weight(1f))
                StatsCard("Recent Added", recentAdded.toString(), modifier = Modifier.weight(1f))
                StatsCard("Total Pinned", totalPinned.toString(), modifier = Modifier.weight(1f))
                StatsCard("Most Called", mostCalled, modifier = Modifier.weight(1f))
            }

            // Action Buttons
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
            )

            PremiumActionButton(
                icon = Icons.Default.Add,
                title = "Add Contact",
                description = "Create a new contact",
                gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5)),
                onClick = {
                    navController?.navigate("addContact/$userId")
                }
            )

            PremiumActionButton(
                icon = Icons.AutoMirrored.Filled.List,
                title = "View Contacts",
                description = "Browse your contact list",
                gradientColors = listOf(Color(0xFF66BB6A), Color(0xFF43A047)),
                onClick = {
                    navController?.navigate("viewContacts/$userId")
                }
            )
            PremiumActionButton(
                icon =Icons.Filled.Star,
                title = "Pinned Contacts",
                description = "Browse your pinned contacts",
                gradientColors = listOf(Color(0xFFDAB803), Color(0xFFB68C0F)),
                onClick = {
                    navController?.navigate("viewPinnedContacts/$userId")
                }
            )

            PremiumActionButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Logout",
                description = "Sign out of your account",
                gradientColors = listOf(Color(0xFFEF5350), Color(0xFFE53935)),
                onClick = onLogout
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable {
                        (context as? androidx.activity.ComponentActivity)?.finishAffinity()
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
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
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

