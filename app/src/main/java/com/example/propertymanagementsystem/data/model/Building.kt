package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A building that an agent manages.
 *
 * A building belongs to one owner and holds many units. The agent's whole
 * business hangs off this table: units, tenants, payments, expenses and
 * owner statements all roll up to a building.
 */
@Entity(tableName = "buildings")
data class Building(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // The name the agent uses, for example "Kasarani Bedsitters".
    val name: String,

    // The street and area. This is also the address used for the weather.
    val address: String,

    // One of the TYPE_ values below.
    val type: String,

    // The owner the agent reports to.
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String,

    // The agent's fee for this building, as a percentage of rent collected.
    val managementFee: Double = 10.0,

    val notes: String = "",

    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_APARTMENT_BLOCK = "Apartment block"
        const val TYPE_BEDSITTER_BLOCK = "Bedsitter block"
        const val TYPE_MIXED_USE = "Mixed use"
        const val TYPE_COMMERCIAL = "Commercial"

        val allTypes = listOf(
            TYPE_APARTMENT_BLOCK,
            TYPE_BEDSITTER_BLOCK,
            TYPE_MIXED_USE,
            TYPE_COMMERCIAL
        )
    }
}
