package helmi.benabdelghani.gestioncontactjc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.Sms
import helmi.benabdelghani.gestioncontactjc.data.utils.SessionManager
import helmi.benabdelghani.gestioncontactjc.service.LocationService
import helmi.benabdelghani.gestioncontactjc.util.MessageFormat
import helmi.benabdelghani.gestioncontactjc.util.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()

        try {
            val messages: Array<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val appContext = context.applicationContext

            messages.forEach { message ->
                val sender = message.originatingAddress ?: return@forEach
                val body = message.messageBody ?: return@forEach
                val normalizedSender = normalizePhoneNumber(sender)

                Log.d("SmsReceiver", "From: $sender, Body: $body")

                saveReceivedSms(appContext, normalizedSender, body)

                if (MessageFormat.isLocationResponse(body)) {
                    val coords = MessageFormat.parseLocationResponse(body)
                    coords?.let { (lat, lon) ->
                        NotificationUtils.showLocationNotification(appContext, sender, lat, lon)
                    }
                    return@forEach
                }

                if (MessageFormat.isLocationRequest(body)) {
                    Log.d("SmsReceiver", "LOCREQ from $sender - starting service")

                    val serviceIntent = Intent(appContext, LocationService::class.java).apply {
                        putExtra(LocationService.Companion.EXTRA_SENDER, normalizedSender)
                    }

                    appContext.startForegroundService(serviceIntent)
                }
            }
        } finally {
            pendingResult.finish()
        }
    }

    private fun saveReceivedSms(context: Context, sender: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager(context)
                val currentUserSession = sessionManager.getUserSession()
                val currentUserId = currentUserSession?.first

                if (currentUserId != null) {
                    val db = AppDatabase.getDatabase(context)
                    val senderContact = db.contactDao().getContactByPhoneNumber(currentUserId, sender)

                    val sms = Sms(
                        address = sender,
                        body = body,
                        isSender = false,
                        contactId = senderContact?.id
                    )

                    db.smsDao().insert(sms)
                    Log.d("SmsReceiver", "Saved received SMS from $sender")
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Failed to save received SMS", e)
            }
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        return phone.replace("+216", "").replace(Regex("[^0-9]"), "")
    }
}