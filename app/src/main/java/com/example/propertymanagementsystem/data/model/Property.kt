package com.example.propertymanagementsystem.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A rentable unit inside a building.
 *
 * The class is still called Property because that is what the first version
 * of the app stored and the database already has the table. In the interface
 * it is called a unit, because that is what an agent calls it.
 */
@Entity(
    tableName = "properties",
    indices = [Index("buildingId")]
)
data class Property(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // The building this unit belongs to.
    @ColumnInfo(defaultValue = "0")
    val buildingId: Long = 0,

    // The unit label, for example "A1" or "Shop 3".
    val name: String,

    // Kept for the weather lookup and for older records. New units copy the
    // address of their building when they are saved.
    val address: String,

    // One of the TYPE_ values below.
    val type: String,

    // The market rent of the unit, in Kenya shillings.
    val rent: Double,

    // One of the STATUS_ values below.
    val status: String,

    // Filled in by the weather API after the address has been looked up.
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_BEDSITTER = "Bedsitter"
        const val TYPE_STUDIO = "Studio"
        const val TYPE_ONE_BEDROOM = "1 Bedroom"
        const val TYPE_TWO_BEDROOM = "2 Bedroom"
        const val TYPE_SHOP = "Shop"
        const val TYPE_OFFICE = "Office"

        // The old type names are kept so records saved by version 1 are not
        // lost when the unit form opens.
        const val TYPE_APARTMENT = "Apartment"
        const val TYPE_HOUSE = "House"

        const val STATUS_VACANT = "Vacant"
        const val STATUS_OCCUPIED = "Occupied"
        const val STATUS_MAINTENANCE = "Under maintenance"

        val allTypes = listOf(
            TYPE_BEDSITTER,
            TYPE_STUDIO,
            TYPE_ONE_BEDROOM,
            TYPE_TWO_BEDROOM,
            TYPE_SHOP,
            TYPE_OFFICE,
            TYPE_APARTMENT,
            TYPE_HOUSE
        )

        val allStatuses = listOf(STATUS_VACANT, STATUS_OCCUPIED, STATUS_MAINTENANCE)
    }
}
