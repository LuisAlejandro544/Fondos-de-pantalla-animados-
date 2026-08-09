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
import com.example.data.ScaleMode
import com.example.data.WallpaperConfig
import com.example.data.WallpaperPreferences
import com.example.service.VideoWallpaperService
import com.example.ui.helpers.DownloadState
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
    private val preferences: WallpaperPreferences
) : ViewModel() {

    private val _hasOriginalBackup = MutableStateFlow(false)
    val hasOriginalBackup: StateFlow<Boolean> = _hasOriginalBackup.asStateFlow()

    private val _videoResolutionInfo = MutableStateFlow<VideoResolutionInfo?>(null)
    val videoResolutionInfo: StateFlow<VideoResolutionInfo?> = _videoResolutionInfo.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

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

    fun onVideoSelected(context: Context, uri: Uri, contentResolver: ContentResolver) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w("WallpaperViewModel", "Could not take persistable URI permission: ${e.message}")
        }
        preferences.saveVideoUri(uri)
        detectVideoResolution(context, uri.toString())
    }

    fun downloadTikTokVideo(context: Context, tiktokUrl: String) {
        viewModelScope.launch {
            backupOriginalWallpaperIfNeeded(context)
            val downloadedUri = TikTokDownloader.downloadTikTokVideo(context, tiktokUrl) { state ->
                _downloadState.value = state
            }
            if (downloadedUri != null) {
                preferences.saveVideoUri(downloadedUri)
                detectVideoResolution(context, downloadedUri.toString())
            }
        }
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
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
