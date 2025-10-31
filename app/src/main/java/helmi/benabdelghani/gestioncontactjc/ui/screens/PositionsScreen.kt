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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
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
import helmi.benabdelghani.gestioncontactjc.data.model.Position
import helmi.benabdelghani.gestioncontactjc.repository.PositionRepository
import helmi.benabdelghani.gestioncontactjc.ui.components.PositionCard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import helmi.benabdelghani.gestioncontactjc.MapActivity
import helmi.benabdelghani.gestioncontactjc.repository.PositionRepository.deletePosition
import kotlinx.coroutines.launch


@Composable
fun PositionsScreen(
    contactId: Int,
    navController: NavController? = null,
    onCountChange: (Int) -> Unit = {}
) {
    var positions by remember { mutableStateOf<List<Position>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as ComponentActivity

    val addPositionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                positions = PositionRepository.fetchPositions(contactId)
                onCountChange(positions.size)
            }
        }
    }

    LaunchedEffect(contactId) {
        positions = PositionRepository.fetchPositions(contactId)
        onCountChange(positions.size)
    }
    Box(modifier = Modifier.fillMaxSize()) {
     if(positions.isEmpty()){
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
                         " POSITIONS ",
                         color = Color(0xFFFFD700),
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Bold
                     )
                     Text(
                         " yet",
                         color = Color.White,
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Bold
                     )
                 }

                 Text(
                     "Save a position to get started",
                     color = Color.White.copy(alpha = 0.7f),
                     fontSize = 14.sp,
                     modifier = Modifier.padding(top = 8.dp)
                 )
             }
         }
     }else {
         LazyColumn(
             verticalArrangement = Arrangement.spacedBy(12.dp),
             modifier = Modifier.fillMaxSize().padding(vertical = 24.dp)
         )
         {
             items(positions, key = { it.idposition }) { pos ->
                 PositionCard(
                     position = pos,
                     onDelete = {
                         coroutineScope.launch {
                             val ok = deletePosition(pos.idposition)
                             if (ok) {
                                 val newPositions =
                                     positions.filter { it.idposition != pos.idposition }
                                 positions = newPositions
                                 onCountChange(newPositions.size)
                                 Toast.makeText(context, "Position deleted", Toast.LENGTH_SHORT)
                                     .show()
                             }
                         }
                     },
                     onClick = {
                         val intent = Intent(context, MapActivity::class.java).apply {
                             putExtra(MapActivity.EXTRA_LATITUDE, pos.latitude)
                             putExtra(MapActivity.EXTRA_LONGITUDE, pos.longitude)
                             putExtra(MapActivity.EXTRA_SENDER, pos.pseudo)
                         }
                         context.startActivity(intent)

                     },

                     )


             }
             item {
                 Spacer(modifier = Modifier.height(12.dp))
             }
         }
     }
        AddPositionFAB(onClick = {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra(MapActivity.EXTRA_MODE, "add")
            intent.putExtra(MapActivity.EXTRA_USERID, contactId)
            addPositionLauncher.launch(intent)
        })

    }

}


@Composable
fun AddPositionFAB(onClick: () -> Unit) {
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
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Position",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
