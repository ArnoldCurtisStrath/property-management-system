package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A person who stays in one of the properties.
 *
 * The tenant keeps a link to the property so the app can show who lives where
 * and can work out the rent that is still outstanding for each house.
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

    // The property this tenant lives in.
    val propertyId: Long,

    // The rent this tenant agreed to pay. It is copied here on purpose so a
    // tenant can have a different amount from the listed rent of the house.
    val rent: Double,

    // The day the tenant moved in, stored as yyyy-MM-dd.
    val startDate: String,

    // Set to false when the tenant leaves. The record is kept because the
    // payments that were made while they were there are still needed.
    val active: Boolean = true
)
