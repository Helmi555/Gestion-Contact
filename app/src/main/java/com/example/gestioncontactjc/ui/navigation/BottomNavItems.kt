package com.example.gestioncontactjc.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Default.Home),
    BottomNavItem("viewContacts", "Contacts", Icons.Default.AccountBox),
    BottomNavItem("viewPinnedContacts", "Pinned", Icons.Default.Star),
    BottomNavItem("conversationsScreen", "Messages", Icons.Default.Email),
    BottomNavItem("positionsScreen", "Locations", Icons.Default.LocationOn),
    BottomNavItem("profile", "Profile", Icons.Default.Person)
)
