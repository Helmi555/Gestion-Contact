package helmi.benabdelghani.gestioncontactjc.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import helmi.benabdelghani.gestioncontactjc.MainActivity
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingSession
import helmi.benabdelghani.gestioncontactjc.ui.components.SessionCard
import helmi.benabdelghani.gestioncontactjc.ui.map.SessionMapActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SessionsScreen(
    userId: Int,
    navController: NavController? = null,
    onCountChange: (Int) -> Unit = {}
) {
    var sessions by remember { mutableStateOf<List<TrackingSession>>(emptyList()) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.trackingSessionDao()
    val coroutineScope = rememberCoroutineScope()
    val activity = context as ComponentActivity

    val addSessionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                val list = withContext(Dispatchers.IO) { dao.getAllSessions() }
                sessions = list.filter { it.userId == userId }
                onCountChange(sessions.size)
            }
        }
    }

    LaunchedEffect(userId) {
        val list = withContext(Dispatchers.IO) { dao.getAllSessions() }
        sessions = list.filter { it.userId == userId }
        onCountChange(sessions.size)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Text("No", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(" SESSIONS ", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("yet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Start a session to get started",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp, horizontal =10.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onDelete = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    db.trackingSessionDao().deleteSession(session)

                                }
                                val newList = sessions.filter { it.id != session.id }
                                sessions = newList
                                onCountChange(newList.size)
                                Toast.makeText(context, "Session deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClick = {
                            val intent = Intent(context, SessionMapActivity::class.java).apply {
                                putExtra(SessionMapActivity.EXTRA_SESSION_ID, session.id)
                                putExtra(SessionMapActivity.EXTRA_USER_ID, session.userId)
                            }
                            context.startActivity(intent)

                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }


    }
}

@Composable
fun AddSessionFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 0.dp, bottom = 6.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = Color(0xFF0D47A1),
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Session", modifier = Modifier.size(24.dp))
        }
    }
}
