package com.example.propertymanagementsystem.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Small helpers for the dates that are used in the app.
 *
 * The dates are kept as plain text in the form yyyy-MM-dd. The month is kept
 * as yyyy-MM. Text like that can be compared and sorted as it is, so the
 * database does not have to do anything special with it.
 *
 * Everything here uses Calendar rather than java.time because the app still
 * supports Android 7 (API 24), where java.time is not available without
 * desugaring.
 */
object Dates {

    private val DAY = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val MONTH = SimpleDateFormat("yyyy-MM", Locale.US)

    fun today(): String = format(Date(), "yyyy-MM-dd")

    fun currentMonth(): String = format(Date(), "yyyy-MM")

    fun monthOf(date: String): String {
        if (date.length >= 7) return date.substring(0, 7)
        return currentMonth()
    }

    fun yearOf(date: String): String {
        if (date.length >= 4) return date.substring(0, 4)
        return currentMonth().substring(0, 4)
    }

    fun dayOfMonth(date: String): Int =
        date.substringAfterLast("-", "").toIntOrNull() ?: 1

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

    fun monthShortLabel(month: String): String {
        val parts = month.split("-")
        if (parts.size != 2) return month
        val year = parts[0].toIntOrNull() ?: return month
        val number = parts[1].toIntOrNull() ?: return month
        val names = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        if (number < 1 || number > 12) return month
        return "${names[number - 1]} $year"
    }

    fun shortDate(date: String): String {
        val parsed = parse(date) ?: return date
        return SimpleDateFormat("d MMM", Locale.US).format(parsed.time)
    }

    fun longDate(): String = format(Date(), "EEEE, d MMMM")

    fun parse(date: String): Calendar? {
        return try {
            val parsed = DAY.parse(date) ?: return null
            Calendar.getInstance().apply { time = parsed }
        } catch (error: Exception) {
            null
        }
    }

    fun daysInMonth(month: String): Int {
        val parsed = parse("$month-01") ?: return 30
        return parsed.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun addMonths(month: String, count: Int): String {
        val parsed = parse("$month-01") ?: return currentMonth()
        parsed.add(Calendar.MONTH, count)
        return MONTH.format(parsed.time)
    }

    fun monthsBetween(startMonth: String, endMonth: String): List<String> {
        if (startMonth.isEmpty() || endMonth.isEmpty()) return emptyList()
        val months = mutableListOf<String>()
        var cursor = startMonth
        var guard = 0
        while (cursor <= endMonth && guard < 240) {
            months.add(cursor)
            cursor = addMonths(cursor, 1)
            guard++
        }
        return months
    }

    fun dueDate(month: String, dueDay: Int): String {
        val safeDay = dueDay.coerceIn(1, 28)
        return "$month-" + safeDay.toString().padStart(2, '0')
    }

    /**
     * Whole days between two dates, ignoring the time of day. A due date of
     * today is 0 days late; yesterday is 1 day late.
     */
    fun daysLate(dueDate: String, today: String = today()): Int {
        val due = parse(dueDate) ?: return 0
        val now = parse(today) ?: return 0
        due.set(Calendar.HOUR_OF_DAY, 0)
        due.set(Calendar.MINUTE, 0)
        due.set(Calendar.SECOND, 0)
        due.set(Calendar.MILLISECOND, 0)
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        val days = TimeUnit.MILLISECONDS.toDays(now.timeInMillis - due.timeInMillis).toInt()
        return days.coerceAtLeast(0)
    }

    /**
     * Inclusive number of days between two dates in the same month. Used to
     * prorate the first or the last month of a tenancy.
     */
    fun daysBetweenInclusive(startDate: String, endDate: String): Int {
        val start = parse(startDate) ?: return 0
        val end = parse(endDate) ?: return 0
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        end.set(Calendar.HOUR_OF_DAY, 0)
        end.set(Calendar.MINUTE, 0)
        end.set(Calendar.SECOND, 0)
        end.set(Calendar.MILLISECOND, 0)
        val days = TimeUnit.MILLISECONDS.toDays(end.timeInMillis - start.timeInMillis).toInt()
        return (days + 1).coerceAtLeast(0)
    }

    private fun format(date: Date, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}
