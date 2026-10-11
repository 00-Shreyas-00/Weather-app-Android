package com.example.weatherapp.data.remote

import com.example.weatherapp.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for OpenWeatherMap Current Weather Data endpoint:
 * https://openweathermap.org/current
 */
interface WeatherApiService {

    /**
     * Fetch current weather by city name.
     *
     * @param cityName Name of the city (e.g., "Pune", "London")
     * @param apiKey OpenWeatherMap API key
     * @param units Metric for Celsius, Imperial for Fahrenheit
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
}
