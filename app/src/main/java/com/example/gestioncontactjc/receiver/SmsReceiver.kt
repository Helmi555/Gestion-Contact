package com.example.gestioncontactjc.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.gestioncontactjc.service.LocationService
import com.example.gestioncontactjc.util.MessageFormat
import com.example.gestioncontactjc.util.NotificationUtils


//TODO: adb -s emulator-5554 emu sms send 5556 "LOCREQ: please share your location"  and  adb -s emulator-5554 emu sms send 5554 "LOCRESP:37.42,-122.08"



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

                Log.d("SmsReceiver", "From: $sender, Body: $body")

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
                        putExtra(LocationService.EXTRA_SENDER, sender)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appContext.startForegroundService(serviceIntent)
                    } else {
                        appContext.startService(serviceIntent)
                    }
                }
            }
        } finally {
            pendingResult.finish()
        }
    }
}