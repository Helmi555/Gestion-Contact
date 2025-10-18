package com.example.gestioncontactjc.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.gestioncontactjc.data.database.AppDatabase
import com.example.gestioncontactjc.data.model.Contact
import com.example.gestioncontactjc.ui.components.ContactCard
import com.example.gestioncontactjc.ui.components.EditContactModal
import com.example.gestioncontactjc.util.MessageFormat
import com.example.gestioncontactjc.util.SmsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun ViewContactsScreen(
    userId: Int = 0,
    navController: NavController?,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var pendingCallNumber by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }
    var showEditContactModal by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }


    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pendingCallNumber?.let { number ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            pendingCallNumber = null
        }
    }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                contacts = db.contactDao().getContactsForUserSorted(userId)
            } finally {
                isLoading = false
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    )
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 32.dp)
        ) {
            IconButton(
                onClick = { navController?.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Contacts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${contacts.size} contact${if (contacts.size > 1) "s" else ""} saved",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading contacts...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        } else if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No contacts yet",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add a new contact to get started",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            val groupedContacts = contacts
                .groupBy { contact ->
                    contact.nom.firstOrNull()?.uppercaseChar() ?: '#'
                }
                .toSortedMap()

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedContacts.forEach { (letter, contactsInGroup) ->
                    item {
                        Text(
                            text = letter.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(contactsInGroup) { contact ->
                        ContactCard(
                            contact = contact,
                            onDelete = {
                                contactToDelete = contact
                                showDeleteDialog = true
                            },
                            onCall = {
                                scope.launch {
                                    val db = AppDatabase.getDatabase(context)
                                    val updatedContact =
                                        contact.copy(callCount = contact.callCount + 1)
                                    db.contactDao().update(updatedContact)

                                    contacts =
                                        contacts.map { if (it.id == contact.id) updatedContact else it }

                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CALL_PHONE
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pendingCallNumber = contact.phoneNumber
                                        callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                                    } else {
                                        val intent = Intent(
                                            Intent.ACTION_CALL,
                                            Uri.parse("tel:${contact.phoneNumber}")
                                        )
                                        context.startActivity(intent)
                                        Log.d("CALL", "Calling: ${contact.phoneNumber}")
                                    }
                                }
                            },
                            onEdit = {
                                contactToEdit = contact
                                showEditContactModal = true
                            },
                            onPin = {
                                scope.launch {
                                    try {
                                        val db = AppDatabase.getDatabase(context)
                                        db.contactDao().togglePinContact(contact.id)
                                        val updatedContact =
                                            db.contactDao().getContactById(contact.id)
                                        contacts = db.contactDao().getContactsForUserSorted(userId)
                                        withContext(Dispatchers.Main) {
                                            val msg =
                                                if (updatedContact?.isPinned == true) "📌 Pinned! 😊" else "Unpinned 😕"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("VIEW_CONTACTS", "Error pinning/unpinning contact", e)
                                    }
                                }
                            },
                            onGetLocation = {
                                scope.launch {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                                        == PackageManager.PERMISSION_GRANTED) {
                                        SmsUtils.sendSms(context, contact.phoneNumber, MessageFormat.locationRequest())
                                        Log.d("CHAT", "Sent LOCREQ to ${contact.phoneNumber}")
                                    } else {
                                        // request permission or show message
                                    }
                                }

                            }
                        )
                    }
                }
            }
        }
        if (showDeleteDialog && contactToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Contact") },
                text = { Text("Are you sure you want to delete ${contactToDelete!!.nom}?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                val db = AppDatabase.getDatabase(context)
                                db.contactDao().delete(contactToDelete!!)
                                contacts =
                                    contacts.filter { it.id != contactToDelete!!.id }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Contact deleted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.d("VIEW_CONTACTS", "Error deleting contact", e)
                            }
                        }
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }


    }

    if (showEditContactModal && contactToEdit != null) {
        EditContactModal(
            contact = contactToEdit!!,
            onDismiss = { showEditContactModal = false },
            visible = true,
            onSave = { updatedContact ->
                scope.launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        db.contactDao().update(updatedContact)
                        contacts = contacts.map {
                            if (it.id == updatedContact.id) updatedContact else it
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Contact updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("VIEW_CONTACTS", "Error updating contact", e)
                    }
                }
                showEditContactModal = false
            }
        )
    }
        AddContactFAB(
            onClick = {
                navController?.navigate("addContact/$userId") {
                    launchSingleTop = true
                }

            }
        )
}
}

@Composable
fun AddContactFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(60.dp),
            containerColor = Color(0xFF0D47A1),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Contact",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

