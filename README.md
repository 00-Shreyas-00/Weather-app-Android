# WeatherApp (Android / Kotlin / Jetpack Compose)

A clean, modern, and robust Weather application for Android built using **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM architecture**, **Coroutines**, and **Retrofit with OkHttp**.

The app queries real-time weather information from the **OpenWeatherMap Current Weather API**, displays the weather for a default city on startup (Pune), allows searching any city worldwide, retains prior weather data upon search failures, and features full error and loading state indicators.

---

## 1. Technology Stack

| Component | Library / Framework | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9.24 | Idiomatic, null-safe Kotlin codebase |
| **Minimum SDK** | Android 8.0 (API 26) | Target SDK 34 (Android 14) |
| **UI Toolkit** | Jetpack Compose (Material 3) | Declarative modern UI with adaptive components |
| **Image Loading** | Coil Compose 2.6.0 | Async weather icon loading from OpenWeather CDN |
| **Architecture** | MVVM (Model-View-ViewModel) | Clean separation of UI, business logic, and data layer |
| **Networking** | Retrofit 2.11.0 + OkHttp 4.12.0 | HTTP client with logging interceptor |
| **JSON Parser** | Google Gson 2.11.0 | Null-safe deserialization of API responses |
| **Async / Threading** | Kotlin Coroutines & StateFlow | Reactive asynchronous operations and IO offloading |
| **API** | OpenWeatherMap Current Weather API | `https://api.openweathermap.org/data/2.5/weather` |

---

## 2. Architecture & Data Flow

The project follows the standard Android architectural guidelines recommended by Google:

```
┌────────────────────────────────────────────────────────┐
│                      Compose UI                        │
│   (WeatherScreen, WeatherInfoCard, WeatherSearchBar)   │
└───────────────────────────▲────────────────────────────┘
                            │ Collects StateFlow (WeatherUiState)
                            │ Emits UI Events (Search, Refresh)
┌───────────────────────────┴────────────────────────────┐
│                    WeatherViewModel                    │
│   - Holds UI state (StateFlow<WeatherUiState>)         │
│   - Validates search queries (prevents empty inputs)   │
│   - Retains last valid weather on failure              │
└───────────────────────────▲────────────────────────────┘
                            │ Calls suspend fun getWeather()
                            │ Returns Result<WeatherResponse>
┌───────────────────────────┴────────────────────────────┐
│                   WeatherRepository                    │
│   - Executes network request on Dispatchers.IO         │
│   - Translates HTTP 404, 401, timeouts, and IO errors  │
│     into domain-specific WeatherException types        │
└───────────────────────────▲────────────────────────────┘
                            │ Calls Retrofit interface
┌───────────────────────────┴────────────────────────────┐
│             RetrofitClient & WeatherApiService         │
│   - Calls OpenWeatherMap API: /data/2.5/weather        │
│   - Reads BuildConfig.OPENWEATHER_API_KEY              │
└────────────────────────────────────────────────────────┘
```

### Key Data Flow Steps:
1. **App Launch**: `MainActivity` initiates `WeatherViewModel`, which automatically triggers a fetch for the default city (`Pune`).
2. **Repository Call**: `WeatherRepositoryImpl` checks that an API key is configured, switches execution to `Dispatchers.IO`, and calls `WeatherApiService.getCurrentWeather(cityName, apiKey, "metric")`.
3. **Network Response**:
   - **Success (200 OK)**: Deserialized into `WeatherResponse`. The ViewModel updates `WeatherUiState` with `weather = response`, `isLoading = false`, and `errorMessage = null`.
   - **City Not Found (404)**: Mapped to `WeatherException.CityNotFoundException`. The ViewModel preserves the previously loaded `weather` object and sets `errorMessage = "City \"<name>\" was not found"`.
   - **No Internet (IOException / SocketTimeout)**: Mapped to `WeatherException.NetworkException`. The ViewModel keeps the existing weather visible and informs the user via the error banner.
   - **Missing / Invalid Key (401)**: Mapped to `WeatherException.InvalidApiKeyException` with setup guidance.
4. **UI Rendering**: `WeatherScreen` listens to `uiState` via Compose's `collectAsState()`, rendering the weather hero card, metric chips (humidity, wind, pressure), or an error banner without blanking the screen.

---

## 3. Project Structure

```
d:\android-app\
├── app/
│   ├── build.gradle.kts                # App dependencies, Compose & BuildConfig setup
│   ├── proguard-rules.pro              # Proguard rules for Retrofit & Gson
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml     # INTERNET & network state permissions
│       │   ├── java/com/example/weatherapp/
│       │   │   ├── MainActivity.kt     # App entry point
│       │   │   ├── data/
│       │   │   │   ├── model/
│       │   │   │   │   └── WeatherResponse.kt      # DTOs & display formatters
│       │   │   │   ├── remote/
│       │   │   │   │   ├── WeatherApiService.kt   # Retrofit endpoints
│       │   │   │   │   └── RetrofitClient.kt      # OkHttp & Retrofit instance
│       │   │   │   └── repository/
│       │   │   │       ├── WeatherRepository.kt   # Interface & WeatherException
│       │   │   │       └── WeatherRepositoryImpl.kt# Network call & error handling
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   ├── WeatherSearchBar.kt    # Input with clear & search actions
│       │   │   │   │   ├── WeatherInfoCard.kt     # Main temperature & condition card
│       │   │   │   │   ├── WeatherDetailItem.kt   # Humidity, wind, pressure cards
│       │   │   │   │   └── WeatherErrorBanner.kt  # Dismissable error alerts
│       │   │   │   ├── screen/
│       │   │   │   │   └── WeatherScreen.kt       # Scaffold, top bar, main column
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt               # Sky blue & slate light theme
│       │   │   │   │   ├── Theme.kt               # MaterialTheme configuration
│       │   │   │   │   └── Type.kt                # Typography styles
│       │   │   │   └── viewmodel/
│       │   │   │       ├── WeatherUiState.kt      # State model (retains weather)
│       │   │   │       └── WeatherViewModel.kt    # State management & coroutines
│       │   └── res/
│       │       ├── values/
│       │       │   ├── strings.xml            # Accessible strings & labels
│       │       │   └── colors.xml
│       │       ├── drawable/
│       │       │   ├── ic_launcher_background.xml
│       │       │   └── ic_launcher_foreground.xml
│       │       └── mipmap-anydpi-v26/
│       │           ├── ic_launcher.xml
│       │           └── ic_launcher_round.xml
│       └── test/java/com/example/weatherapp/
│           ├── WeatherViewModelTest.kt         # Coroutine & state transition tests
│           └── WeatherResponseTest.kt          # Formatting & null-safety tests
├── build.gradle.kts                    # Root build script
├── settings.gradle.kts                 # Project & repository configurations
├── gradle.properties                   # JVM args & AndroidX flags
├── local.properties                    # Secrets file (ignored in git)
├── local.properties.example            # Setup template for API key
├── gradlew & gradlew.bat               # Gradle wrapper execution scripts
└── .gitignore                          # Git exclusions (protects local.properties)
```

---

## 4. API Key Configuration

To keep API credentials secure and prevent committing secrets to source control, the API key is read from `local.properties` at build time and injected into `BuildConfig.OPENWEATHER_API_KEY`.

### Step 1: Obtain a Free OpenWeatherMap API Key
1. Visit [https://openweathermap.org/](https://openweathermap.org/) and sign up for a free account.
2. Go to your account profile and select the **API keys** tab: [https://home.openweathermap.org/api_keys](https://home.openweathermap.org/api_keys).
3. Copy your default API key (or generate a new key name, e.g., `WeatherApp`).
> *Note: Newly generated OpenWeatherMap keys typically activate within 10–30 minutes.*

### Step 2: Configure `local.properties`
Open `d:\android-app\local.properties` in your text editor or Android Studio and set your key:

```properties
OPENWEATHER_API_KEY=your_actual_api_key_here
```

*(Note: `local.properties` is already added to `.gitignore` so your secret will never be pushed to Git).*

---

## 5. How to Build and Run in Android Studio

1. **Open Android Studio**:
   - Choose **File > Open...** (or click **Open** on the Welcome screen).
   - Navigate to `d:\android-app` and click **OK**.
2. **Gradle Sync**:
   - Android Studio will automatically recognize the Gradle files and initiate a sync.
   - Wait for the sync to complete and index dependencies.
3. **Select an Android Device or Emulator**:
   - In the toolbar device selector, select an Android Emulator (API 26+) or a connected physical Android device with USB Debugging enabled.
4. **Run the App**:
   - Click the green **Run** button (or press `Shift + F10`).
   - The application will build, install, and launch on your device.

---

## 6. Running Unit Tests

To run the automated unit tests verifying the ViewModel, state retention, empty search prevention, and data formatting:

- In Android Studio: Right-click `app/src/test/java/com/example/weatherapp` and choose **Run 'Tests in 'weatherapp''**.
- Or via terminal:
  ```powershell
  .\gradlew.bat test
  ```

---

## 7. App Features & Behavior Summary

- **Default City**: Pune is loaded automatically on first launch.
- **Search**: Enter any city name and press Enter/Search icon. Empty inputs are caught immediately with an on-screen validation prompt.
- **Weather Metrics Displayed**:
  - City Name & Country code (e.g. `Pune, IN`)
  - Weather condition (e.g. `Scattered Clouds`)
  - High-res Weather Icon from OpenWeather CDN
  - Temperature in °C (Large bold typography)
  - Feels-like temperature (`Feels like 30°C`)
  - Daily High and Low (`H: 32°C  •  L: 22°C`)
  - Humidity percentage (`65%`)
  - Wind speed (`4.2 m/s`)
  - Atmospheric pressure (`1012 hPa`)
- **Refresh**: Tap the refresh icon on the top right at any time to re-fetch the latest metrics for the active city.
- **Resilient Error State**: When an invalid city is searched or the network drops during a search, the app alerts the user via a dismissable error banner while **keeping the last valid weather information on screen**.
