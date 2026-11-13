package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A problem that needs to be fixed in one of the properties.
 *
 * A request moves through three states. It starts as OPEN when the tenant
 * reports it, becomes IN PROGRESS when someone is working on it and ends as
 * DONE when it has been sorted out.
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

    // The property that has the problem.
    val propertyId: Long,

    // A short summary, for example "Kitchen tap leaking".
    val title: String,

    // The full description of what is wrong.
    val description: String,

    // One of the CATEGORY_ values below.
    val category: String,

    // One of the STATUS_ values below.
    val status: String,

    // The day the request was raised, stored as yyyy-MM-dd.
    val reportedAt: String,

    // The day it was closed. It stays empty while the request is still open.
    val resolvedAt: String? = null
) {
    companion object {
        const val CATEGORY_PLUMBING = "Plumbing"
        const val CATEGORY_ELECTRICAL = "Electrical"
        const val CATEGORY_STRUCTURAL = "Structural"
        const val CATEGORY_OTHER = "Other"

        const val STATUS_OPEN = "Open"
        const val STATUS_IN_PROGRESS = "In progress"
        const val STATUS_DONE = "Done"

        val allCategories = listOf(
            CATEGORY_PLUMBING,
            CATEGORY_ELECTRICAL,
            CATEGORY_STRUCTURAL,
            CATEGORY_OTHER
        )

        val allStatuses = listOf(STATUS_OPEN, STATUS_IN_PROGRESS, STATUS_DONE)
    }
}
