package com.example.otpbridge.sms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.otpbridge.data.OtpParser
import com.example.otpbridge.data.OtpStore

class SmsConsentActivity : Activity() {
    companion object { const val EXTRA_CONSENT_INTENT = "consent_intent"; private const val REQ_CONSENT = 7001 }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val consent = intent.getParcelableExtra(EXTRA_CONSENT_INTENT, Intent::class.java)
        if (consent == null) { finish(); return }
        startActivityForResult(consent, REQ_CONSENT)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_CONSENT) return
        if (resultCode == RESULT_OK) {
            val message = data?.getStringExtra("com.google.android.gms.auth.api.phone.EXTRA_SMS_MESSAGE")
            val code = message?.let(OtpParser::extract)
            if (code != null) OtpStore.save(this, code, "SMS")
            else Toast.makeText(this, "No OTP found in the approved SMS", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
