package com.example.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.repository.WeatherException
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing weather screen state, search interactions,
 * refreshing, and error handling.
 */
class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            isApiKeyMissing = !repository.isApiKeyConfigured()
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Fetch weather for default city (Pune) when the app launches
        fetchWeather(WeatherUiState.DEFAULT_CITY)
    }

    /**
     * Updates the current search input text.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    /**
     * Initiates a weather search using the text currently in [uiState.searchQuery].
     * Prevents empty or whitespace-only searches.
     */
    fun searchWeather() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a city name to search.")
            }
            return
        }
        fetchWeather(query)
    }

    /**
     * Refreshes weather data for the currently selected city.
     */
    fun refreshCurrentCity() {
        fetchWeather(_uiState.value.currentCity)
    }

    /**
     * Clears the current search text input.
     */
    fun clearSearchQuery() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    /**
     * Dismisses the active error message.
     */
    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Internal method to fetch weather from the repository.
     * Keeps the last successfully fetched weather data intact if the subsequent search fails.
     */
    private fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = repository.getWeather(cityName)

            result.fold(
                onSuccess = { weatherResponse ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            weather = weatherResponse,
                            currentCity = weatherResponse.name ?: cityName,
                            errorMessage = null,
                            isApiKeyMissing = false
                        )
                    }
                },
                onFailure = { error ->
                    val isApiKeyError = error is WeatherException.MissingApiKeyException ||
                            error is WeatherException.InvalidApiKeyException

                    // Notice: current.weather is preserved so previous weather data remains visible!
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to fetch weather data.",
                            isApiKeyMissing = isApiKeyError
                        )
                    }
                }
            )
        }
    }

    /**
     * Factory class to instantiate [WeatherViewModel] with custom dependencies if needed.
     */
    companion object {
        fun provideFactory(
            repository: WeatherRepository = WeatherRepositoryImpl()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(repository) as T
            }
        }
    }
}
