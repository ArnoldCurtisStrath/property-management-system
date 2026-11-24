package com.example.propertymanagementsystem.ui.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates
import kotlinx.coroutines.launch

class MoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container

    val today: LiveData<TodayState> = container.ledger.today

    val payments: LiveData<List<Payment>> = container.ledger.payments

    val tenants: LiveData<List<Tenant>> = container.ledger.tenants

    val ledgers: LiveData<List<TenantLedger>> = container.ledger.ledgers

    val expenses: LiveData<List<Expense>> =
        container.expenses.getForMonth(Dates.currentMonth())

    val buildings: LiveData<List<Building>> = container.buildings.getAll()

    val month: String = Dates.currentMonth()

    fun deletePayment(payment: Payment) {
        viewModelScope.launch { container.ledger.deletePayment(payment) }
    }
}
