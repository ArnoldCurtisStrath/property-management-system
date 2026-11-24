package com.example.propertymanagementsystem.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A problem that needs to be fixed in one of the units.
 *
 * A request moves through three states. It starts as OPEN when the tenant
 * reports it, becomes IN PROGRESS when someone is working on it and ends as
 * DONE when it has been sorted out. A completed request can carry a cost,
 * which becomes an expense for the owner's statement.
 */
@Entity(
    tableName = "maintenance",
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
data class MaintenanceRequest(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // The unit that has the problem.
    val propertyId: Long,

    // A short summary, for example "Kitchen tap leaking".
    val title: String,

    // The full description of what is wrong.
    val description: String,

    // One of the CATEGORY_ values below.
    val category: String,

    // One of the PRIORITY_ values below.
    @ColumnInfo(defaultValue = "'Normal'")
    val priority: String = PRIORITY_NORMAL,

    // One of the STATUS_ values below.
    val status: String,

    // The day the request was raised, stored as yyyy-MM-dd.
    val reportedAt: String,

    // The day it was closed. It stays empty while the request is still open.
    val resolvedAt: String? = null,

    // The cost of the repair, filled in when it is done.
    @ColumnInfo(defaultValue = "0")
    val cost: Double = 0.0,

    val vendor: String? = null
) {
    companion object {
        const val CATEGORY_PLUMBING = "Plumbing"
        const val CATEGORY_ELECTRICAL = "Electrical"
        const val CATEGORY_STRUCTURAL = "Structural"
        const val CATEGORY_OTHER = "Other"

        const val PRIORITY_LOW = "Low"
        const val PRIORITY_NORMAL = "Normal"
        const val PRIORITY_HIGH = "High"
        const val PRIORITY_URGENT = "Urgent"

        const val STATUS_OPEN = "Open"
        const val STATUS_IN_PROGRESS = "In progress"
        const val STATUS_DONE = "Done"

        val allCategories = listOf(
            CATEGORY_PLUMBING,
            CATEGORY_ELECTRICAL,
            CATEGORY_STRUCTURAL,
            CATEGORY_OTHER
        )

        val allPriorities = listOf(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT)

        val allStatuses = listOf(STATUS_OPEN, STATUS_IN_PROGRESS, STATUS_DONE)
    }
}
