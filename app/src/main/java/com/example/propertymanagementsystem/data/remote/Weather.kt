package com.example.propertymanagementsystem.data.remote

/**
 * The weather that was found for a property.
 */
data class Weather(
    // The name of the place the API found for the address.
    val place: String,

    // The temperature in degrees celsius.
    val temperature: Double,

    // The weather in words, for example "Partly cloudy".
    val description: String
)
