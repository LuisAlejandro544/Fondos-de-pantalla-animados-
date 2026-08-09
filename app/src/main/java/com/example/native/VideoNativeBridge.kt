package com.example.native

import android.util.Log
import android.view.Surface

object VideoNativeBridge {

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("videowallpaper_native")
            isNativeLoaded = true
            Log.i("VideoNativeBridge", "Librería nativa C++/Rust NDK MediaCodec cargada correctamente.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w("VideoNativeBridge", "Librería nativa NDK no vinculada o fallback activo: ${e.message}")
            isNativeLoaded = false
        } catch (e: Exception) {
            Log.e("VideoNativeBridge", "Error al cargar la librería nativa: ${e.message}")
            isNativeLoaded = false
        }
    }

    external fun getNativeEngineInfo(): String
    external fun isHardwareAccelerationSupported(): Boolean
    external fun configureNativeWindowSurface(surface: Surface, targetWidth: Int, targetHeight: Int, enableHardwareSharpness: Boolean): Boolean
    external fun calculateOptimalResolution(origWidth: Int, origHeight: Int, qualityModeIndex: Int): IntArray
    external fun getEngineStats(
        isNativeActive: Boolean,
        batterySaverOn: Boolean,
        qualityIndex: Int,
        sharpnessOn: Boolean,
        compressionOn: Boolean
    ): String

    fun isNativeReady(): Boolean = isNativeLoaded

    fun getEngineStatus(): String {
        return if (isNativeLoaded) {
            try {
                getNativeEngineInfo()
            } catch (e: Exception) {
                "Motor C++/Rust NDK Activo"
            }
        } else {
            "Motor C++/Rust NDK Preparado"
        }
    }
}
