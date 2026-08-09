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
    val scaleMode: ScaleMode = ScaleMode.CROP
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
            }.getOrDefault(ScaleMode.CROP)
        )
    }

    fun saveVideoUri(uri: Uri) {
        prefs.edit().putString(KEY_VIDEO_URI, uri.toString()).apply()
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

    companion object {
        private const val PREFS_NAME = "video_wallpaper_prefs"
        const val KEY_VIDEO_URI = "key_video_uri"
        const val KEY_VOLUME = "key_volume"
        const val KEY_LAST_NON_ZERO_VOLUME = "key_last_non_zero_volume"
        const val KEY_IS_MUTED = "key_is_muted"
        const val KEY_SCALE_MODE = "key_scale_mode"

        private val WATCHED_KEYS = setOf(KEY_VIDEO_URI, KEY_VOLUME, KEY_IS_MUTED, KEY_SCALE_MODE)
    }
}
