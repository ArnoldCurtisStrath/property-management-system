package com.example.propertymanagementsystem.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Talks to the Open-Meteo API to find the weather at a property.
 *
 * The API is free and does not need a key. It is used in two steps. The
 * address of the property is first turned into a latitude and a longitude,
 * and those are then used to ask for the current weather at that point.
 *
 * The class only uses the classes that come with Android, HttpURLConnection
 * for the request and the JSON reader for the answer. That keeps the app
 * light and there is no extra library to set up.
 */
class WeatherApi {

    // The place that was found for an address.
    private data class Location(
        val latitude: Double,
        val longitude: Double,
        val name: String
    )

    /**
     * Finds the weather at the address that is given.
     *
     * Returns null when the address cannot be found or when the network is
     * not available. The caller shows a message when that happens.
     */
    fun load(address: String): Weather? {
        val location = findLocation(address) ?: return null
        val current = findWeather(location) ?: return null
        return Weather(
            place = location.name,
            temperature = current.first,
            description = describe(current.second)
        )
    }

    // Turns the address of a property into a latitude and a longitude.
    private fun findLocation(address: String): Location? {
        val link = "https://geocoding-api.open-meteo.com/v1/search?name=" +
            URLEncoder.encode(address, "UTF-8") +
            "&count=1&language=en&format=json"

        val answer = read(URL(link)) ?: return null

        val results = JSONObject(answer).optJSONArray("results") ?: return null
        if (results.length() == 0) return null

        val first = results.getJSONObject(0)
        return Location(
            latitude = first.optDouble("latitude", 0.0),
            longitude = first.optDouble("longitude", 0.0),
            name = first.optString("name", address)
        )
    }

    // Asks for the weather at a point on the map.
    private fun findWeather(location: Location): Pair<Double, Int>? {
        val link = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${location.latitude}" +
            "&longitude=${location.longitude}" +
            "&current=temperature_2m,weather_code" +
            "&timezone=auto"

        val answer = read(URL(link)) ?: return null

        val current = JSONObject(answer).optJSONObject("current") ?: return null
        val temperature = current.optDouble("temperature_2m", 0.0)
        val code = current.optInt("weather_code", 0)
        return Pair(temperature, code)
    }

    // Reads the answer from the API. It returns null when anything goes
    // wrong, for example when there is no network.
    private fun read(url: URL): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            val reader: BufferedReader = connection.inputStream.bufferedReader()
            reader.use { it.readText() }
        } catch (error: Exception) {
            // A failed request is not a crash, the screen just shows a
            // message that the weather could not be loaded.
            null
        } finally {
            connection?.disconnect()
        }
    }

    // The API sends the weather as a number. This turns it into words that
    // can be shown to the user.
    private fun describe(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        in 51..57 -> "Drizzle"
        in 61..67 -> "Rain"
        in 71..77 -> "Snow"
        in 80..82 -> "Rain showers"
        in 95..99 -> "Thunderstorm"
        else -> "Weather unknown"
    }
}
