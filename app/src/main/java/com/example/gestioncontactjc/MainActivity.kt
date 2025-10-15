package com.example.gestioncontactjc

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gestioncontactjc.data.database.AppDatabase
import com.example.gestioncontactjc.data.model.Contact
import com.example.gestioncontactjc.data.model.User
import com.example.gestioncontactjc.ui.navigation.AppNavGraph
import com.example.gestioncontactjc.ui.theme.GestionContactJCTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       WindowCompat.setDecorFitsSystemWindows(window, false) // Edge-to-edge

        setContent {
            GestionContactJCTheme {
                val view = LocalView.current

                SideEffect {
                    window.statusBarColor = android.graphics.Color.TRANSPARENT // Transparent status bar
                    WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false // Use dark icons if needed
                }
                AppNavGraph()
            }
        }

        //Seed initial data
CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            val userDao = db.userDao()
            val user1 = userDao.getUserByUsername("helmi")
            var user1Id: Int? = user1?.id

            if (user1 == null) {
                userDao.insert(User(username = "helmi", name = "Helmi", password = "1234"))
                val newUser = userDao.getUserByUsername("helmi")
                user1Id = newUser?.id
                Log.d("MAIN ACTIVITY", "Seeding users")
            }

            val user2 = userDao.getUserByUsername("youssef")
            if (user2 == null) {
                userDao.insert(User(username = "youssef", name = "Youssef", password = "1234"))
            }

            val contactDao = db.contactDao()
            if (user1Id != null && contactDao.getContactsForUser(user1Id).isEmpty()) {
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Alice",
                        pseudo = "Ally",
                        phoneNumber = "1234567890",
                        isPinned = true
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Bob",
                        pseudo = "Bobby",
                        phoneNumber = "2345678901"
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Bapa",
                        pseudo = "Bapa",
                        phoneNumber = "2345678921"
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Borly",
                        pseudo = "borlyy",
                        phoneNumber = "2345628901"
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Charlie",
                        pseudo = "Chuck",
                        phoneNumber = "3456789012",
                        isPinned = true
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "David",
                        pseudo = "Dave",
                        phoneNumber = "4567890123"
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Eve",
                        pseudo = "Evie",
                        phoneNumber = "5678901234"
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Frank",
                        pseudo = "Franky",
                        phoneNumber = "6789012345"
                    )
                )
                contactDao.insert(
                    Contact(
                        userId = user1Id,
                        nom = "Grace",
                        pseudo = "Gracie",
                        phoneNumber = "7890123456"
                    )
                )

            }
        }
    }
}