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
    val appTheme: String = "SLATE_INDIGO",
    val isDayNightEnabled: Boolean = false,
    val dayVideoUri: String = "",
    val nightVideoUri: String = "",
    val dayStartHour: Int = 6,   // 06:00
    val nightStartHour: Int = 18 // 18:00
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
            appTheme = prefs.getString(KEY_APP_THEME, "SLATE_INDIGO") ?: "SLATE_INDIGO",
            isDayNightEnabled = prefs.getBoolean(KEY_IS_DAY_NIGHT_ENABLED, false),
            dayVideoUri = prefs.getString(KEY_DAY_VIDEO_URI, "") ?: "",
            nightVideoUri = prefs.getString(KEY_NIGHT_VIDEO_URI, "") ?: "",
            dayStartHour = prefs.getInt(KEY_DAY_START_HOUR, 6),
            nightStartHour = prefs.getInt(KEY_NIGHT_START_HOUR, 18)
        )
    }

    fun getActiveVideoUriForTime(config: WallpaperConfig = loadConfig(), currentHour: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)): String {
        if (!config.isDayNightEnabled) {
            return config.videoUri
        }
        val isDay = currentHour in config.dayStartHour until config.nightStartHour
        return if (isDay) {
            if (config.dayVideoUri.isNotBlank()) config.dayVideoUri else config.videoUri
        } else {
            if (config.nightVideoUri.isNotBlank()) config.nightVideoUri else config.videoUri
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

    fun saveAppTheme(theme: String) {
        prefs.edit().putString(KEY_APP_THEME, theme).apply()
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
        const val KEY_APP_THEME = "key_app_theme"
        const val KEY_IS_DAY_NIGHT_ENABLED = "key_is_day_night_enabled"
        const val KEY_DAY_VIDEO_URI = "key_day_video_uri"
        const val KEY_NIGHT_VIDEO_URI = "key_night_video_uri"
        const val KEY_DAY_START_HOUR = "key_day_start_hour"
        const val KEY_NIGHT_START_HOUR = "key_night_start_hour"

        private val WATCHED_KEYS = setOf(
            KEY_VIDEO_URI, KEY_VOLUME, KEY_IS_MUTED, KEY_SCALE_MODE,
            KEY_USE_NATIVE_ENGINE, KEY_USE_BATTERY_SAVER, KEY_QUALITY_RES_INDEX,
            KEY_HW_SHARPNESS, KEY_USE_VIDEO_COMPRESSION, KEY_APP_THEME,
            KEY_IS_DAY_NIGHT_ENABLED, KEY_DAY_VIDEO_URI, KEY_NIGHT_VIDEO_URI,
            KEY_DAY_START_HOUR, KEY_NIGHT_START_HOUR
        )
    }
}
