package com.example.ui.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Loading(val message: String, val progressPercentage: Int = -1) : DownloadState()
    data class Success(val uri: Uri, val title: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object TikTokDownloader {

    private const val TAG = "TikTokDownloader"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    suspend fun downloadTikTokVideo(
        context: Context,
        rawUrl: String,
        onProgress: (DownloadState) -> Unit
    ): Uri? = withContext(Dispatchers.IO) {
        val cleanUrl = extractTikTokUrl(rawUrl)
        if (cleanUrl.isBlank()) {
            onProgress(DownloadState.Error("URL de TikTok no válida. Introduce un enlace válido."))
            return@withContext null
        }

        try {
            onProgress(DownloadState.Loading("Obteniendo información del vídeo...", 5))

            var videoDownloadUrl: String? = null
            var videoTitle: String = "TikTok Video"

            // 1. Try TikWM API
            try {
                val encodedUrl = URLEncoder.encode(cleanUrl, "UTF-8")
                val requestUrl = "https://www.tikwm.com/api/?url=$encodedUrl"
                val request = Request.Builder()
                    .url(requestUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string()
                    if (response.isSuccessful && !bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        val code = json.optInt("code", -1)
                        if (code == 0 && json.has("data")) {
                            val data = json.getJSONObject("data")
                            videoTitle = data.optString("title", "TikTok Video")
                            val playUrl = data.optString("play")
                            val hdPlayUrl = data.optString("hdplay")

                            val rawPlay = if (hdPlayUrl.isNotBlank() && !hdPlayUrl.startsWith("null")) hdPlayUrl else playUrl
                            if (rawPlay.isNotBlank()) {
                                videoDownloadUrl = if (rawPlay.startsWith("/")) "https://www.tikwm.com$rawPlay" else rawPlay
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "TikWM API error: ${e.message}")
            }

            // 2. Fallback if TikWM failed
            if (videoDownloadUrl.isNullOrBlank()) {
                try {
                    val encodedUrl = URLEncoder.encode(cleanUrl, "UTF-8")
                    val requestUrl = "https://api.tiklydown.eu.org/api/download?url=$encodedUrl"
                    val request = Request.Builder()
                        .url(requestUrl)
                        .header("User-Agent", "Mozilla/5.0")
                        .build()

                    client.newCall(request).execute().use { response ->
                        val bodyString = response.body?.string()
                        if (response.isSuccessful && !bodyString.isNullOrBlank()) {
                            val json = JSONObject(bodyString)
                            if (json.has("video")) {
                                val videoObj = json.getJSONObject("video")
                                videoDownloadUrl = videoObj.optString("noWatermark")
                                if (videoDownloadUrl.isNullOrBlank()) {
                                    videoDownloadUrl = videoObj.optString("watermark")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "TikLyDown API error: ${e.message}")
                }
            }

            if (videoDownloadUrl.isNullOrBlank()) {
                onProgress(DownloadState.Error("No se pudo extraer el vídeo de TikTok. Verifica que la cuenta no sea privada."))
                return@withContext null
            }

            onProgress(DownloadState.Loading("Iniciando descarga sin marca de agua...", 15))

            // Download file
            val fileRequest = Request.Builder()
                .url(videoDownloadUrl!!)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            client.newCall(fileRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    onProgress(DownloadState.Error("Error al descargar el archivo (${response.code})."))
                    return@withContext null
                }

                val body = response.body
                if (body == null) {
                    onProgress(DownloadState.Error("La respuesta de TikTok estuvo vacía."))
                    return@withContext null
                }

                val contentLength = body.contentLength()
                val outputFile = File(context.filesDir, "tiktok_wallpaper_${System.currentTimeMillis()}.mp4")

                body.byteStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                val constrainedProgress = progress.coerceIn(15, 98)
                                onProgress(DownloadState.Loading("Descargando vídeo ($constrainedProgress%)...", constrainedProgress))
                            }
                        }
                        outputStream.flush()
                    }
                }

                val fileUri = Uri.fromFile(outputFile)
                onProgress(DownloadState.Success(fileUri, videoTitle))
                return@withContext fileUri
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading TikTok video", e)
            onProgress(DownloadState.Error("Error de red: ${e.localizedMessage ?: "Fallo de conexión"}"))
            return@withContext null
        }
    }

    private fun extractTikTokUrl(input: String): String {
        val trimmed = input.trim()
        val regex = Regex("(https?://[\\w-]+\\.[\\w-]+/[\\S]+)")
        val match = regex.find(trimmed)
        return match?.value ?: if (trimmed.startsWith("http")) trimmed else ""
    }
}
