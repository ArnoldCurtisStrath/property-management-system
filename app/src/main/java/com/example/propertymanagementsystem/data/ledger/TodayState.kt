package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Payment

/**
 * The numbers on the Today screen.
 *
 * It is deliberately about decisions, not just counts: what came in, what is
 * still owed, where the vacancies are and what needs a visit.
 */
data class TodayState(
    val month: String,
    val buildingCount: Int,
    val unitCount: Int,
    val occupiedCount: Int,
    val vacantCount: Int,
    val rentRoll: Double,
    val expected: Double,
    val collected: Double,
    val cashCollected: Double,
    val mpesaCollected: Double,
    val receiptCount: Int,
    val outstanding: Double,
    val collectionRate: Double,
    val arrearsCount: Int,
    val arrearsAmount: Double,
    val vacancyLoss: Double,
    val openRepairs: Int,
    val urgentRepairs: Int,
    val expensesThisMonth: Double,
    val netThisMonth: Double,
    val recentReceipts: List<Payment>,
    val topDebtors: List<TenantLedger>,
    val buildings: List<BuildingSummary>
) {
    val collectedPercent: Int
        get() = (collectionRate * 100).toInt().coerceIn(0, 100)

    val hasData: Boolean get() = buildingCount > 0 || unitCount > 0
}
