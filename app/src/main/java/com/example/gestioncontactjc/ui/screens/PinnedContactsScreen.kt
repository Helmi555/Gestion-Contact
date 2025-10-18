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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.AlertDialog
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
fun PinnedContactsScreen(
    userId: Int = 0,
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pinnedContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
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
                pinnedContacts = db.contactDao().getAllPinnedContacts(userId)
            } finally {
                isLoading = false
            }
        }
    }




        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header
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
                        text = "Pinned Contacts",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${pinnedContacts.size} contact${if (pinnedContacts.size > 1) "s" else ""} saved",
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
            } else if (pinnedContacts.isEmpty()) {
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
                        Row(){
                            Text(
                                "No",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                " PINNED ",
                                color = Color(0xFFFFD700),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "contacts yet",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Pin a contact to get started",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pinnedContacts, key = {it.id}) { contact ->
                        ContactCard(
                            contact = contact,
                            onDelete = {
                                contactToDelete = contact
                                showDeleteDialog = true
                            },
                            onCall = {
                                scope.launch {
                                    val db = AppDatabase.getDatabase(context)
                                    val updatedContact = contact.copy(callCount = contact.callCount + 1)
                                    db.contactDao().update(updatedContact)

                                    pinnedContacts = pinnedContacts.map { if (it.id == contact.id) updatedContact else it }

                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CALL_PHONE
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        pendingCallNumber = contact.phoneNumber
                                        callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                                    } else {
                                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.phoneNumber}"))
                                        context.startActivity(intent) // comment if  for testing
                                        Log.d("CALL", "Would call: ${contact.phoneNumber}")
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
                                        withContext(Dispatchers.IO) {
                                            db.contactDao().togglePinContact(contact.id)
                                        }

                                        val newPinnedList = withContext(Dispatchers.IO) {
                                            db.contactDao().getAllPinnedContacts(userId)
                                        }

                                        pinnedContacts = newPinnedList

                                        // Show toast
                                        val wasPinned = contact.isPinned
                                        val msg = if (wasPinned) "Unpinned 😕" else "📌 Pinned! 😊"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                                    } catch (e: Exception) {
                                        Log.e("PIN_TOGGLE", "Error", e)
                                        Toast.makeText(context, "Failed to update pin", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onGetLocation = {
                                // call send location request
                                scope.launch {
                                    // ensure permission checked before calling
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
                                    pinnedContacts =
                                        pinnedContacts.filter { it.id != contactToDelete!!.id }
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
                        pinnedContacts = pinnedContacts.map {
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
}


