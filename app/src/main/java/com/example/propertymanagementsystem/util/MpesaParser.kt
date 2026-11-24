package com.example.propertymanagementsystem.util

import java.util.Locale

/**
 * A small parser for the M-Pesa confirmation messages that land on the
 * agent's phone.
 *
 * The app deliberately does not read SMS. The agent pastes the message, the
 * parser shows what it understood, and the agent confirms before anything is
 * saved. That keeps the app privacy-friendly and makes a wrong parse easy to
 * catch.
 */
data class MpesaMessage(
    val amount: Double,
    val phone: String,
    val date: String,
    val reference: String,
    val senderName: String,
    val raw: String
)

object MpesaParser {

    private val amountPattern =
        Regex("""(?:Ksh|KES)\s?([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val phonePattern =
        Regex("""(\+?254\d{9}|0\d{9}|[17]\d{8})""")
    private val referencePattern =
        Regex("""^([A-Z0-9]{8,12})\s+Confirmed""", RegexOption.IGNORE_CASE)
    private val datePattern =
        Regex("""on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})""", RegexOption.IGNORE_CASE)
    private val nameBeforePhonePattern =
        Regex("""from\s+(.+?)\s+(?:\+?254|0)\d{9}""", RegexOption.IGNORE_CASE)
    private val nameAfterPhonePattern =
        Regex("""(?:\+?254|0)\d{9}\s*[-–]\s*([A-Za-z .'-]+)""")

    fun parse(raw: String): MpesaMessage? {
        val text = raw.trim()
        if (text.isEmpty()) return null

        val lower = text.lowercase(Locale.US)
        if (!lower.contains("received") && !lower.contains("confirmed")) return null
        if (lower.contains("sent to") && !lower.contains("received")) return null

        val amount = amountPattern.find(text)
            ?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?: return null
        if (amount <= 0.0) return null

        val phone = phonePattern.find(text)?.value ?: return null
        val reference = referencePattern.find(text)?.groupValues?.getOrNull(1).orEmpty()
        val sender = nameBeforePhonePattern.find(text)?.groupValues?.getOrNull(1)
            ?: nameAfterPhonePattern.find(text)?.groupValues?.getOrNull(1)
            ?: ""

        return MpesaMessage(
            amount = amount,
            phone = normalizePhone(phone),
            date = parseDate(text) ?: Dates.today(),
            reference = reference.trim(),
            senderName = sender.trim().trim('-', '–', ' '),
            raw = text
        )
    }

    fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return digits.takeLast(9)
    }

    private fun parseDate(text: String): String? {
        val match = datePattern.find(text) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null
        val rawYear = match.groupValues[3].toIntOrNull() ?: return null
        val year = if (rawYear < 100) 2000 + rawYear else rawYear
        if (month !in 1..12 || day !in 1..31) return null
        return "$year-" + month.toString().padStart(2, '0') + "-" +
            day.toString().padStart(2, '0')
    }
}
