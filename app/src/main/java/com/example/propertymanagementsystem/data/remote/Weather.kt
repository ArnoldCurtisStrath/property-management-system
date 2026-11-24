package com.example.propertymanagementsystem.data.remote

/**
 * The weather that was found for a property, including the next few days.
 */
data class Weather(
    val place: String,
    val temperature: Double,
    val description: String,
    val days: List<WeatherDay> = emptyList()
) {
    val rainLikely: Boolean get() = days.any { it.rainChance >= 60 }
}

data class WeatherDay(
    val date: String,
    val maxTemperature: Double,
    val rainChance: Int,
    val description: String
)
