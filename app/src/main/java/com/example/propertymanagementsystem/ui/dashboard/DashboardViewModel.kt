package com.example.propertymanagementsystem.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.SessionManager
import java.util.Calendar
import java.util.Locale

/**
 * The day book. It observes the ledger repository, so every number on the
 * Today screen changes by itself when a payment, expense or repair is saved.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container

    val today: LiveData<TodayState> = container.ledger.today

    val tenants: LiveData<List<Tenant>> = container.ledger.tenants

    val dateLabel: String = Dates.longDate()

    val monthLabel: String = Dates.monthLabel(Dates.currentMonth()).uppercase(Locale.getDefault())

    val daysLeftLabel: String = run {
        val calendar = Calendar.getInstance()
        val left = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) -
            calendar.get(Calendar.DAY_OF_MONTH)
        when {
            left <= 0 -> "LAST DAY"
            left == 1 -> "1 DAY LEFT"
            else -> "$left DAYS LEFT"
        }
    }

    val greetingName: String = run {
        val configured = container.settings.agentName.trim()
        if (configured.isNotEmpty()) {
            configured.split(" ").firstOrNull().orEmpty()
        } else {
            val email = SessionManager(application).signedInEmail()
            val prefix = email.substringBefore("@").replace(".", " ").trim()
            prefix.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() }.orEmpty()
        }
    }.ifBlank { "there" }
}
