package com.example.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ScaleMode
import com.example.data.WallpaperConfig
import com.example.data.WallpaperPreferences
import com.example.service.VideoWallpaperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class WallpaperViewModel(
    private val preferences: WallpaperPreferences
) : ViewModel() {

    private val _hasOriginalBackup = MutableStateFlow(false)
    val hasOriginalBackup: StateFlow<Boolean> = _hasOriginalBackup.asStateFlow()

    val configState: StateFlow<WallpaperConfig> = preferences.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = preferences.loadConfig()
        )

    fun checkOriginalBackup(context: Context) {
        val file = File(context.filesDir, ORIGINAL_WALLPAPER_FILENAME)
        _hasOriginalBackup.value = file.exists()
    }

    fun backupOriginalWallpaperIfNeeded(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, ORIGINAL_WALLPAPER_FILENAME)
            if (file.exists()) {
                // Original static wallpaper already backed up! Keep it forever.
                _hasOriginalBackup.value = true
                return@launch
            }

            // Only capture if active wallpaper is NOT our live video wallpaper
            if (!isServiceActiveWallpaper(context)) {
                try {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    val drawable = wallpaperManager.drawable
                    if (drawable != null) {
                        val bitmap = drawableToBitmap(drawable)
                        if (bitmap != null) {
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            _hasOriginalBackup.value = true
                            Log.i("WallpaperViewModel", "Respaldo del fondo de pantalla original guardado correctamente.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WallpaperViewModel", "Error al respaldar el fondo original", e)
                }
            }
        }
    }

    fun restoreOriginalWallpaper(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, ORIGINAL_WALLPAPER_FILENAME)
            val wallpaperManager = WallpaperManager.getInstance(context)

            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        wallpaperManager.setBitmap(bitmap)
                        withContext(Dispatchers.Main) { onResult(true) }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("WallpaperViewModel", "Error al restaurar bitmap original", e)
                }
            }

            // Fallback: Clear live wallpaper service to return to system default wallpaper
            try {
                wallpaperManager.clear()
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                Log.e("WallpaperViewModel", "Error al limpiar fondo de pantalla", e)
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun clearWallpaper(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.clear()
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                Log.e("WallpaperViewModel", "Error al restablecer fondo del sistema", e)
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun openSystemWallpaperPicker(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
            context.startActivity(Intent.createChooser(intent, "Seleccionar fondo de pantalla"))
        } catch (e: Exception) {
            Log.e("WallpaperViewModel", "Error abriendo el selector del sistema", e)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1080
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun onVideoSelected(uri: Uri, contentResolver: ContentResolver) {
        try {
            // Take persistable URI permission so WallpaperService can access video across reboots
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w("WallpaperViewModel", "Could not take persistable URI permission: ${e.message}")
        }
        preferences.saveVideoUri(uri)
    }

    fun onVolumeChanged(volume: Float) {
        preferences.saveVolume(volume)
    }

    fun onMuteToggled() {
        val currentMuted = configState.value.isMuted
        preferences.saveIsMuted(!currentMuted)
    }

    fun onScaleModeChanged(scaleMode: ScaleMode) {
        preferences.saveScaleMode(scaleMode)
    }

    fun onUseNativeEngineChanged(enabled: Boolean) {
        preferences.saveUseNativeEngine(enabled)
    }

    fun onUseBatterySaverChanged(enabled: Boolean) {
        preferences.saveUseBatterySaver(enabled)
    }

    fun onQualityResolutionIndexChanged(index: Int) {
        preferences.saveQualityResolutionIndex(index)
    }

    fun onHardwareSharpnessChanged(enabled: Boolean) {
        preferences.saveHardwareSharpness(enabled)
    }

    fun isServiceActiveWallpaper(context: Context): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val info = wallpaperManager.wallpaperInfo
            info?.packageName == context.packageName && info?.serviceName == VideoWallpaperService::class.java.name
        } catch (e: Exception) {
            false
        }
    }

    fun openWallpaperPicker(context: Context) {
        try {
            backupOriginalWallpaperIfNeeded(context)
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, VideoWallpaperService::class.java)
                )
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("WallpaperViewModel", "Error opening live wallpaper chooser", e)
        }
    }

    companion object {
        private const val ORIGINAL_WALLPAPER_FILENAME = "original_wallpaper.png"
    }

    class Factory(private val preferences: WallpaperPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WallpaperViewModel::class.java)) {
                return WallpaperViewModel(preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

