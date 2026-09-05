package com.example.otpbridge.data

object OtpParser {
    private val patterns = listOf(
        Regex("(?i)\\b(?:otp|one[- ]time password|verification code|security code|passcode)\\D{0,30}(\\d{4,8})\\b"),
        Regex("\\b\\d{6}\\b"),
        Regex("\\b\\d{4,8}\\b")
    )
    fun extract(text: String): String? = patterns.asSequence().mapNotNull { it.find(text)?.groupValues?.getOrNull(1) ?: it.find(text)?.value }
        .firstOrNull { it.length in 4..8 && it.all(Char::isDigit) }
}
