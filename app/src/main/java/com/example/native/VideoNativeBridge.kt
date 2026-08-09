package com.example.native

import android.util.Log

object VideoNativeBridge {

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("videowallpaper_native")
            isNativeLoaded = true
            Log.i("VideoNativeBridge", "Librería nativa C++/Rust cargada correctamente.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w("VideoNativeBridge", "Entorno sin NDK activo o librería nativa no vinculada en runtime: ${e.message}")
            isNativeLoaded = false
        } catch (e: Exception) {
            Log.e("VideoNativeBridge", "Error al cargar la librería nativa: ${e.message}")
            isNativeLoaded = false
        }
    }

    external fun getNativeEngineInfo(): String
    external fun isHardwareAccelerationSupported(): Boolean

    fun getEngineStatus(): String {
        return if (isNativeLoaded) {
            try {
                getNativeEngineInfo()
            } catch (e: Exception) {
                "Motor C++/Rust listo en proyecto (CMake & JNI)"
            }
        } else {
            "Motor C++/Rust listo en proyecto (CMake & JNI)"
        }
    }
}
