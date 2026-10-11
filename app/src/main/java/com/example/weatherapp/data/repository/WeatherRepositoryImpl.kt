package com.example.weatherapp.data.repository

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.remote.RetrofitClient
import com.example.weatherapp.data.remote.WeatherApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of [WeatherRepository] that calls OpenWeatherMap API via Retrofit.
 */
class WeatherRepositoryImpl(
    private val apiService: WeatherApiService = RetrofitClient.weatherApiService,
    private val apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }

    override suspend fun getWeather(cityName: String): Result<WeatherResponse> {
        val trimmedCity = cityName.trim()
        if (trimmedCity.isBlank()) {
            return Result.failure(IllegalArgumentException("City name cannot be empty."))
        }

        if (!isApiKeyConfigured()) {
            return Result.failure(WeatherException.MissingApiKeyException())
        }

        return withContext(ioDispatcher) {
            try {
                val response = apiService.getCurrentWeather(
                    cityName = trimmedCity,
                    apiKey = apiKey,
                    units = "metric"
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(WeatherException.UnknownException("Received empty response body from server."))
                    }
                } else {
                    when (response.code()) {
                        404 -> Result.failure(WeatherException.CityNotFoundException(trimmedCity))
                        401 -> Result.failure(WeatherException.InvalidApiKeyException())
                        else -> Result.failure(
                            WeatherException.ApiException(
                                response.code(),
                                response.message().ifBlank { "Request failed with code ${response.code()}" }
                            )
                        )
                    }
                }
            } catch (e: UnknownHostException) {
                Result.failure(
                    WeatherException.NetworkException(
                        "No internet connection. Please verify your Wi-Fi or mobile data."
                    )
                )
            } catch (e: SocketTimeoutException) {
                Result.failure(
                    WeatherException.NetworkException(
                        "Connection timed out. Please check your network and try again."
                    )
                )
            } catch (e: IOException) {
                Result.failure(
                    WeatherException.NetworkException(
                        "Network error: ${e.localizedMessage ?: "Unable to connect to weather service."}"
                    )
                )
            } catch (e: Exception) {
                Result.failure(
                    WeatherException.UnknownException(
                        e.localizedMessage ?: "An unexpected error occurred."
                    )
                )
            }
        }
    }
}
