package com.example.propertymanagementsystem

import android.app.Application
import android.content.Context
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.repository.BuildingRepository
import com.example.propertymanagementsystem.data.repository.ExpenseRepository
import com.example.propertymanagementsystem.data.repository.LedgerRepository
import com.example.propertymanagementsystem.data.repository.MaintenanceRepository
import com.example.propertymanagementsystem.data.repository.PaymentRepository
import com.example.propertymanagementsystem.data.repository.PropertyRepository
import com.example.propertymanagementsystem.data.repository.SettingsRepository
import com.example.propertymanagementsystem.data.repository.TenantRepository

/**
 * One small service locator for the whole app.
 *
 * It is deliberately not a full dependency-injection framework. It keeps the
 * wiring in one readable place and lets every ViewModel ask for a repository
 * without opening the database itself.
 */
class SahihiApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {

    val database: AppDatabase = AppDatabase.getInstance(context)

    val settings = SettingsRepository(context)

    val buildings = BuildingRepository(database.buildingDao())
    val units = PropertyRepository(database.propertyDao())
    val tenants = TenantRepository(database.tenantDao())
    val payments = PaymentRepository(database.paymentDao())
    val expenses = ExpenseRepository(database.expenseDao())
    val maintenance = MaintenanceRepository(database.maintenanceDao())

    val ledger = LedgerRepository(database, settings)
}
