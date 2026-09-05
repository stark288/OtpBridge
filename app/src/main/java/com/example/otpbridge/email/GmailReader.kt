package com.example.otpbridge.email

import android.util.Base64
import com.example.otpbridge.data.OtpParser
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GmailReader {
    fun findLatestOtp(accessToken: String): String? {
        val list = get("https://gmail.googleapis.com/gmail/v1/users/me/messages?q=newer_than:2d&maxResults=20", accessToken)
        val ids = list.optJSONArray("messages") ?: return null
        for (i in 0 until ids.length()) {
            val id = ids.getJSONObject(i).optString("id")
            val msg = get("https://gmail.googleapis.com/gmail/v1/users/me/messages/$id?format=full", accessToken)
            val text = extractParts(msg.optJSONObject("payload"))
            OtpParser.extract(text)?.let { return it }
        }
        return null
    }

    private fun extractParts(part: JSONObject?): String {
        if (part == null) return ""
        val body = part.optJSONObject("body")
        val data = body?.optString("data").orEmpty()
        val own = if (data.isNotBlank()) try { String(Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP)) } catch (_: Exception) { "" } else ""
        val children = part.optJSONArray("parts") ?: return own
        return buildString {
            append(own)
            for (i in 0 until children.length()) append(extractParts(children.getJSONObject(i)))
        }
    }

    private fun get(url: String, token: String): JSONObject {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = "GET"
        c.setRequestProperty("Authorization", "Bearer $token")
        c.setRequestProperty("Accept", "application/json")
        c.connectTimeout = 15000
        c.readTimeout = 15000
        return c.inputStream.bufferedReader().use { JSONObject(it.readText()) }
    }
}
