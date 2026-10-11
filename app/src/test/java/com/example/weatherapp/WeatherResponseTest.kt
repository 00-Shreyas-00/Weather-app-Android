package com.example.weatherapp

import com.example.weatherapp.data.model.MainWeather
import com.example.weatherapp.data.model.SysInfo
import com.example.weatherapp.data.model.WeatherDescription
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.model.Wind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests ensuring [WeatherResponse] formatters operate accurately and handle null/incomplete fields gracefully.
 */
class WeatherResponseTest {

    @Test
    fun `formatters produce expected outputs with full data`() {
        val response = WeatherResponse(
            name = "Pune",
            sys = SysInfo(country = "IN"),
            main = MainWeather(
                temp = 28.4,
                feelsLike = 30.1,
                tempMin = 22.0,
                tempMax = 32.0,
                pressure = 1012,
                humidity = 65
            ),
            weather = listOf(
                WeatherDescription(
                    main = "Clouds",
                    description = "scattered clouds",
                    icon = "03d"
                )
            ),
            wind = Wind(speed = 4.2)
        )

        assertEquals("Pune, IN", response.displayLocation)
        assertEquals("28°C", response.displayTemp)
        assertEquals("Feels like 30°C", response.displayFeelsLike)
        assertEquals("H: 32°C  •  L: 22°C", response.displayMinMax)
        assertEquals("Scattered Clouds", response.displayCondition)
        assertEquals("https://openweathermap.org/img/wn/03d@4x.png", response.iconUrl)
        assertEquals("65%", response.displayHumidity)
        assertEquals("4.2 m/s", response.displayWindSpeed)
        assertEquals("1012 hPa", response.displayPressure)
    }

    @Test
    fun `formatters never throw on completely null response data`() {
        val emptyResponse = WeatherResponse()

        assertEquals("Unknown City", emptyResponse.displayLocation)
        assertEquals("--°C", emptyResponse.displayTemp)
        assertEquals("Feels like --°C", emptyResponse.displayFeelsLike)
        assertEquals("H: --°C  •  L: --°C", emptyResponse.displayMinMax)
        assertEquals("Clear", emptyResponse.displayCondition)
        assertNull(emptyResponse.iconUrl)
        assertEquals("--%", emptyResponse.displayHumidity)
        assertEquals("-- m/s", emptyResponse.displayWindSpeed)
        assertEquals("-- hPa", emptyResponse.displayPressure)
    }
}
