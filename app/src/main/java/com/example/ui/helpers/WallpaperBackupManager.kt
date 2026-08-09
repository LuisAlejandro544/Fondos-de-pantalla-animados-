package com.example.ui.helpers

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.service.VideoWallpaperService
import java.io.File
import java.io.FileOutputStream

object WallpaperBackupManager {

    private const val ORIGINAL_WALLPAPER_FILENAME = "original_wallpaper.png"

    fun checkOriginalBackup(context: Context): Boolean {
        val file = File(context.filesDir, ORIGINAL_WALLPAPER_FILENAME)
        return file.exists()
    }

    fun backupOriginalWallpaperIfNeeded(context: Context): Boolean {
        val file = File(context.filesDir, ORIGINAL_WALLPAPER_FILENAME)
        if (file.exists()) {
            return true
        }

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
                        Log.i("WallpaperBackupManager", "Respaldo del fondo de pantalla original guardado correctamente.")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.e("WallpaperBackupManager", "Error al respaldar el fondo original", e)
            }
        }
        return false
    }

    fun restoreOriginalWallpaper(context: Context): Boolean {
        val file = File(context.filesDir, ORIGINAL_WALLPAPER_FILENAME)
        val wallpaperManager = WallpaperManager.getInstance(context)

        if (file.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    wallpaperManager.setBitmap(bitmap)
                    return true
                }
            } catch (e: Exception) {
                Log.e("WallpaperBackupManager", "Error al restaurar bitmap original", e)
            }
        }

        return try {
            wallpaperManager.clear()
            true
        } catch (e: Exception) {
            Log.e("WallpaperBackupManager", "Error al limpiar fondo de pantalla", e)
            false
        }
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

    fun openSystemWallpaperPicker(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
            context.startActivity(Intent.createChooser(intent, "Seleccionar fondo de pantalla"))
        } catch (e: Exception) {
            Log.e("WallpaperBackupManager", "Error abriendo el selector del sistema", e)
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
}
