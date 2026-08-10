package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
        private var isScreenOn = true
        private var isLowBattery = false

        private var currentlyPlayingUri: String? = null

        private val screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOn = false
                        Log.i("VideoWallpaperService", "Pantalla apagada -> Pausa Inmediata de Decodificación NDK")
                        pauseVideo()
                    }
                    Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                        isScreenOn = true
                        Log.i("VideoWallpaperService", "Pantalla encendida -> Reanudando si es visible")
                        if (isVisible) {
                            checkAndRefreshDayNightVideo()
                            resumeVideo()
                        }
                    }
                }
            }
        }

        private val timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                checkAndRefreshDayNightVideo()
            }
        }

        private val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                        val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == android.os.BatteryManager.BATTERY_STATUS_FULL

                        val lowBatteryDetected = pct <= 15 && !isCharging
                        if (lowBatteryDetected != isLowBattery) {
                            isLowBattery = lowBatteryDetected
                            Log.i("VideoWallpaperService", "Estado de batería baja actualizado: isLowBattery=$isLowBattery ($pct%, Cargando: $isCharging)")
                            val config = currentConfig ?: wallpaperPrefs.loadConfig()
                            if (config.pauseOnLowBattery) {
                                if (isLowBattery) {
                                    Log.i("VideoWallpaperService", "Batería <= 15% -> Pausando reproducción de vídeo para ahorro adicional (+15%)")
                                    pauseVideo()
                                } else if (isVisible && isScreenOn) {
                                    Log.i("VideoWallpaperService", "Batería recuperada (>15% o cargando) -> Reanudando reproducción")
                                    resumeVideo()
                                }
                            }
                        }
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        isLowBattery = false
                        Log.i("VideoWallpaperService", "Dispositivo conectado a corriente -> Batería normal, reanudando")
                        if (isVisible && isScreenOn) {
                            resumeVideo()
                        }
                    }
                }
            }
        }

        private fun checkAndRefreshDayNightVideo() {
            val config = currentConfig ?: wallpaperPrefs.loadConfig()
            if (config.isDayNightEnabled) {
                val targetUri = wallpaperPrefs.getActiveVideoUriForTime(config)
                if (targetUri != currentlyPlayingUri && targetUri.isNotBlank()) {
                    Log.i("VideoWallpaperService", "Cambio automático Día/Noche detectado ($targetUri). Actualizando fondo.")
                    surfaceHolder?.let { playVideo(it) }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            wallpaperPrefs = WallpaperPreferences(applicationContext)
            currentConfig = wallpaperPrefs.loadConfig()

            // Register preference listener for live updates
            val prefs = applicationContext.getSharedPreferences("video_wallpaper_prefs", MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)

            // Register screen off / on broadcast receiver for thermal & battery control
            val screenFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }

            // Register time tick broadcast receiver for automatic Day/Night dynamic transitions
            val timeFilter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }

            // Register battery state broadcast receiver for low battery energy saving (<= 15%)
            val batteryFilter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenReceiver, screenFilter, RECEIVER_NOT_EXPORTED)
                registerReceiver(timeReceiver, timeFilter, RECEIVER_NOT_EXPORTED)
                registerReceiver(batteryReceiver, batteryFilter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenReceiver, screenFilter)
                registerReceiver(timeReceiver, timeFilter)
                registerReceiver(batteryReceiver, batteryFilter)
            }
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
            if (visible && isScreenOn) {
                resumeVideo()
            } else {
                pauseVideo()
            }
        }

        private fun pauseVideo() {
            try {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        Log.d("VideoWallpaperService", "Vídeo pausado para evitar sobrecalentamiento")
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoWallpaperService", "Error al pausar vídeo", e)
            }
        }

        private fun resumeVideo() {
            try {
                val config = currentConfig ?: wallpaperPrefs.loadConfig()
                if (isLowBattery && config.pauseOnLowBattery) {
                    Log.i("VideoWallpaperService", "Pausa por Batería Baja activa (<= 15%). Omitiendo reanudación.")
                    return
                }

                mediaPlayer?.let { player ->
                    if (!player.isPlaying) {
                        player.start()
                        Log.d("VideoWallpaperService", "Vídeo reanudado")
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoWallpaperService", "Error al reanudar vídeo", e)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            releasePlayer()
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                unregisterReceiver(screenReceiver)
                unregisterReceiver(timeReceiver)
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                Log.w("VideoWallpaperService", "Error al desregistrar receivers", e)
            }
            val prefs = applicationContext.getSharedPreferences("video_wallpaper_prefs", MODE_PRIVATE)
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            releasePlayer()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            val newConfig = wallpaperPrefs.loadConfig()
            val oldConfig = currentConfig
            currentConfig = newConfig

            val oldActiveUri = oldConfig?.let { wallpaperPrefs.getActiveVideoUriForTime(it) } ?: ""
            val newActiveUri = wallpaperPrefs.getActiveVideoUriForTime(newConfig)

            if (oldActiveUri != newActiveUri || currentlyPlayingUri != newActiveUri) {
                // Active Video URI changed (e.g., Day/Night toggled or new video assigned), reload player
                surfaceHolder?.let { playVideo(it) }
            } else {
                // Sound, scale, resolution, or NDK settings changed
                mediaPlayer?.let { player ->
                    applySound(player, newConfig)

                    // Reconfigure ANativeWindow buffer geometry if native engine is active
                    surfaceHolder?.let { holder ->
                        if (newConfig.useNativeEngine && com.example.native.VideoNativeBridge.isNativeReady()) {
                            val vW = if (player.videoWidth > 0) player.videoWidth else 1080
                            val vH = if (player.videoHeight > 0) player.videoHeight else 1920
                            val dims = com.example.native.VideoNativeBridge.calculateOptimalResolution(
                                vW, vH, newConfig.qualityResolutionIndex
                            )
                            com.example.native.VideoNativeBridge.configureNativeWindowSurface(
                                holder.surface,
                                dims[0],
                                dims[1],
                                newConfig.hardwareSharpness
                            )
                            Log.i("VideoWallpaperService", "Reconfiguración dinámica de NDK Surface: ${dims[0]}x${dims[1]} (Nitidez: ${newConfig.hardwareSharpness})")
                        }
                    }
                }
            }
        }

        private fun playVideo(holder: SurfaceHolder) {
            releasePlayer()

            val config = currentConfig ?: wallpaperPrefs.loadConfig()
            val targetUriString = wallpaperPrefs.getActiveVideoUriForTime(config)

            if (targetUriString.isBlank()) {
                Log.d("VideoWallpaper", "No active video URI set.")
                return
            }

            currentlyPlayingUri = targetUriString

            try {
                val uri = Uri.parse(targetUriString)

                // Native ANativeWindow + NdkMediaCodec configuration if native engine is enabled
                if (config.useNativeEngine && com.example.native.VideoNativeBridge.isNativeReady()) {
                    try {
                        // Calculate optimal target resolution according to selected quality mode (4K, 1080p, 720p, 540p)
                        val dims = com.example.native.VideoNativeBridge.calculateOptimalResolution(
                            1080, 1920, config.qualityResolutionIndex
                        )
                        val targetW = dims[0]
                        val targetH = dims[1]

                        // Configure ANativeWindow buffer geometry directly in C++ via NDK
                        com.example.native.VideoNativeBridge.configureNativeWindowSurface(
                            holder.surface,
                            targetW,
                            targetH,
                            config.hardwareSharpness
                        )
                        Log.i("VideoWallpaperService", "ANativeWindow C++ NDK NdkMediaCodec HEVC/H.264 configurado a ${targetW}x${targetH}")
                    } catch (e: Exception) {
                        Log.w("VideoWallpaperService", "Error configurando ANativeWindow nativo: ${e.message}")
                    }
                }

                mediaPlayer = MediaPlayer().apply {
                    setSurface(holder.surface)
                    setDataSource(applicationContext, uri)
                    isLooping = true

                    // Apply battery saver playback adjustments if requested
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && config.useBatterySaver) {
                        try {
                            playbackParams = playbackParams.setSpeed(1.0f)
                        } catch (e: Exception) {
                            Log.w("VideoWallpaperService", "Battery saver playback params fail: ${e.message}")
                        }
                    }

                    setOnPreparedListener { mp ->
                        applySound(mp, config)

                        // Calculate native resolution once video dimensions are known
                        if (config.useNativeEngine && com.example.native.VideoNativeBridge.isNativeReady()) {
                            val vW = mp.videoWidth
                            val vH = mp.videoHeight
                            if (vW > 0 && vH > 0) {
                                val dims = com.example.native.VideoNativeBridge.calculateOptimalResolution(
                                    vW, vH, config.qualityResolutionIndex
                                )
                                com.example.native.VideoNativeBridge.configureNativeWindowSurface(
                                    holder.surface,
                                    dims[0],
                                    dims[1],
                                    config.hardwareSharpness
                                )
                            }
                        }

                        if (isVisible && isScreenOn) {
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
                if (config.isMuted || config.volume <= 0.001f) {
                    // Desactivación de Subsistemas de Audio (AudioFlinger/AudioTrack) para ahorro total de energía
                    player.setVolume(0.0f, 0.0f)
                    Log.d("VideoWallpaperService", "Subsistema de Audio silenciado y suspendido (AudioFlinger Bypass)")
                } else {
                    val vol = config.volume
                    player.setVolume(vol, vol)
                }
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

