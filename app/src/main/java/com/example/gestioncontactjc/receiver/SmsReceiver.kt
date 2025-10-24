package com.example.gestioncontactjc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.gestioncontactjc.data.database.AppDatabase
import com.example.gestioncontactjc.data.model.Sms
import com.example.gestioncontactjc.data.utils.SessionManager
import com.example.gestioncontactjc.service.LocationService
import com.example.gestioncontactjc.util.MessageFormat
import com.example.gestioncontactjc.util.NotificationUtils
import kotlinx.coroutines.launch


//TODO: adb -s emulator-5554 emu sms send 5556 "LOCREQ: please share your location"  and  adb -s emulator-5554 emu sms send 5554 "LOCRESP:35.633,10.9000"

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
                        putExtra(LocationService.EXTRA_SENDER, normalizedSender)
                    }

                    appContext.startForegroundService(serviceIntent)
                }
            }
        } finally {
            pendingResult.finish()
        }
    }

    private fun saveReceivedSms(context: Context, sender: String, body: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
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