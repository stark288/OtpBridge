package com.example.otpbridge.data

import android.content.Context

object OtpStore {
    private const val PREF = "otp_bridge"
    private const val KEY_CODE = "latest_code"
    private const val KEY_SOURCE = "latest_source"

    fun save(context: Context, code: String, source: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_CODE, code).putString(KEY_SOURCE, source).apply()
    }
    fun code(context: Context): String? = context.getSharedPreferences(PREF, 0).getString(KEY_CODE, null)
    fun source(context: Context): String? = context.getSharedPreferences(PREF, 0).getString(KEY_SOURCE, null)
}
