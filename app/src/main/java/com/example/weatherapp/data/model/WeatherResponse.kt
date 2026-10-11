package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

/**
 * Root data model representing the response from OpenWeatherMap Current Weather API:
 * https://api.openweathermap.org/data/2.5/weather
 *
 * All properties are nullable with default values to ensure the app never crashes
 * when the API returns incomplete or unexpected data.
 */
data class WeatherResponse(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("cod")
    val cod: Int? = null,

    @SerializedName("weather")
    val weather: List<WeatherDescription>? = null,

    @SerializedName("main")
    val main: MainWeather? = null,

    @SerializedName("wind")
    val wind: Wind? = null,

    @SerializedName("sys")
    val sys: SysInfo? = null,

    @SerializedName("message")
    val message: String? = null
) {
    /**
     * Returns formatted "City, Country" (e.g., "Pune, IN") or fallback to available name.
     */
    val displayLocation: String
        get() {
            val cityName = name?.takeIf { it.isNotBlank() } ?: "Unknown City"
            val countryCode = sys?.country?.takeIf { it.isNotBlank() }
            return if (countryCode != null) "$cityName, $countryCode" else cityName
        }

    /**
     * Returns formatted temperature in Celsius (e.g. "28°C").
     */
    val displayTemp: String
        get() = main?.temp?.let { "${it.roundToInt()}°C" } ?: "--°C"

    /**
     * Returns feels-like temperature (e.g. "Feels like 30°C").
     */
    val displayFeelsLike: String
        get() = main?.feelsLike?.let { "Feels like ${it.roundToInt()}°C" } ?: "Feels like --°C"

    /**
     * Returns High and Low temperatures (e.g. "H: 31°C  •  L: 24°C").
     */
    val displayMinMax: String
        get() {
            val high = main?.tempMax?.let { "${it.roundToInt()}°C" } ?: "--°C"
            val low = main?.tempMin?.let { "${it.roundToInt()}°C" } ?: "--°C"
            return "H: $high  •  L: $low"
        }

    /**
     * Returns capitalized weather condition description (e.g. "Scattered Clouds").
     */
    val displayCondition: String
        get() {
            val desc = weather?.firstOrNull()?.description
            if (!desc.isNullOrBlank()) {
                return desc.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
            return weather?.firstOrNull()?.main ?: "Clear"
        }

    /**
     * Returns high-resolution icon URL from OpenWeatherMap CDN (4x scale).
     */
    val iconUrl: String?
        get() = weather?.firstOrNull()?.icon?.let { iconCode ->
            "https://openweathermap.org/img/wn/${iconCode}@4x.png"
        }

    /**
     * Returns humidity percentage (e.g. "65%").
     */
    val displayHumidity: String
        get() = main?.humidity?.let { "$it%" } ?: "--%"

    /**
     * Returns wind speed in m/s (e.g. "4.2 m/s").
     */
    val displayWindSpeed: String
        get() = wind?.speed?.let { "%.1f m/s".format(it) } ?: "-- m/s"

    /**
     * Returns atmospheric pressure in hPa (e.g. "1013 hPa").
     */
    val displayPressure: String
        get() = main?.pressure?.let { "$it hPa" } ?: "-- hPa"
}

data class WeatherDescription(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("main")
    val main: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("icon")
    val icon: String? = null
)

data class MainWeather(
    @SerializedName("temp")
    val temp: Double? = null,

    @SerializedName("feels_like")
    val feelsLike: Double? = null,

    @SerializedName("temp_min")
    val tempMin: Double? = null,

    @SerializedName("temp_max")
    val tempMax: Double? = null,

    @SerializedName("pressure")
    val pressure: Int? = null,

    @SerializedName("humidity")
    val humidity: Int? = null
)

data class Wind(
    @SerializedName("speed")
    val speed: Double? = null,

    @SerializedName("deg")
    val deg: Int? = null
)

data class SysInfo(
    @SerializedName("country")
    val country: String? = null,

    @SerializedName("sunrise")
    val sunrise: Long? = null,

    @SerializedName("sunset")
    val sunset: Long? = null
)
