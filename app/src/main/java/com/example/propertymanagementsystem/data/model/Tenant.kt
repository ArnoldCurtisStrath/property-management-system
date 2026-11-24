package com.example.propertymanagementsystem.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A person who rents a unit.
 *
 * The tenant keeps a link to the unit so the ledger can work out what has
 * been charged, what has been paid and what is still owed.
 */
@Entity(
    tableName = "tenants",
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("propertyId")]
)
data class Tenant(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    // Kept as text so a landlord can type numbers with spaces or a plus sign.
    val phone: String,

    val email: String,

    // The unit this tenant lives in.
    val propertyId: Long,

    // The rent this tenant agreed to pay. It is copied here on purpose so a
    // tenant can have a different amount from the listed rent of the unit.
    val rent: Double,

    // The day the tenant moved in, stored as yyyy-MM-dd.
    val startDate: String,

    // The day the tenant moved out. Null means the tenancy is still running.
    val endDate: String? = null,

    // The day of the month rent is due, from 1 to 28.
    @ColumnInfo(defaultValue = "1")
    val dueDay: Int = 1,

    // Deposit held for the unit.
    @ColumnInfo(defaultValue = "0")
    val deposit: Double = 0.0,

    val notes: String? = null,

    // Set to false when the tenant leaves. The record is kept because the
    // payments that were made while they were there are still needed.
    val active: Boolean = true
)
