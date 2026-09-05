package com.example.otpbridge.sms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.example.otpbridge.data.OtpParser
import com.example.otpbridge.data.OtpStore

object SmsOtpManager {
    fun hasPermissions(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED

    fun scanInbox(context: Context): Int {
        if (!hasPermissions(context)) return 0
        var count = 0
        val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE)
        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
            while (cursor.moveToNext() && count < 50) {
                val body = cursor.getString(bodyIndex) ?: continue
                val code = OtpParser.extract(body) ?: continue
                OtpStore.save(context, code, "SMS")
                count++
                break
            }
        }
        return count
    }

    fun startReceiver(context: Context) {
        if (!hasPermissions(context)) return
        context.sendBroadcast(Intent(SmsOtpReceiver.ACTION_ENABLE))
    }
}
