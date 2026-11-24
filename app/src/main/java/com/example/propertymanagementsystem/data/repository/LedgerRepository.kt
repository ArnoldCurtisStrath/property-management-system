package com.example.propertymanagementsystem.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import androidx.room.withTransaction
import com.example.propertymanagementsystem.data.ledger.BuildingSummary
import com.example.propertymanagementsystem.data.ledger.LedgerCalculator
import com.example.propertymanagementsystem.data.ledger.PortfolioCalculator
import com.example.propertymanagementsystem.data.ledger.ReceiptData
import com.example.propertymanagementsystem.data.ledger.Statement
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The one place that turns raw Room rows into the answers the agent needs.
 *
 * Every screen observes this repository instead of talking to a DAO. The
 * arithmetic lives in LedgerCalculator and PortfolioCalculator, which are
 * pure Kotlin and unit-tested.
 */
class LedgerRepository(
    private val database: AppDatabase,
    private val settings: SettingsRepository
) {

    private val buildingDao = database.buildingDao()
    private val propertyDao = database.propertyDao()
    private val tenantDao = database.tenantDao()
    private val paymentDao = database.paymentDao()
    private val expenseDao = database.expenseDao()
    private val maintenanceDao = database.maintenanceDao()

    val buildings: LiveData<List<Building>> = buildingDao.getAll()
    val units: LiveData<List<Property>> = propertyDao.getAll()
    val tenants: LiveData<List<Tenant>> = tenantDao.getAll()
    val payments: LiveData<List<Payment>> = paymentDao.getAll()
    val expenses: LiveData<List<Expense>> = expenseDao.getAll()
    val maintenance: LiveData<List<MaintenanceRequest>> = maintenanceDao.getAll()

    private val ledgersLive = MediatorLiveData<List<TenantLedger>>()
    private val todayLive = MediatorLiveData<TodayState>()

    init {
        listOf(buildings, units, tenants, payments).forEach { source ->
            ledgersLive.addSource(source) { recomputeLedgers() }
        }
        listOf(ledgersLive, buildings, units, tenants, payments, expenses, maintenance)
            .forEach { source ->
                todayLive.addSource(source) { recomputeToday() }
            }
    }

    val ledgers: LiveData<List<TenantLedger>> get() = ledgersLive

    val today: LiveData<TodayState> get() = todayLive

    val buildingSummaries: LiveData<List<BuildingSummary>> =
        todayLive.map { it.buildings }

    private fun recomputeLedgers() {
        val buildings = buildings.value ?: return
        val units = units.value ?: return
        val tenants = tenants.value ?: return
        val payments = payments.value ?: return

        ledgersLive.value = LedgerCalculator.buildLedgers(
            buildings = buildings,
            units = units,
            tenants = tenants,
            payments = payments
        )
    }

    private fun recomputeToday() {
        val buildings = buildings.value ?: return
        val units = units.value ?: return
        val tenants = tenants.value ?: return
        val payments = payments.value ?: return
        val expenses = expenses.value ?: return
        val maintenance = maintenance.value ?: return
        val ledgers = ledgersLive.value ?: return

        todayLive.value = PortfolioCalculator.today(
            buildings = buildings,
            units = units,
            tenants = tenants,
            payments = payments,
            expenses = expenses,
            maintenance = maintenance,
            ledgers = ledgers
        )
    }

    // ---------------------------------------------------------------------
    // Reads that are needed once, for example when a screen is opened.
    // ---------------------------------------------------------------------

    suspend fun tenantLedgerOnce(tenantId: Long): TenantLedger? = withContext(Dispatchers.IO) {
        val tenant = tenantDao.getOnce(tenantId) ?: return@withContext null
        val unit = propertyDao.getOnce(tenant.propertyId)
        val building = unit?.let { buildingDao.getOnce(it.buildingId) }
        val tenantPayments = paymentDao.getForTenantOnce(tenantId)
        LedgerCalculator.ledgerFor(tenant, unit, building, tenantPayments)
    }

    suspend fun receiptData(paymentId: Long): ReceiptData? = withContext(Dispatchers.IO) {
        val payment = paymentDao.getOnce(paymentId) ?: return@withContext null
        val tenant = tenantDao.getOnce(payment.tenantId)
        val unit = propertyDao.getOnce(payment.propertyId)
        val building = unit?.let { buildingDao.getOnce(it.buildingId) }
        if (tenant == null) return@withContext null
        ReceiptData(payment, tenant, unit, building)
    }

    suspend fun statementOnce(buildingId: Long, month: String): Statement? =
        withContext(Dispatchers.IO) {
            val building = buildingDao.getOnce(buildingId) ?: return@withContext null
            val units = propertyDao.getForBuildingOnce(buildingId)
            val unitIds = units.map { it.id }.toSet()
            val tenants = tenantDao.getAllOnce().filter { it.propertyId in unitIds }
            val allPayments = paymentDao.getAllOnce()
            val paymentsUpToMonth = allPayments.filter { it.date <= "$month-31" }
            val expenses = expenseDao.getForMonthOnce(month)
            val maintenance = maintenanceDao.getAllOnce()
            val ledgers = LedgerCalculator.buildLedgers(
                buildings = listOf(building),
                units = units,
                tenants = tenants,
                payments = paymentsUpToMonth,
                asOfMonth = month
            )
            PortfolioCalculator.statement(
                building = building,
                units = units,
                tenants = tenants,
                payments = paymentsUpToMonth,
                expenses = expenses,
                maintenance = maintenance,
                ledgers = ledgers,
                month = month,
                preparedBy = settings.preparedBy()
            )
        }

    suspend fun snapshotToday(): TodayState = withContext(Dispatchers.IO) {
        val buildings = buildingDao.getAllOnce()
        val units = propertyDao.getAllOnce()
        val tenants = tenantDao.getAllOnce()
        val payments = paymentDao.getAllOnce()
        val expenses = expenseDao.getAllOnce()
        val maintenance = maintenanceDao.getAllOnce()
        val ledgers = LedgerCalculator.buildLedgers(buildings, units, tenants, payments)
        PortfolioCalculator.today(
            buildings = buildings,
            units = units,
            tenants = tenants,
            payments = payments,
            expenses = expenses,
            maintenance = maintenance,
            ledgers = ledgers
        )
    }

    // ---------------------------------------------------------------------
    // Writes
    // ---------------------------------------------------------------------

    /**
     * Records a payment and numbers its receipt.
     *
     * The balance after the payment is worked out before the row is written
     * and stored on the row, so a re-printed receipt never changes.
     */
    suspend fun recordPayment(
        tenantId: Long,
        amount: Double,
        date: String,
        method: String,
        reference: String?,
        note: String?,
        recordedBy: String
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val tenant = tenantDao.getOnce(tenantId)
                ?: throw IllegalStateException("The tenant could not be found")
            val unit = propertyDao.getOnce(tenant.propertyId)
            val existingPayments = paymentDao.getForTenantOnce(tenantId)
            val before = LedgerCalculator.ledgerFor(
                tenant = tenant,
                unit = unit,
                building = null,
                payments = existingPayments,
                asOfMonth = Dates.monthOf(date),
                asOfDate = date
            ).balance

            val payment = Payment(
                tenantId = tenant.id,
                propertyId = tenant.propertyId,
                buildingId = unit?.buildingId ?: 0L,
                amount = amount,
                date = date,
                month = Dates.monthOf(date),
                method = method,
                status = Payment.STATUS_PAID,
                receiptNumber = "",
                reference = reference?.trim()?.ifBlank { null },
                note = note?.trim()?.ifBlank { null },
                recordedBy = recordedBy.trim().ifBlank { "Me" },
                balanceAfter = LedgerCalculator.round2(before - amount),
                createdAt = System.currentTimeMillis()
            )

            val id = paymentDao.insert(payment)
            val year = date.take(4).ifBlank { Dates.currentMonth().take(4) }
            val receiptNumber = settings.receiptPrefix
                .ifBlank { "SH" } + "-" + year + "-" + id.toString().padStart(4, '0')
            paymentDao.update(payment.copy(id = id, receiptNumber = receiptNumber))
            id
        }
    }

    suspend fun deletePayment(payment: Payment) = withContext(Dispatchers.IO) {
        paymentDao.delete(payment)
    }

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        expenseDao.insert(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.delete(expense)
    }

    suspend fun addMaintenance(request: MaintenanceRequest): Long = withContext(Dispatchers.IO) {
        maintenanceDao.insert(request)
    }

    suspend fun updateMaintenance(request: MaintenanceRequest) = withContext(Dispatchers.IO) {
        maintenanceDao.update(request)
    }
}
