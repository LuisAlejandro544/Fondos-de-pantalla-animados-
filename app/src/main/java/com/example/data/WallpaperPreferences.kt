package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WallpaperConfig(
    val videoUri: String = "",
    val volume: Float = 0.0f, // Default muted for wallpaper
    val isMuted: Boolean = true,
    val scaleMode: ScaleMode = ScaleMode.CROP,
    val useNativeEngine: Boolean = true,
    val useBatterySaver: Boolean = true,
    val qualityResolutionIndex: Int = 1, // 0 = 4K Original, 1 = 1080p Smart, 2 = 720p Eco, 3 = 540p Max Battery
    val hardwareSharpness: Boolean = true,
    val useVideoCompression: Boolean = true,
    val pauseOnLowBattery: Boolean = true, // Pausa automática por batería baja (≤15%)
    val appTheme: String = "SLATE_INDIGO",
    val isDayNightEnabled: Boolean = false,
    val dayVideoUri: String = "",
    val nightVideoUri: String = "",
    val dayStartHour: Int = 6,   // 06:00
    val nightStartHour: Int = 18, // 18:00
    val smartAudioFocus: Boolean = true, // Gestión Inteligente de Enfoque de Audio (Spotify, YouTube, Llamadas)
    val audioFadeEnabled: Boolean = true, // Fundido Suave de Entrada/Salida de Audio
    val nightQuietMode: Boolean = true,   // Silencio Nocturno Automático para el fondo de pantalla
    // Filtros Visuales y Efecto Blur para Launcher
    val blurRadius: Float = 0.0f, // 0.0f = Desactivado, 0.1f..1.0f = Blur en tiempo real
    val brightness: Float = 0.0f, // -0.5f a +0.5f
    val contrast: Float = 1.0f,   // 0.5f a 1.5f
    val saturation: Float = 1.0f, // 0.0f (Blanco y Negro) a 2.0f (Ultra Vívido)
    val colorFilterMode: String = "NONE", // NONE, LAUNCHER_DARK, SEPIA, CYBERPUNK, NIGHT_WARM
    // Cambio por Clima y Puesta/Salida de Sol Real (Sin Google Play Services)
    val isWeatherEnabled: Boolean = false,
    val isRealSolarEnabled: Boolean = false,
    val sunnyVideoUri: String = "",
    val rainyVideoUri: String = "",
    val cloudyVideoUri: String = "",
    val snowyVideoUri: String = "",
    val lastLocationLat: Double = 0.0,
    val lastLocationLng: Double = 0.0,
    val lastCityName: String = "Ubicación Automática",
    val lastWeatherCondition: String = "CLEAR", // CLEAR, RAIN, CLOUDS, SNOW
    val lastWeatherTemp: String = "--°C",
    val lastSunriseTime: String = "06:30",
    val lastSunsetTime: String = "20:30"
)

enum class ScaleMode {
    CROP,
    FIT,
    STRETCH
}

class WallpaperPreferences(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<WallpaperConfig> = _configFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in WATCHED_KEYS) {
            _configFlow.value = loadConfig()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun loadConfig(): WallpaperConfig {
        return WallpaperConfig(
            videoUri = prefs.getString(KEY_VIDEO_URI, "") ?: "",
            volume = prefs.getFloat(KEY_VOLUME, 0.0f),
            isMuted = prefs.getBoolean(KEY_IS_MUTED, true),
            scaleMode = runCatching {
                ScaleMode.valueOf(prefs.getString(KEY_SCALE_MODE, ScaleMode.CROP.name) ?: ScaleMode.CROP.name)
            }.getOrDefault(ScaleMode.CROP),
            useNativeEngine = prefs.getBoolean(KEY_USE_NATIVE_ENGINE, true),
            useBatterySaver = prefs.getBoolean(KEY_USE_BATTERY_SAVER, true),
            qualityResolutionIndex = prefs.getInt(KEY_QUALITY_RES_INDEX, 1),
            hardwareSharpness = prefs.getBoolean(KEY_HW_SHARPNESS, true),
            useVideoCompression = prefs.getBoolean(KEY_USE_VIDEO_COMPRESSION, true),
            pauseOnLowBattery = prefs.getBoolean(KEY_PAUSE_ON_LOW_BATTERY, true),
            appTheme = prefs.getString(KEY_APP_THEME, "SLATE_INDIGO") ?: "SLATE_INDIGO",
            isDayNightEnabled = prefs.getBoolean(KEY_IS_DAY_NIGHT_ENABLED, false),
            dayVideoUri = prefs.getString(KEY_DAY_VIDEO_URI, "") ?: "",
            nightVideoUri = prefs.getString(KEY_NIGHT_VIDEO_URI, "") ?: "",
            dayStartHour = prefs.getInt(KEY_DAY_START_HOUR, 6),
            nightStartHour = prefs.getInt(KEY_NIGHT_START_HOUR, 18),
            smartAudioFocus = prefs.getBoolean(KEY_SMART_AUDIO_FOCUS, true),
            audioFadeEnabled = prefs.getBoolean(KEY_AUDIO_FADE_ENABLED, true),
            nightQuietMode = prefs.getBoolean(KEY_NIGHT_QUIET_MODE, true),
            blurRadius = prefs.getFloat(KEY_BLUR_RADIUS, 0.0f),
            brightness = prefs.getFloat(KEY_BRIGHTNESS, 0.0f),
            contrast = prefs.getFloat(KEY_CONTRAST, 1.0f),
            saturation = prefs.getFloat(KEY_SATURATION, 1.0f),
            colorFilterMode = prefs.getString(KEY_COLOR_FILTER_MODE, "NONE") ?: "NONE",
            isWeatherEnabled = prefs.getBoolean(KEY_IS_WEATHER_ENABLED, false),
            isRealSolarEnabled = prefs.getBoolean(KEY_IS_REAL_SOLAR_ENABLED, false),
            sunnyVideoUri = prefs.getString(KEY_SUNNY_VIDEO_URI, "") ?: "",
            rainyVideoUri = prefs.getString(KEY_RAINY_VIDEO_URI, "") ?: "",
            cloudyVideoUri = prefs.getString(KEY_CLOUDY_VIDEO_URI, "") ?: "",
            snowyVideoUri = prefs.getString(KEY_SNOWY_VIDEO_URI, "") ?: "",
            lastLocationLat = prefs.getFloat(KEY_LAST_LOC_LAT, 0.0f).toDouble(),
            lastLocationLng = prefs.getFloat(KEY_LAST_LOC_LNG, 0.0f).toDouble(),
            lastCityName = prefs.getString(KEY_LAST_CITY_NAME, "Ubicación Automática") ?: "Ubicación Automática",
            lastWeatherCondition = prefs.getString(KEY_LAST_WEATHER_CONDITION, "CLEAR") ?: "CLEAR",
            lastWeatherTemp = prefs.getString(KEY_LAST_WEATHER_TEMP, "--°C") ?: "--°C",
            lastSunriseTime = prefs.getString(KEY_LAST_SUNRISE_TIME, "06:30") ?: "06:30",
            lastSunsetTime = prefs.getString(KEY_LAST_SUNSET_TIME, "20:30") ?: "20:30"
        )
    }

    fun getActiveVideoUriForTime(
        config: WallpaperConfig = loadConfig(),
        currentHour: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
        currentMinute: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
    ): String {
        // 1. Weather condition check if weather mode is active
        if (config.isWeatherEnabled) {
            val weatherVideo = when (config.lastWeatherCondition) {
                "RAIN" -> config.rainyVideoUri
                "CLOUDS" -> config.cloudyVideoUri
                "SNOW" -> config.snowyVideoUri
                else -> config.sunnyVideoUri
            }
            if (weatherVideo.isNotBlank()) {
                return weatherVideo
            }
        }

        // 2. Astronomical Sunrise / Sunset check if real solar mode is active
        if (config.isRealSolarEnabled) {
            val currentMinutesToday = currentHour * 60 + currentMinute
            val sunriseMinutes = parseTimeToMinutes(config.lastSunriseTime, 6 * 60 + 30)
            val sunsetMinutes = parseTimeToMinutes(config.lastSunsetTime, 20 * 60 + 30)

            val isDay = currentMinutesToday in sunriseMinutes until sunsetMinutes
            return if (isDay) {
                if (config.dayVideoUri.isNotBlank()) config.dayVideoUri else config.videoUri
            } else {
                if (config.nightVideoUri.isNotBlank()) config.nightVideoUri else config.videoUri
            }
        }

        // 3. Fallback to standard fixed-hour Day / Night
        if (config.isDayNightEnabled) {
            val isDay = currentHour in config.dayStartHour until config.nightStartHour
            return if (isDay) {
                if (config.dayVideoUri.isNotBlank()) config.dayVideoUri else config.videoUri
            } else {
                if (config.nightVideoUri.isNotBlank()) config.nightVideoUri else config.videoUri
            }
        }

        return config.videoUri
    }

    private fun parseTimeToMinutes(timeStr: String, defaultVal: Int): Int {
        return try {
            val parts = timeStr.trim().split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            h * 60 + m
        } catch (e: Exception) {
            defaultVal
        }
    }

    fun saveVideoUri(uri: Uri) {
        prefs.edit().putString(KEY_VIDEO_URI, uri.toString()).apply()
        _configFlow.value = loadConfig()
    }

    fun saveIsDayNightEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DAY_NIGHT_ENABLED, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveDayVideoUri(uriString: String) {
        prefs.edit().putString(KEY_DAY_VIDEO_URI, uriString).apply()
        _configFlow.value = loadConfig()
    }

    fun saveNightVideoUri(uriString: String) {
        prefs.edit().putString(KEY_NIGHT_VIDEO_URI, uriString).apply()
        _configFlow.value = loadConfig()
    }

    fun saveDayNightHours(dayStart: Int, nightStart: Int) {
        prefs.edit()
            .putInt(KEY_DAY_START_HOUR, dayStart)
            .putInt(KEY_NIGHT_START_HOUR, nightStart)
            .apply()
        _configFlow.value = loadConfig()
    }

    fun saveVolume(volume: Float) {
        val clampedVol = volume.coerceIn(0.0f, 1.0f)
        val editor = prefs.edit()
        if (clampedVol > 0.0f) {
            editor.putFloat(KEY_VOLUME, clampedVol)
            editor.putFloat(KEY_LAST_NON_ZERO_VOLUME, clampedVol)
            editor.putBoolean(KEY_IS_MUTED, false)
        } else {
            editor.putFloat(KEY_VOLUME, 0.0f)
            editor.putBoolean(KEY_IS_MUTED, true)
        }
        editor.apply()
        _configFlow.value = loadConfig()
    }

    fun saveIsMuted(isMuted: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_MUTED, isMuted)
        if (isMuted) {
            editor.putFloat(KEY_VOLUME, 0.0f)
        } else {
            val lastVol = prefs.getFloat(KEY_LAST_NON_ZERO_VOLUME, 0.7f)
            val restoreVol = if (lastVol > 0.0f) lastVol else 0.7f
            editor.putFloat(KEY_VOLUME, restoreVol)
        }
        editor.apply()
        _configFlow.value = loadConfig()
    }

    fun saveScaleMode(scaleMode: ScaleMode) {
        prefs.edit().putString(KEY_SCALE_MODE, scaleMode.name).apply()
        _configFlow.value = loadConfig()
    }

    fun saveUseNativeEngine(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_NATIVE_ENGINE, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveUseBatterySaver(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_BATTERY_SAVER, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveQualityResolutionIndex(index: Int) {
        prefs.edit().putInt(KEY_QUALITY_RES_INDEX, index).apply()
        _configFlow.value = loadConfig()
    }

    fun saveHardwareSharpness(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HW_SHARPNESS, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveUseVideoCompression(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_VIDEO_COMPRESSION, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun savePauseOnLowBattery(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PAUSE_ON_LOW_BATTERY, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveAppTheme(theme: String) {
        prefs.edit().putString(KEY_APP_THEME, theme).apply()
        _configFlow.value = loadConfig()
    }

    fun saveSmartAudioFocus(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMART_AUDIO_FOCUS, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveAudioFadeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUDIO_FADE_ENABLED, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveNightQuietMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_QUIET_MODE, enabled).apply()
        _configFlow.value = loadConfig()
    }

    fun saveVisualFilters(blurRadius: Float, brightness: Float, contrast: Float, saturation: Float, colorFilterMode: String) {
        prefs.edit()
            .putFloat(KEY_BLUR_RADIUS, blurRadius.coerceIn(0.0f, 1.0f))
            .putFloat(KEY_BRIGHTNESS, brightness.coerceIn(-0.5f, 0.5f))
            .putFloat(KEY_CONTRAST, contrast.coerceIn(0.5f, 1.5f))
            .putFloat(KEY_SATURATION, saturation.coerceIn(0.0f, 2.0f))
            .putString(KEY_COLOR_FILTER_MODE, colorFilterMode)
            .apply()
        _configFlow.value = loadConfig()
    }

    fun saveWeatherSettings(
        isWeatherEnabled: Boolean,
        isRealSolarEnabled: Boolean,
        sunnyUri: String = "",
        rainyUri: String = "",
        cloudyUri: String = "",
        snowyUri: String = ""
    ) {
        val editor = prefs.edit()
            .putBoolean(KEY_IS_WEATHER_ENABLED, isWeatherEnabled)
            .putBoolean(KEY_IS_REAL_SOLAR_ENABLED, isRealSolarEnabled)
        if (sunnyUri.isNotBlank()) editor.putString(KEY_SUNNY_VIDEO_URI, sunnyUri)
        if (rainyUri.isNotBlank()) editor.putString(KEY_RAINY_VIDEO_URI, rainyUri)
        if (cloudyUri.isNotBlank()) editor.putString(KEY_CLOUDY_VIDEO_URI, cloudyUri)
        if (snowyUri.isNotBlank()) editor.putString(KEY_SNOWY_VIDEO_URI, snowyUri)
        editor.apply()
        _configFlow.value = loadConfig()
    }

    fun updateWeatherData(
        cityName: String,
        condition: String,
        temp: String,
        sunrise: String,
        sunset: String,
        lat: Double = 0.0,
        lng: Double = 0.0
    ) {
        prefs.edit()
            .putString(KEY_LAST_CITY_NAME, cityName)
            .putString(KEY_LAST_WEATHER_CONDITION, condition)
            .putString(KEY_LAST_WEATHER_TEMP, temp)
            .putString(KEY_LAST_SUNRISE_TIME, sunrise)
            .putString(KEY_LAST_SUNSET_TIME, sunset)
            .putFloat(KEY_LAST_LOC_LAT, lat.toFloat())
            .putFloat(KEY_LAST_LOC_LNG, lng.toFloat())
            .apply()
        _configFlow.value = loadConfig()
    }

    companion object {
        private const val PREFS_NAME = "video_wallpaper_prefs"
        const val KEY_VIDEO_URI = "key_video_uri"
        const val KEY_VOLUME = "key_volume"
        const val KEY_LAST_NON_ZERO_VOLUME = "key_last_non_zero_volume"
        const val KEY_IS_MUTED = "key_is_muted"
        const val KEY_SCALE_MODE = "key_scale_mode"
        const val KEY_USE_NATIVE_ENGINE = "key_use_native_engine"
        const val KEY_USE_BATTERY_SAVER = "key_use_battery_saver"
        const val KEY_QUALITY_RES_INDEX = "key_quality_res_index"
        const val KEY_HW_SHARPNESS = "key_hw_sharpness"
        const val KEY_USE_VIDEO_COMPRESSION = "key_use_video_compression"
        const val KEY_PAUSE_ON_LOW_BATTERY = "key_pause_on_low_battery"
        const val KEY_APP_THEME = "key_app_theme"
        const val KEY_IS_DAY_NIGHT_ENABLED = "key_is_day_night_enabled"
        const val KEY_DAY_VIDEO_URI = "key_day_video_uri"
        const val KEY_NIGHT_VIDEO_URI = "key_night_video_uri"
        const val KEY_DAY_START_HOUR = "key_day_start_hour"
        const val KEY_NIGHT_START_HOUR = "key_night_start_hour"
        const val KEY_SMART_AUDIO_FOCUS = "key_smart_audio_focus"
        const val KEY_AUDIO_FADE_ENABLED = "key_audio_fade_enabled"
        const val KEY_NIGHT_QUIET_MODE = "key_night_quiet_mode"

        // Visual Filters & Blur
        const val KEY_BLUR_RADIUS = "key_blur_radius"
        const val KEY_BRIGHTNESS = "key_brightness"
        const val KEY_CONTRAST = "key_contrast"
        const val KEY_SATURATION = "key_saturation"
        const val KEY_COLOR_FILTER_MODE = "key_color_filter_mode"

        // Weather & Real Solar
        const val KEY_IS_WEATHER_ENABLED = "key_is_weather_enabled"
        const val KEY_IS_REAL_SOLAR_ENABLED = "key_is_real_solar_enabled"
        const val KEY_SUNNY_VIDEO_URI = "key_sunny_video_uri"
        const val KEY_RAINY_VIDEO_URI = "key_rainy_video_uri"
        const val KEY_CLOUDY_VIDEO_URI = "key_cloudy_video_uri"
        const val KEY_SNOWY_VIDEO_URI = "key_snowy_video_uri"
        const val KEY_LAST_LOC_LAT = "key_last_loc_lat"
        const val KEY_LAST_LOC_LNG = "key_last_loc_lng"
        const val KEY_LAST_CITY_NAME = "key_last_city_name"
        const val KEY_LAST_WEATHER_CONDITION = "key_last_weather_condition"
        const val KEY_LAST_WEATHER_TEMP = "key_last_weather_temp"
        const val KEY_LAST_SUNRISE_TIME = "key_last_sunrise_time"
        const val KEY_LAST_SUNSET_TIME = "key_last_sunset_time"

        private val WATCHED_KEYS = setOf(
            KEY_VIDEO_URI, KEY_VOLUME, KEY_IS_MUTED, KEY_SCALE_MODE,
            KEY_USE_NATIVE_ENGINE, KEY_USE_BATTERY_SAVER, KEY_QUALITY_RES_INDEX,
            KEY_HW_SHARPNESS, KEY_USE_VIDEO_COMPRESSION, KEY_PAUSE_ON_LOW_BATTERY,
            KEY_APP_THEME, KEY_IS_DAY_NIGHT_ENABLED, KEY_DAY_VIDEO_URI,
            KEY_NIGHT_VIDEO_URI, KEY_DAY_START_HOUR, KEY_NIGHT_START_HOUR,
            KEY_SMART_AUDIO_FOCUS, KEY_AUDIO_FADE_ENABLED, KEY_NIGHT_QUIET_MODE,
            KEY_BLUR_RADIUS, KEY_BRIGHTNESS, KEY_CONTRAST, KEY_SATURATION,
            KEY_COLOR_FILTER_MODE, KEY_IS_WEATHER_ENABLED, KEY_IS_REAL_SOLAR_ENABLED,
            KEY_SUNNY_VIDEO_URI, KEY_RAINY_VIDEO_URI, KEY_CLOUDY_VIDEO_URI,
            KEY_SNOWY_VIDEO_URI, KEY_LAST_LOC_LAT, KEY_LAST_LOC_LNG,
            KEY_LAST_CITY_NAME, KEY_LAST_WEATHER_CONDITION, KEY_LAST_WEATHER_TEMP,
            KEY_LAST_SUNRISE_TIME, KEY_LAST_SUNSET_TIME
        )
    }
}
