package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building

data class ExpenseLine(
    val category: String,
    val amount: Double
)

/**
 * The document the agent sends to the owner.
 *
 * It answers the owner's three questions: what came in, what went out, and
 * what is left. Arrears and vacancy loss are shown separately so the owner
 * can see that they are not being hidden inside the net figure.
 */
data class Statement(
    val building: Building,
    val month: String,
    val statementNumber: String,
    val unitCount: Int,
    val occupiedCount: Int,
    val tenantCount: Int,
    val rentRoll: Double,
    val expected: Double,
    val collected: Double,
    val arrears: Double,
    val vacancyLoss: Double,
    val expenses: Double,
    val expensesByCategory: List<ExpenseLine>,
    val managementFeePercent: Double,
    val managementFee: Double,
    val netToOwner: Double,
    val cashCollected: Double,
    val mpesaCollected: Double,
    val bankCollected: Double,
    val openRepairs: Int,
    val preparedFor: String,
    val preparedBy: String,
    val generatedAt: String
) {
    val collectedPercent: Int
        get() = if (expected <= 0.0) 0 else ((collected / expected) * 100).toInt().coerceIn(0, 100)
}
