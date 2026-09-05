package com.example.otpbridge

import android.Manifest
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.otpbridge.data.OtpStore
import com.example.otpbridge.email.GmailReader
import com.example.otpbridge.sms.SmsOtpManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val gmailScope = "https://www.googleapis.com/auth/gmail.readonly"
    private val executor = Executors.newSingleThreadExecutor()
    private val smsPermissions = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
    private val smsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result[Manifest.permission.READ_SMS] == true && result[Manifest.permission.RECEIVE_SMS] == true) {
            SmsOtpManager.scanInbox(this)
            refresh()
            showStatus("SMS OTP capture is enabled")
        } else showStatus("SMS permissions are required for inbox and incoming-message OTP capture")
    }
    private val authResolution = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        try {
            val ar = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(result.data)
            syncGmail(ar.accessToken)
        } catch (e: Exception) { showStatus("Gmail authorization failed: ${e.message}") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refresh()
        findViewById<Button>(R.id.smsButton).setOnClickListener { startSms() }
        findViewById<Button>(R.id.gmailButton).setOnClickListener { authorizeGmail() }
        findViewById<Button>(R.id.copyButton).setOnClickListener { copyCode() }
    }

    private fun startSms() {
        val phone = findViewById<EditText>(R.id.phone).text.toString().trim()
        if (phone.isBlank()) { showStatus("Enter your mobile number first"); return }
        if (!SmsOtpManager.hasPermissions(this)) {
            smsPermissionLauncher.launch(smsPermissions)
        } else {
            SmsOtpManager.scanInbox(this)
            refresh()
            showStatus("SMS OTP capture is enabled for this device")
        }
    }

    private fun authorizeGmail() {
        val request = AuthorizationRequest.builder().setRequestedScopes(listOf(Scope(gmailScope))).build()
        Identity.getAuthorizationClient(this).authorize(request).addOnSuccessListener { result ->
            if (result.hasResolution()) {
                val pi: PendingIntent = result.pendingIntent ?: return@addOnSuccessListener
                authResolution.launch(androidx.activity.result.IntentSenderRequest.Builder(pi.intentSender).build())
            } else syncGmail(result.accessToken)
        }.addOnFailureListener { e -> showStatus("Gmail authorization failed: ${e.message}") }
    }

    private fun syncGmail(token: String?) {
        if (token.isNullOrBlank()) { showStatus("No Gmail access token returned"); return }
        showStatus("Checking recent Gmail messages for a verification code…")
        executor.execute {
            try {
                val code = GmailReader.findLatestOtp(token)
                runOnUiThread { if (code != null) { OtpStore.save(this, code, "Gmail"); refresh() } else showStatus("No OTP found in recent Gmail messages") }
            } catch (e: Exception) { runOnUiThread { showStatus("Gmail sync failed: ${e.message}") } }
        }
    }

    private fun copyCode() {
        val code = OtpStore.code(this) ?: return
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("OTP", code))
        Toast.makeText(this, "OTP copied", Toast.LENGTH_SHORT).show()
    }

    private fun refresh() {
        val code = OtpStore.code(this)
        val source = OtpStore.source(this)
        findViewById<TextView>(R.id.status).text = if (code == null) "No OTP captured" else "Latest OTP from $source"
        findViewById<TextView>(R.id.code).text = code ?: "—"
    }
    private fun showStatus(text: String) { findViewById<TextView>(R.id.status).text = text }
}
