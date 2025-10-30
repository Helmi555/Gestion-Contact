package helmi.benabdelghani.gestioncontactjc.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.Sms
import helmi.benabdelghani.gestioncontactjc.ui.components.ConversationListCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ConversationsScreen(
    userId: Int,
    navController: NavController? = null,
    onCountChange: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val contactsFlow = remember(userId) {
        db.contactDao().getContactsForUserSortedFlow(userId)
    }
    val contacts by contactsFlow.collectAsState(initial = emptyList())

    val lastMessages = contacts.associate { contact ->
        contact.id to produceState<Sms?>(initialValue = null, contact.id, contacts) {
            val flow = db.smsDao().getLastMessageByContactId(contact.id)
            flow.collect { msg -> value = msg }
        }.value
    }.filterValues { it != null } as Map<Int, Sms>

    val contactsWithMessages = remember(contacts, lastMessages) {
        contacts.filter { lastMessages.containsKey(it.id) }
    }

    // Call onCountChange only when the size changes
    LaunchedEffect(contactsWithMessages.size) {
        onCountChange(contactsWithMessages.size)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            if (contactsWithMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No conversations yet", color = Color.White, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(contactsWithMessages) { contact ->
                        ConversationListCard(
                            contact = contact,
                            lastMessage = lastMessages[contact.id],
                            onClick = { navController?.navigate("conversationScreen/${contact.id}") }
                        )
                    }
                }
            }
        }
    }
}