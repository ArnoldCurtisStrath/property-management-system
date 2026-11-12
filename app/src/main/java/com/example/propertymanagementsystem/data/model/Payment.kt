package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A rent payment that was recorded against a tenant.
 *
 * The payment keeps the month it was paid for, not only the day it was
 * entered. That is what allows the app to tell whether a tenant has paid
 * for the current month or still owes.
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenantId")]
)
data class Payment(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // The tenant who made the payment.
    val tenantId: Long,

    // The property is also kept here so the portfolio list does not have to
    // look up the tenant first.
    val propertyId: Long,

    val amount: Double,

    // The day the payment was made, stored as yyyy-MM-dd.
    val date: String,

    // The month the payment was for, stored as yyyy-MM.
    val month: String,

    // One of the METHOD_ values below.
    val method: String,

    // PAID when the money was received, PENDING when it was promised.
    val status: String
) {
    companion object {
        const val METHOD_CASH = "Cash"
        const val METHOD_MPESA = "M-Pesa"
        const val METHOD_BANK = "Bank"

        const val STATUS_PAID = "Paid"
        const val STATUS_PENDING = "Pending"

        val allMethods = listOf(METHOD_CASH, METHOD_MPESA, METHOD_BANK)
    }
}
