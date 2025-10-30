package helmi.benabdelghani.gestioncontactjc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helmi.benabdelghani.gestioncontactjc.data.model.Sms
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationCard(
    sms: Sms,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (sms.isSender) Color(0xFF42A5F5) else Color(0xFF37474F)
    val alignment = if (sms.isSender) Alignment.End else Alignment.Start
    val textColor = Color.White

    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sms.timestamp))

    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = if (sms.isSender) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .width(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Text(
                text = sms.body,
                color = textColor,
                fontSize = 16.sp
            )

            if (sms.latitude != null && sms.longitude != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${sms.latitude}, ${sms.longitude}",
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timestamp,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.align(alignment)
            )
        }
    }
}
