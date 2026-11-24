package com.example.propertymanagementsystem.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Talks to the Open-Meteo API to find the weather at a building.
 *
 * The API is free and does not need a key. The address is first turned into a
 * latitude and a longitude, and those are then used to ask for the current
 * weather and a three-day forecast. The forecast is what makes the card
 * useful: it tells the agent when rain is likely to reach an open repair.
 */
class WeatherApi {

    private data class Location(
        val latitude: Double,
        val longitude: Double,
        val name: String
    )

    fun load(address: String): Weather? {
        val location = findLocation(address) ?: return null
        return findWeather(location)
    }

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

    private fun findWeather(location: Location): Weather? {
        val link = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${location.latitude}" +
            "&longitude=${location.longitude}" +
            "&current=temperature_2m,weather_code" +
            "&daily=weather_code,temperature_2m_max,precipitation_probability_max" +
            "&forecast_days=3&timezone=auto"

        val answer = read(URL(link)) ?: return null
        val json = JSONObject(answer)
        val current = json.optJSONObject("current") ?: return null
        val temperature = current.optDouble("temperature_2m", 0.0)
        val code = current.optInt("weather_code", 0)

        val days = mutableListOf<WeatherDay>()
        val daily = json.optJSONObject("daily")
        if (daily != null) {
            val dates = daily.optJSONArray("time")
            val codes = daily.optJSONArray("weather_code")
            val maxima = daily.optJSONArray("temperature_2m_max")
            val rain = daily.optJSONArray("precipitation_probability_max")
            if (dates != null) {
                for (index in 0 until dates.length()) {
                    days.add(
                        WeatherDay(
                            date = dates.optString(index, ""),
                            maxTemperature = maxima?.optDouble(index, 0.0) ?: 0.0,
                            rainChance = rain?.optInt(index, 0) ?: 0,
                            description = describe(codes?.optInt(index, 0) ?: 0)
                        )
                    )
                }
            }
        }

        return Weather(
            place = location.name,
            temperature = temperature,
            description = describe(code),
            days = days
        )
    }

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
            null
        } finally {
            connection?.disconnect()
        }
    }

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
