package com.example.gestioncontactjc.util

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

object SmsUtils {
    private const val TAG = "SmsUtils"

    fun sendSms(context: Context, phoneNumber: String?, body: String) {
        if (phoneNumber.isNullOrBlank()) {
            Log.e(TAG, "sendSms: empty phone")
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "sendSms: SEND_SMS permission not granted")
            return
        }

        try {
            val smsManager: SmsManager =
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()

            val sentAction = "SMS_SENT_${phoneNumber.hashCode()}"
            val deliveredAction = "SMS_DELIVERED_${phoneNumber.hashCode()}"

            val sentIntent = PendingIntent.getBroadcast(
                context,
                phoneNumber.hashCode(),
                Intent(sentAction),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val deliveredIntent = PendingIntent.getBroadcast(
                context,
                phoneNumber.hashCode() + 1,
                Intent(deliveredAction),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val sentReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    Log.d(TAG, "SENT callback for $phoneNumber result=${resultCode}")
                    try { ctx.unregisterReceiver(this) } catch (_: Exception) {}
                }
            }

            val deliveredReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    Log.d(TAG, "DELIVERED callback for $phoneNumber result=${resultCode}")
                    try { ctx.unregisterReceiver(this) } catch (_: Exception) {}
                }
            }

            context.applicationContext.registerReceiver(sentReceiver, IntentFilter(sentAction))
            context.applicationContext.registerReceiver(deliveredReceiver, IntentFilter(deliveredAction))

            val parts = ArrayList(smsManager.divideMessage(body))
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(
                    phoneNumber, null, parts,
                    arrayListOf(sentIntent), arrayListOf(deliveredIntent)
                )
            } else {
                smsManager.sendTextMessage(phoneNumber, null, body, sentIntent, deliveredIntent)
            }

            Log.d(TAG, "sendSms: PUBLISHED to SmsManager -> to=$phoneNumber body=${body.take(200)}")
        } catch (e: Exception) {
            Log.e(TAG, "sendSms error: ${e.message}", e)
        }
    }
}
