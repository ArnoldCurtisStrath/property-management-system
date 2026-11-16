package com.example.propertymanagementsystem.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Small helpers for the dates that are used in the app.
 *
 * The dates are kept as plain text in the form yyyy-MM-dd. The month is kept
 * as yyyy-MM. Text like that can be compared and sorted as it is, so the
 * database does not have to do anything special with it.
 */
object Dates {

    // The day the user is looking at. It is the first day of the current
    // month until a day is picked from the calendar.
    fun today(): String = format(Date(), "yyyy-MM-dd")

    // The month the user is looking at, for example 2026-11.
    fun currentMonth(): String = format(Date(), "yyyy-MM")

    // Takes the month out of a day, so 2026-11-14 becomes 2026-11.
    fun monthOf(date: String): String {
        if (date.length >= 7) return date.substring(0, 7)
        return currentMonth()
    }

    // Turns 2026-11 into November 2026 so it can be shown to the user.
    fun monthLabel(month: String): String {
        val parts = month.split("-")
        if (parts.size != 2) return month
        val year = parts[0].toIntOrNull() ?: return month
        val number = parts[1].toIntOrNull() ?: return month
        val names = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        if (number < 1 || number > 12) return month
        return "${names[number - 1]} $year"
    }

    private fun format(date: Date, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}
