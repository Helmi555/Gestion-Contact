package com.example.gestioncontactjc.ui.navigation

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.gestioncontactjc.data.utils.SessionManager
import com.example.gestioncontactjc.ui.screens.*

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen {
            showSplash = false
        }
        return
    }

    val session = sessionManager.getUserSession()
    val startDestination =
        if (session != null && session.second) "home/${session.first}" else "login"

    Log.d("NAV", "startDestination = $startDestination, session=$session")

    fun resolveUserIdFromBundle(bundle: android.os.Bundle?): Int {
        if (bundle == null) return session?.first ?: sessionManager.getUserSession()?.first ?: 0
        val raw = bundle.get("userId")
        return when (raw) {
            is Int -> raw
            is Long -> raw.toInt()
            is String -> raw.toIntOrNull() ?: (session?.first ?: sessionManager.getUserSession()?.first ?: 0)
            else -> session?.first ?: sessionManager.getUserSession()?.first ?: 0
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute?.startsWith("home/") == true ||
            currentRoute?.startsWith("viewContacts/") == true ||
            currentRoute?.startsWith("viewPinnedContacts/") == true ||
            currentRoute?.startsWith("profile/") == true ||
            currentRoute?.startsWith("addContact/") == true ||
            currentRoute?.startsWith("positionsScreen/") == true ||
            currentRoute ?.startsWith("conversationsScreen/") == true
       //     || currentRoute ?.startsWith("conversationScreen/") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val activeUserId = resolveUserIdFromBundle(navBackStackEntry?.arguments)
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(Color(0xFF0D47A1)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,

                        ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute?.contains(item.route) == true
                          //  val offsetY by animateDpAsState(if (selected) (-12).dp else 0.dp)
                            val iconTint by animateColorAsState(
                                if (selected) Color.White else Color(0xFFB3E5FC)
                            )
                            val iconSize = if(selected) 32.dp else 24.dp

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                   // .offset(y = offsetY)
                                    .clip(CircleShape)
                                    .clickable {
                                        navController.navigate("${item.route}/$activeUserId") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(iconSize)
                                )
                                if (!selected) {
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        color = iconTint,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1976D2))
                    )
                )
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-100).dp, y = (-100).dp)
                    .background(Color.White.copy(alpha = 0.1f), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 280.dp, y = 500.dp)
                    .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
            )

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { userId, rememberMe ->
                            if (rememberMe) sessionManager.saveUserSession(userId, rememberMe)
                            navController.navigate("home/$userId") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onSignUpClick = { navController.navigate("signup") }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        onSignUpSuccess = { userId ->
                            sessionManager.saveUserSession(userId, true)
                            navController.navigate("home/$userId") {
                                popUpTo("signup") { inclusive = true }
                            }
                        },
                        onLoginClick = { navController.navigate("login") }
                    )
                }

                composable(
                    route = "home/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val uid = resolveUserIdFromBundle(backStackEntry.arguments)
                    Log.d("NAV HOME", "Passing user with ID: $uid")
                    HomeScreen(
                        userId = uid,
                        navController = navController

                    )
                }

                composable(
                    route = "viewContacts/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val uid = resolveUserIdFromBundle(backStackEntry.arguments)
                    ViewContactsScreen(uid, navController)
                }

                composable(
                    route = "viewPinnedContacts/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val uid = resolveUserIdFromBundle(backStackEntry.arguments)
                    PinnedContactsScreen(uid, navController)
                }

                composable(
                    route = "profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val uid = resolveUserIdFromBundle(backStackEntry.arguments)
                    ProfileScreen(uid,
                        navController = navController,
                            onLogout = {
                            sessionManager.clearSession()
                            navController.navigate("login") {
                                popUpTo("home/$uid") { inclusive = true }
                            }
                        }
                    )

                }
                composable("addContact/{userId}") {
                    backStackEntry ->
                    val uid = resolveUserIdFromBundle(backStackEntry.arguments)
                    AddContactScreen(
                        uid,
                        navController,
                        )
                }
                composable("conversationsScreen/{userId}") { backStackEntry ->
                    val uid = resolveUserIdFromBundle(backStackEntry.arguments)
                    ConversationsScreen(
                        userId = uid,
                        navController = navController
                    )
                }
                composable(
                    route = "conversationScreen/{contactId}",
                    arguments = listOf(navArgument("contactId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
                    ConversationScreen(contactId = contactId, navController = navController)
                }
                composable(
                    route = "positionsScreen/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val contactId = resolveUserIdFromBundle(backStackEntry.arguments)
                    PositionsScreen(contactId = contactId, navController = navController)
                }



            }
        }
    }
}
