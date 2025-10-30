package helmi.benabdelghani.gestioncontactjc.ui.screens


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.Sms
import helmi.benabdelghani.gestioncontactjc.ui.components.ConversationCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    contactId: Int,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var contactName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val messagesFlow = remember(contactId) { db.smsDao().getByContactIdFlow(contactId) }
    val messages by messagesFlow.collectAsState(initial = emptyList())

    LaunchedEffect(contactId) {
        try {
            val contact = db.contactDao().getContactById(contactId)
            contactName = contact?.nom ?: "Conversation"
        } catch (e: Exception) {
            Log.e("ConversationScreen", "Error loading contact", e)
        } finally {
            isLoading = false
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {
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
                        text = contactName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (isLoading) {
                    Text(
                        "Loading messages...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (messages.isEmpty()) {
                    Text(
                        "No messages yet",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages) { msg ->
                            ConversationCard(sms = msg)
                        }
                    }
                }
            }
        }
    }
}
