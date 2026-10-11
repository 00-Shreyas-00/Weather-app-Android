package com.example.weatherapp

import com.example.weatherapp.data.model.MainWeather
import com.example.weatherapp.data.model.WeatherDescription
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.model.Wind
import com.example.weatherapp.data.repository.WeatherException
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying ViewModel state transitions, error preservation,
 * empty search prevention, and default city loading.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeWeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads weather for default city Pune`() = runTest {
        viewModel = WeatherViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.weather)
        assertEquals("Pune", state.weather?.name)
        assertEquals("Pune", state.currentCity)
        assertNull(state.errorMessage)
    }

    @Test
    fun `empty search does not fetch and sets validation error`() = runTest {
        viewModel = WeatherViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("   ")
        viewModel.searchWeather()

        val state = viewModel.uiState.value
        assertEquals("Please enter a city name to search.", state.errorMessage)
        // Weather remains unchanged
        assertEquals("Pune", state.weather?.name)
    }

    @Test
    fun `successful city search updates weather and city name`() = runTest {
        viewModel = WeatherViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Mumbai")
        viewModel.searchWeather()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.weather)
        assertEquals("Mumbai", state.weather?.name)
        assertEquals("Mumbai", state.currentCity)
        assertNull(state.errorMessage)
    }

    @Test
    fun `failed subsequent search keeps last successful weather visible`() = runTest {
        viewModel = WeatherViewModel(fakeRepository)
        advanceUntilIdle()

        // Verify initial success
        assertNotNull(viewModel.uiState.value.weather)
        assertEquals("Pune", viewModel.uiState.value.weather?.name)

        // Now search for an invalid city that returns CityNotFoundException
        fakeRepository.shouldReturnError = true
        viewModel.onSearchQueryChanged("InvalidCity123")
        viewModel.searchWeather()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // CRITICAL REQUIREMENT VERIFICATION:
        // The last successfully displayed weather information is STILL VISIBLE!
        assertNotNull("Weather data should not be wiped on subsequent search failure", state.weather)
        assertEquals("Pune", state.weather?.name)
        assertNotNull("Error message should be shown to user", state.errorMessage)
        assertTrue(state.errorMessage!!.contains("InvalidCity123"))
    }

    @Test
    fun `dismissErrorMessage clears the active error`() = runTest {
        viewModel = WeatherViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("")
        viewModel.searchWeather()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.dismissErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    /**
     * Fake implementation of [WeatherRepository] for predictable testing.
     */
    private class FakeWeatherRepository : WeatherRepository {
        var shouldReturnError: Boolean = false

        override fun isApiKeyConfigured(): Boolean = true

        override suspend fun getWeather(cityName: String): Result<WeatherResponse> {
            if (shouldReturnError) {
                return Result.failure(WeatherException.CityNotFoundException(cityName))
            }

            return Result.success(
                WeatherResponse(
                    id = 1,
                    name = cityName,
                    main = MainWeather(
                        temp = 28.0,
                        feelsLike = 29.5,
                        tempMin = 26.0,
                        tempMax = 31.0,
                        pressure = 1012,
                        humidity = 60
                    ),
                    weather = listOf(
                        WeatherDescription(
                            id = 800,
                            main = "Clear",
                            description = "clear sky",
                            icon = "01d"
                        )
                    ),
                    wind = Wind(speed = 3.5, deg = 180)
                )
            )
        }
    }
}
