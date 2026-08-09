package com.example.service

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.example.data.ScaleMode
import com.example.data.WallpaperConfig
import com.example.data.WallpaperPreferences

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {

        private var mediaPlayer: MediaPlayer? = null
        private lateinit var wallpaperPrefs: WallpaperPreferences
        private var currentConfig: WallpaperConfig? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            wallpaperPrefs = WallpaperPreferences(applicationContext)
            currentConfig = wallpaperPrefs.loadConfig()

            // Register preference listener for live updates
            val prefs = applicationContext.getSharedPreferences("video_wallpaper_prefs", MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            playVideo(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (mediaPlayer == null) {
                playVideo(holder)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            mediaPlayer?.let { player ->
                try {
                    if (visible) {
                        if (!player.isPlaying) {
                            player.start()
                        }
                    } else {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VideoWallpaper", "Error toggling visibility play state", e)
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            releasePlayer()
        }

        override fun onDestroy() {
            super.onDestroy()
            val prefs = applicationContext.getSharedPreferences("video_wallpaper_prefs", MODE_PRIVATE)
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            releasePlayer()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            val newConfig = wallpaperPrefs.loadConfig()
            val oldConfig = currentConfig
            currentConfig = newConfig

            if (oldConfig?.videoUri != newConfig.videoUri) {
                // Video changed, reload
                surfaceHolder?.let { playVideo(it) }
            } else {
                // Sound or scale changed, update live player
                mediaPlayer?.let { applySound(it, newConfig) }
            }
        }

        private fun playVideo(holder: SurfaceHolder) {
            releasePlayer()

            val config = currentConfig ?: wallpaperPrefs.loadConfig()
            if (config.videoUri.isBlank()) {
                Log.d("VideoWallpaper", "No video URI set yet.")
                return
            }

            try {
                val uri = Uri.parse(config.videoUri)
                mediaPlayer = MediaPlayer().apply {
                    setSurface(holder.surface)
                    setDataSource(applicationContext, uri)
                    isLooping = true
                    setOnPreparedListener { mp ->
                        applySound(mp, config)
                        if (isVisible) {
                            mp.start()
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("VideoWallpaper", "MediaPlayer error: what=$what extra=$extra")
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("VideoWallpaper", "Failed to initialize video wallpaper player", e)
            }
        }

        private fun applySound(player: MediaPlayer, config: WallpaperConfig) {
            try {
                val vol = if (config.isMuted) 0.0f else config.volume
                player.setVolume(vol, vol)
            } catch (e: Exception) {
                Log.e("VideoWallpaper", "Error setting volume", e)
            }
        }

        private fun releasePlayer() {
            try {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
            } catch (e: Exception) {
                Log.e("VideoWallpaper", "Error releasing player", e)
            } finally {
                mediaPlayer = null
            }
        }
    }
}
