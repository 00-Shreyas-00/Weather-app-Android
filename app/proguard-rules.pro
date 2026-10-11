# Proguard rules for Retrofit, Gson, and OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.weatherapp.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
