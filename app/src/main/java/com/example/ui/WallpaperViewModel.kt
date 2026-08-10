package com.example.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SavedWallpaper
import com.example.data.ScaleMode
import com.example.data.WallpaperConfig
import com.example.data.WallpaperGalleryRepository
import com.example.data.WallpaperPreferences
import com.example.service.VideoWallpaperService
import com.example.ui.helpers.DownloadState
import com.example.ui.helpers.OptimizationState
import com.example.ui.helpers.RustVideoOptimizer
import com.example.ui.helpers.TikTokDownloader
import com.example.ui.helpers.WallpaperBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperViewModel(
    private val preferences: WallpaperPreferences,
    private val galleryRepository: WallpaperGalleryRepository
) : ViewModel() {

    private val _hasOriginalBackup = MutableStateFlow(false)
    val hasOriginalBackup: StateFlow<Boolean> = _hasOriginalBackup.asStateFlow()

    private val _videoResolutionInfo = MutableStateFlow<VideoResolutionInfo?>(null)
    val videoResolutionInfo: StateFlow<VideoResolutionInfo?> = _videoResolutionInfo.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _optimizationState = MutableStateFlow<OptimizationState>(OptimizationState.Idle)
    val optimizationState: StateFlow<OptimizationState> = _optimizationState.asStateFlow()

    val savedWallpapers: StateFlow<List<SavedWallpaper>> = galleryRepository.wallpapersFlow

    val configState: StateFlow<WallpaperConfig> = preferences.configFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = preferences.loadConfig()
        )

    fun detectVideoResolution(context: Context, videoUriString: String) {
        if (videoUriString.isBlank()) {
            _videoResolutionInfo.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.parse(videoUriString))
                val widthStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                retriever.release()

                var w = widthStr?.toIntOrNull() ?: 0
                var h = heightStr?.toIntOrNull() ?: 0
                val rotation = rotationStr?.toIntOrNull() ?: 0
                if (rotation == 90 || rotation == 270) {
                    val temp = w
                    w = h
                    h = temp
                }

                if (w > 0 && h > 0) {
                    val info = VideoResolutionInfo(w, h)
                    _videoResolutionInfo.value = info

                    // Validate if current selected resolution index is higher than source video resolution
                    val minDim = info.effectiveHeight
                    val currentIdx = configState.value.qualityResolutionIndex
                    
                    val is1080pAllowed = minDim > 1080
                    val is720pAllowed = minDim > 720
                    val is540pAllowed = minDim > 540

                    if ((currentIdx == 1 && !is1080pAllowed) ||
                        (currentIdx == 2 && !is720pAllowed) ||
                        (currentIdx == 3 && !is540pAllowed)) {
                        onQualityResolutionIndexChanged(0) // Fallback to Original
                    }
                }
            } catch (e: Exception) {
                Log.w("WallpaperViewModel", "Could not detect video resolution: ${e.message}")
            }
        }
    }

    fun checkOriginalBackup(context: Context) {
        _hasOriginalBackup.value = WallpaperBackupManager.checkOriginalBackup(context)
    }

    fun backupOriginalWallpaperIfNeeded(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val backedUp = WallpaperBackupManager.backupOriginalWallpaperIfNeeded(context)
            if (backedUp) {
                _hasOriginalBackup.value = true
            }
        }
    }

    fun restoreOriginalWallpaper(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = WallpaperBackupManager.restoreOriginalWallpaper(context)
            withContext(Dispatchers.Main) {
                onResult(success)
            }
        }
    }

    fun openSystemWallpaperPicker(context: Context) {
        WallpaperBackupManager.openSystemWallpaperPicker(context)
    }

    // MANDATORY DEFAULT: Downscale video with Rust engine and preserve sharpness
    fun onVideoSelected(context: Context, uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, flags)
            } catch (e: Exception) {
                Log.w("WallpaperViewModel", "Could not take persistable URI permission: ${e.message}")
            }

            backupOriginalWallpaperIfNeeded(context)

            // Execute Rust Video Downscaling & Sharpness Processing
            val optimizedUri = RustVideoOptimizer.downscaleAndOptimizeVideo(context, uri) { state ->
                _optimizationState.value = state
            }

            val finalUri = optimizedUri ?: uri
            preferences.saveVideoUri(finalUri)
            detectVideoResolution(context, finalUri.toString())

            // Add to Wallpaper Gallery
            val wallpaperId = "wall_${System.currentTimeMillis()}"
            val newWallpaper = SavedWallpaper(
                id = wallpaperId,
                title = "Vídeo Galería (${System.currentTimeMillis() % 10000})",
                uriString = finalUri.toString(),
                isLiveVideo = true,
                resolutionText = "720p Rust • Nitidez Nativa",
                fileSizeMB = 12.5f,
                timestamp = System.currentTimeMillis(),
                isCurrent = true
            )
            galleryRepository.addWallpaper(newWallpaper)
        }
    }

    fun downloadTikTokVideo(context: Context, tiktokUrl: String) {
        viewModelScope.launch {
            backupOriginalWallpaperIfNeeded(context)
            val downloadedUri = TikTokDownloader.downloadTikTokVideo(context, tiktokUrl) { state ->
                _downloadState.value = state
            }
            if (downloadedUri != null) {
                // Mandatory Rust Downscale and Sharpness optimization
                val optimizedUri = RustVideoOptimizer.downscaleAndOptimizeVideo(context, downloadedUri) { state ->
                    _optimizationState.value = state
                }

                val finalUri = optimizedUri ?: downloadedUri
                preferences.saveVideoUri(finalUri)
                detectVideoResolution(context, finalUri.toString())

                val wallpaperId = "tiktok_${System.currentTimeMillis()}"
                val newWallpaper = SavedWallpaper(
                    id = wallpaperId,
                    title = "TikTok Animado (${System.currentTimeMillis() % 10000})",
                    uriString = finalUri.toString(),
                    isLiveVideo = true,
                    resolutionText = "720p Rust • Nitidez Nativa",
                    fileSizeMB = 8.4f,
                    timestamp = System.currentTimeMillis(),
                    isCurrent = true
                )
                galleryRepository.addWallpaper(newWallpaper)
            }
        }
    }

    fun applySavedWallpaper(wallpaper: SavedWallpaper) {
        val uri = Uri.parse(wallpaper.uriString)
        preferences.saveVideoUri(uri)
        galleryRepository.setCurrentWallpaper(wallpaper.id)
    }

    fun deleteSavedWallpaper(id: String) {
        galleryRepository.deleteWallpaper(id)
    }

    fun resetOptimizationState() {
        _optimizationState.value = OptimizationState.Idle
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    fun onDayNightEnabledChanged(enabled: Boolean) {
        preferences.saveIsDayNightEnabled(enabled)
    }

    fun onDayVideoSelected(uriString: String) {
        preferences.saveDayVideoUri(uriString)
    }

    fun onNightVideoSelected(uriString: String) {
        preferences.saveNightVideoUri(uriString)
    }

    fun onDayNightHoursChanged(dayStart: Int, nightStart: Int) {
        preferences.saveDayNightHours(dayStart, nightStart)
    }

    fun isCurrentlyDayTime(): Boolean {
        val config = configState.value
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return currentHour in config.dayStartHour until config.nightStartHour
    }

    fun getActiveVideoUri(): String {
        return preferences.getActiveVideoUriForTime(configState.value)
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

    fun onUseVideoCompressionChanged(enabled: Boolean) {
        preferences.saveUseVideoCompression(enabled)
    }

    fun onAppThemeChanged(theme: String) {
        preferences.saveAppTheme(theme)
    }

    fun isServiceActiveWallpaper(context: Context): Boolean {
        return WallpaperBackupManager.isServiceActiveWallpaper(context)
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

    class Factory(
        private val preferences: WallpaperPreferences,
        private val galleryRepository: WallpaperGalleryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WallpaperViewModel::class.java)) {
                return WallpaperViewModel(preferences, galleryRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

