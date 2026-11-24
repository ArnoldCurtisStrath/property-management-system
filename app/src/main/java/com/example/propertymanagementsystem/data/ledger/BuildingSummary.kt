package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building

/**
 * The one-screen summary of a building: what it should earn, what it did
 * earn, what is still owed, what was spent and what the owner should get.
 */
data class BuildingSummary(
    val building: Building,
    val unitCount: Int,
    val occupiedCount: Int,
    val vacantCount: Int,
    val tenantCount: Int,
    val rentRoll: Double,
    val expected: Double,
    val collected: Double,
    val arrears: Double,
    val arrearsCount: Int,
    val vacancyLoss: Double,
    val expenses: Double,
    val managementFee: Double,
    val netToOwner: Double,
    val openRepairs: Int,
    val occupancyRate: Double
) {
    val arrearsRate: Double
        get() = if (expected <= 0.0) 0.0 else (arrears / expected).coerceIn(0.0, 1.0)
}
