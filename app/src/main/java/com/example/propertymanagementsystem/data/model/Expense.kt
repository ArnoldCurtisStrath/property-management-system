package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Money that left the business.
 *
 * Expenses are the other half of the owner statement. They are kept in one
 * table with a category so the agent can see where the money went, and so
 * the owner can see why the net remittance is lower than the rent collected.
 */
@Entity(
    tableName = "expenses",
    indices = [Index("buildingId"), Index("propertyId"), Index("date")]
)
data class Expense(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val buildingId: Long,

    // Null when the expense belongs to the whole building, for example water.
    val propertyId: Long? = null,

    // One of the CATEGORY_ values below.
    val category: String,

    val amount: Double,

    // The day the money was spent, stored as yyyy-MM-dd.
    val date: String,

    // Who was paid.
    val vendor: String = "",

    // One of the Payment.METHOD_ values.
    val method: String = Payment.METHOD_CASH,

    val reference: String? = null,

    val note: String? = null,

    // Set when the expense came from a completed repair.
    val maintenanceId: Long? = null,

    val recordedBy: String = "Owner",

    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val CATEGORY_REPAIRS = "Repairs"
        const val CATEGORY_WATER = "Water"
        const val CATEGORY_ELECTRICITY = "Electricity"
        const val CATEGORY_SECURITY = "Security"
        const val CATEGORY_GARBAGE = "Garbage"
        const val CATEGORY_CLEANING = "Cleaning"
        const val CATEGORY_PERMITS = "Permits"
        const val CATEGORY_OTHER = "Other"

        val allCategories = listOf(
            CATEGORY_REPAIRS,
            CATEGORY_WATER,
            CATEGORY_ELECTRICITY,
            CATEGORY_SECURITY,
            CATEGORY_GARBAGE,
            CATEGORY_CLEANING,
            CATEGORY_PERMITS,
            CATEGORY_OTHER
        )
    }
}
