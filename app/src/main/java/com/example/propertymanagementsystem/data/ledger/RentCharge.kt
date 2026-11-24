package com.example.propertymanagementsystem.data.ledger

/**
 * One month of rent that the tenant has been charged.
 *
 * The charge is derived from the tenancy: the rent and the start (and maybe
 * end) date. It is not stored as a row, so there is no second copy of the
 * truth that can disagree with the tenancy.
 */
data class RentCharge(
    val month: String,
    val amount: Double,
    val dueDate: String,
    val paid: Double = 0.0
) {
    val remaining: Double get() = (amount - paid).coerceAtLeast(0.0)
    val isPaid: Boolean get() = remaining <= 0.005
}
