package com.example.propertymanagementsystem.data.ledger

enum class LedgerEntryType { CHARGE, PAYMENT }

/**
 * One line in the tenant's ledger: a charge or a payment, with the balance
 * after it. The ledger screen draws these in date order.
 */
data class LedgerEntry(
    val type: LedgerEntryType,
    val date: String,
    val label: String,
    val amount: Double,
    val balanceAfter: Double,
    val receiptNumber: String = "",
    val method: String = "",
    val reference: String? = null,
    val paymentId: Long = 0
)
