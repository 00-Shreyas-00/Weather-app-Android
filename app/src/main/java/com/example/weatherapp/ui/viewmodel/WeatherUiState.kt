package com.example.weatherapp.ui.viewmodel

import com.example.weatherapp.data.model.WeatherResponse

/**
 * Represents the complete UI state for the Weather screen.
 *
 * @param isLoading Whether a weather request is currently in progress.
 * @param weather The last successfully loaded weather data.
 *                Retained even when subsequent searches fail to satisfy the requirement:
 *                "Keep the last successfully displayed weather information visible when a subsequent search fails."
 * @param currentCity Name of the city currently being displayed (defaults to "Pune").
 * @param searchQuery Current text entered in the city search input field.
 * @param errorMessage User-facing error message, if an operation failed.
 * @param isApiKeyMissing Indicates whether the OpenWeatherMap API key is unconfigured.
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val currentCity: String = DEFAULT_CITY,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isApiKeyMissing: Boolean = false
) {
    companion object {
        const val DEFAULT_CITY = "Pune"
    }
}
