package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A property that the landlord rents out.
 *
 * Every other record in the app hangs off a property. A tenant lives in one,
 * payments are made against one and maintenance requests are raised for one.
 * Keeping the property as the starting point makes it easy to see everything
 * that belongs together.
 */
@Entity(tableName = "properties")
data class Property(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // What the landlord calls the place, for example "Kileleshwa Apartments".
    val name: String,

    // The street and area, this is also what gets sent to the weather API.
    val address: String,

    // One of the TYPE_ values below.
    val type: String,

    // The rent in Kenya shillings. A Double is used so the dashboard can add
    // up several payments without converting to cents first.
    val rent: Double,

    // One of the STATUS_ values below.
    val status: String,

    // Filled in by the weather API after the address has been looked up.
    // They start at 0 because a property can be saved before the lookup.
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // When the property was added, kept as a time stamp in milliseconds.
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_APARTMENT = "Apartment"
        const val TYPE_HOUSE = "House"
        const val TYPE_SHOP = "Shop"
        const val TYPE_OFFICE = "Office"

        const val STATUS_VACANT = "Vacant"
        const val STATUS_OCCUPIED = "Occupied"
        const val STATUS_MAINTENANCE = "Under maintenance"

        // The types shown in the drop down on the property form.
        val allTypes = listOf(TYPE_APARTMENT, TYPE_HOUSE, TYPE_SHOP, TYPE_OFFICE)

        // The states a property can be in.
        val allStatuses = listOf(STATUS_VACANT, STATUS_OCCUPIED, STATUS_MAINTENANCE)
    }
}
