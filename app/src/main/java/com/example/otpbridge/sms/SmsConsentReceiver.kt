package com.example.otpbridge.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever

class SmsConsentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION != intent.action) return
        val extras = intent.extras ?: return
        val status = extras.get(SmsRetriever.EXTRA_STATUS) as? com.google.android.gms.common.api.Status ?: return
        if (!status.isSuccess) return
        val consent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT, Intent::class.java) ?: return
        context.startActivity(Intent(context, SmsConsentActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(SmsConsentActivity.EXTRA_CONSENT_INTENT, consent)
        })
    }
}
