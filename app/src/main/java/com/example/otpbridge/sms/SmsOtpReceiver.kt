package com.example.otpbridge.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.otpbridge.data.OtpParser
import com.example.otpbridge.data.OtpStore

class SmsOtpReceiver : BroadcastReceiver() {
    companion object { const val ACTION_ENABLE = "com.example.otpbridge.ENABLE_SMS" }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ENABLE) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        for (message in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            val code = OtpParser.extract(message.messageBody) ?: continue
            OtpStore.save(context, code, "SMS")
            break
        }
    }
}
