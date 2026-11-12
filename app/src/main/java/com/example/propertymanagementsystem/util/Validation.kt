package com.example.propertymanagementsystem.util

/**
 * Small checks that are used when the user fills in a form.
 *
 * They are kept in one place so the same rule is used on every screen. The
 * form screens call these and then show the message that comes back.
 */
object Validation {

    // Checks if the user typed nothing, or only spaces.
    fun isEmpty(text: String): Boolean = text.trim().isEmpty()

    // A basic email check. It looks for text, then @, then a dot with
    // something after it. It is not perfect but it catches most mistakes.
    fun isValidEmail(email: String): Boolean {
        if (isEmpty(email)) return false
        val trimmed = email.trim()
        val at = trimmed.indexOf('@')
        val dot = trimmed.lastIndexOf('.')
        return at > 0 && dot > at + 1 && dot < trimmed.length - 1
    }

    // Kenyan phone numbers are usually 10 digits and start with 0 or 7.
    // Spaces and dashes are removed first so 0712 345 678 is also accepted.
    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        if (cleaned.length != 10) return false
        return cleaned.all { it.isDigit() } && (cleaned.startsWith("0") || cleaned.startsWith("7"))
    }

    // The amount has to be more than zero and it cannot be too big.
    fun isValidAmount(amount: Double): Boolean = amount > 0 && amount <= 10_000_000

    // The day is written as yyyy-MM-dd so it can be sorted as plain text.
    fun isValidDate(date: String): Boolean {
        if (date.length != 10) return false
        val parts = date.split("-")
        if (parts.size != 3) return false
        val month = parts[1].toIntOrNull() ?: return false
        val day = parts[2].toIntOrNull() ?: return false
        return month in 1..12 && day in 1..31
    }
}
