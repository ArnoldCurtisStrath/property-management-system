package com.example.propertymanagementsystem.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A rent payment that was recorded against a tenant.
 *
 * A payment is never just "paid". It carries the receipt number, the method,
 * the reference, who collected it and the balance after it was posted. Those
 * details are what make the receipt and the day close defensible.
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
    indices = [Index("tenantId"), Index("buildingId")]
)
data class Payment(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tenantId: Long,

    // The unit and the building are kept here so reports do not have to walk
    // through the tenant table for every row.
    val propertyId: Long,

    @ColumnInfo(defaultValue = "0")
    val buildingId: Long = 0,

    val amount: Double,

    // The day the payment was made, stored as yyyy-MM-dd.
    val date: String,

    // The month the payment was for, stored as yyyy-MM.
    val month: String,

    // One of the METHOD_ values below.
    val method: String,

    // PAID when the money was received, PENDING when it was promised.
    val status: String,

    // The printed receipt number, for example SH-2026-0007.
    @ColumnInfo(defaultValue = "''")
    val receiptNumber: String = "",

    // The M-Pesa code or bank reference.
    val reference: String? = null,

    val note: String? = null,

    // Who collected the money: the agent or a caretaker.
    @ColumnInfo(defaultValue = "'Owner'")
    val recordedBy: String = "Owner",

    // The tenant's balance immediately after this payment was posted. It is
    // stored so a re-printed receipt always shows the same figure.
    val balanceAfter: Double? = null,

    @ColumnInfo(defaultValue = "0")
    val createdAt: Long = System.currentTimeMillis()
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
