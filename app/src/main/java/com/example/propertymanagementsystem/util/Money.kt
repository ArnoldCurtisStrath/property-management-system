package com.example.propertymanagementsystem.util

import java.text.DecimalFormat

/**
 * Formats money the way the app speaks it: whole shillings with commas.
 *
 * The ledger works in Double because rent is a Double in the database, but
 * every figure the agent reads is rounded to a whole shilling. The receipt
 * and the statement use the same formatter, so the numbers always agree.
 */
object Money {

    private val whole = DecimalFormat("#,###")

    fun format(amount: Double): String {
        val rounded = Math.round(amount).toLong()
        return if (rounded < 0) {
            "-Ksh " + whole.format(-rounded)
        } else {
            "Ksh " + whole.format(rounded)
        }
    }

    fun plain(amount: Double): String {
        val rounded = Math.round(amount).toLong()
        return whole.format(rounded)
    }

    fun formatSigned(amount: Double): String {
        if (amount > 0) return "+" + format(amount)
        return format(amount)
    }

    fun parse(text: String): Double? {
        val cleaned = text
            .replace("Ksh", "", ignoreCase = true)
            .replace("KES", "", ignoreCase = true)
            .replace(",", "")
            .replace(" ", "")
            .trim()
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()
    }
}
