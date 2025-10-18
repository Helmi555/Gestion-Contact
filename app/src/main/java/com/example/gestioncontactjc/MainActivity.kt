package com.example.gestioncontactjc

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.gestioncontactjc.data.database.AppDatabase
import com.example.gestioncontactjc.data.model.Contact
import com.example.gestioncontactjc.data.model.Sms
import com.example.gestioncontactjc.data.model.User
import com.example.gestioncontactjc.ui.navigation.AppNavGraph
import com.example.gestioncontactjc.ui.theme.GestionContactJCTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.os.Build
import kotlin.text.compareTo

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false) // Edge-to-edge

        val basicPermissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            basicPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!basicPermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, basicPermissions.toTypedArray(), 1)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2)
        }


        setContent {
            GestionContactJCTheme {
                val view = LocalView.current

                SideEffect {
                    window.statusBarColor = Color.TRANSPARENT // Transparent status bar
                    WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false // Use dark icons if needed
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.BLACK

                    val controller = WindowInsetsControllerCompat(window, view)
                    controller.isAppearanceLightStatusBars = false
                    controller.isAppearanceLightNavigationBars = false

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

            val smsDao = db.smsDao()
    if (user1Id != null) {
        val contacts = contactDao.getContactsForUser(user1Id)
        if (contacts.isNotEmpty()) {
            val existingSms = smsDao.getByContactId(contacts[0].id).firstOrNull() ?: emptyList()
            if (existingSms.isEmpty()) {
                smsDao.insert(
                    Sms(
                        contactId = contacts[0].id,
                        address = contacts[0].phoneNumber,
                        body = "Hey Alice!",
                        isSender = true
                    )
                )
                smsDao.insert(
                    Sms(
                        contactId = contacts[0].id,
                        address = contacts[0].phoneNumber,
                        body = "How are you?",
                        isSender = false,
                        latitude = 9.666,
                        longitude = 10.253,


                    )
                )
                smsDao.insert(
                    Sms(
                        contactId = contacts[1].id,
                        address = contacts[1].phoneNumber,
                        body = "Hi Bob!",
                        isSender = true
                    )
                )
                smsDao.insert(
                    Sms(
                        contactId = contacts[1].id,
                        address = contacts[1].phoneNumber,
                        body = "See you soon!",
                        isSender = false
                    )
                )
            }
        }
    }


}}
}