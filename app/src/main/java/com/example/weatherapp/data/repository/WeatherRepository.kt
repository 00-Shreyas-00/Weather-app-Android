package com.example.weatherapp.data.repository

import com.example.weatherapp.data.model.WeatherResponse

/**
 * Repository interface defining weather data operations.
 */
interface WeatherRepository {
    /**
     * Fetch weather for a given city name.
     * Returns a [Result] wrapping [WeatherResponse] on success or an appropriate exception on failure.
     */
    suspend fun getWeather(cityName: String): Result<WeatherResponse>

    /**
     * Checks whether an API key has been properly configured in the application.
     */
    fun isApiKeyConfigured(): Boolean
}

/**
 * Domain-specific exceptions for clear, human-readable UI error reporting.
 */
sealed class WeatherException(message: String) : Exception(message) {
    class MissingApiKeyException(
        message: String = "OpenWeatherMap API key is missing. Add your API key to local.properties as OPENWEATHER_API_KEY=your_key."
    ) : WeatherException(message)

    class InvalidApiKeyException(
        message: String = "Invalid or inactive API key. Please check your OpenWeatherMap key in local.properties."
    ) : WeatherException(message)

    class CityNotFoundException(cityName: String) :
        WeatherException("City \"$cityName\" was not found. Please verify the spelling.")

    class NetworkException(
        message: String = "Network connection failed. Please check your internet connection."
    ) : WeatherException(message)

    class ApiException(code: Int, message: String) :
        WeatherException("Server error ($code): $message")

    class UnknownException(
        message: String = "An unexpected error occurred while fetching weather data."
    ) : WeatherException(message)
}
