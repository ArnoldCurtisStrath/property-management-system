package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * Everything the app knows about one tenant's rent account.
 *
 * The balance is positive when the tenant owes money and negative when the
 * tenant has paid ahead. The ageing is based on the oldest unpaid month, not
 * on the most recent one.
 */
data class TenantLedger(
    val tenant: Tenant,
    val unit: Property?,
    val building: Building?,
    val charges: List<RentCharge>,
    val payments: List<Payment>,
    val entries: List<LedgerEntry>,
    val balance: Double,
    val credit: Double,
    val totalCharged: Double,
    val totalPaid: Double,
    val oldestUnpaidMonth: String?,
    val daysLate: Int,
    val agingBucket: String
) {
    val owes: Boolean get() = balance > 0.005
    val isCredit: Boolean get() = balance < -0.005
    val isSettled: Boolean get() = !owes && !isCredit

    val statusLabel: String
        get() = when {
            isCredit -> "Credit"
            owes -> agingBucket
            else -> "Paid"
        }

    val unitLabel: String
        get() = unit?.name ?: "No unit"

    val buildingLabel: String
        get() = building?.name ?: "No building"
}
