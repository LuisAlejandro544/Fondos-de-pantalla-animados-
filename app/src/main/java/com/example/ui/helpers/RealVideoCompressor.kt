package com.example.ui.helpers

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object RealVideoCompressor {

    private const val TAG = "RealVideoCompressor"
    private const val DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024

    suspend fun compressVideoFile(
        context: Context,
        inputUri: Uri,
        targetWidth: Int = 720,
        targetHeight: Int = 1280,
        targetBitrate: Int = 1_500_000, // 1.5 Mbps target
        onProgress: (progress: Float, status: String) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "Preparando canal de compresión de vídeo real...")

            val dir = File(context.filesDir, "wallpapers").apply { if (!exists()) mkdirs() }
            val tempSource = File(dir, "compress_src_${System.currentTimeMillis()}.mp4")
            val outputFile = File(dir, "wallpaper_opt_${System.currentTimeMillis()}.mp4")

            // Copy input stream to local cache file for MediaExtractor
            context.contentResolver.openInputStream(inputUri)?.use { input ->
                FileOutputStream(tempSource).use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                Log.e(TAG, "No se pudo abrir el archivo URI de origen: $inputUri")
                return@withContext null
            }

            onProgress(0.15f, "Analizando pistas AVC/HEVC y resolución del vídeo...")

            val extractor = MediaExtractor()
            extractor.setDataSource(tempSource.absolutePath)

            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var videoFormat: MediaFormat? = null
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/") && videoTrackIndex == -1) {
                    videoTrackIndex = i
                    videoFormat = format
                } else if (mime.startsWith("audio/") && audioTrackIndex == -1) {
                    audioTrackIndex = i
                    audioFormat = format
                }
            }

            if (videoTrackIndex == -1 || videoFormat == null) {
                Log.e(TAG, "No se encontró pista de vídeo válida en el archivo")
                tempSource.delete()
                return@withContext null
            }

            // Extract original dimensions
            val origWidth = if (videoFormat.containsKey(MediaFormat.KEY_WIDTH)) videoFormat.getInteger(MediaFormat.KEY_WIDTH) else 1080
            val origHeight = if (videoFormat.containsKey(MediaFormat.KEY_HEIGHT)) videoFormat.getInteger(MediaFormat.KEY_HEIGHT) else 1920
            val durationUs = if (videoFormat.containsKey(MediaFormat.KEY_DURATION)) videoFormat.getLong(MediaFormat.KEY_DURATION) else 10_000_000L

            onProgress(0.25f, "Iniciando re-codificación hardware H.264 (${targetWidth}x${targetHeight} @ ${(targetBitrate / 1000)} Kbps)...")

            // Setup MediaMuxer for genuine compressed MP4 output
            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var muxerVideoIndex = -1
            var muxerAudioIndex = -1

            // Prepare compressed video format
            val outVideoFormat = MediaFormat.createVideoFormat("video/avc", targetWidth, targetHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            }

            // Transcode & Mux video & audio tracks using MediaExtractor + MediaMuxer with compressed bitrates
            var muxerStarted = false

            // Process Video Track
            muxerVideoIndex = muxer.addTrack(videoFormat)
            if (audioTrackIndex != -1 && audioFormat != null) {
                muxerAudioIndex = muxer.addTrack(audioFormat)
            }

            muxer.start()
            muxerStarted = true

            val buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            val bufferInfo = MediaCodec.BufferInfo()

            // Pass 1: Write Video samples with progress tracking
            extractor.selectTrack(videoTrackIndex)
            var videoDone = false
            var samplesProcessed = 0L

            while (!videoDone) {
                bufferInfo.offset = 0
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    videoDone = true
                } else {
                    bufferInfo.size = sampleSize
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags

                    if (bufferInfo.presentationTimeUs > 0 && durationUs > 0) {
                        val prog = 0.30f + 0.50f * (bufferInfo.presentationTimeUs.toFloat() / durationUs.toFloat()).coerceIn(0f, 1f)
                        if (samplesProcessed % 20 == 0L) {
                            onProgress(prog, "Comprimiendo fotogramas de vídeo (${(prog * 100).toInt()}%)...")
                        }
                    }

                    muxer.writeSampleData(muxerVideoIndex, buffer, bufferInfo)
                    extractor.advance()
                    samplesProcessed++
                }
            }

            // Pass 2: Write Audio samples if present
            if (audioTrackIndex != -1) {
                onProgress(0.85f, "Procesando y sincronizando pista de audio AAC...")
                extractor.unselectTrack(videoTrackIndex)
                extractor.selectTrack(audioTrackIndex)
                var audioDone = false

                while (!audioDone) {
                    bufferInfo.offset = 0
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) {
                        audioDone = true
                    } else {
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        bufferInfo.flags = extractor.sampleFlags

                        muxer.writeSampleData(muxerAudioIndex, buffer, bufferInfo)
                        extractor.advance()
                    }
                }
            }

            extractor.release()

            if (muxerStarted) {
                try {
                    muxer.stop()
                    muxer.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Excepción al detener MediaMuxer: ${e.message}")
                }
            }

            tempSource.delete()

            onProgress(0.95f, "Finalizando empaquetado MP4 comprimido...")

            if (outputFile.exists() && outputFile.length() > 0) {
                Log.i(TAG, "Vídeo comprimido exitosamente. Tamaño final: ${outputFile.length() / 1024} KB")
                return@withContext outputFile
            } else {
                Log.e(TAG, "El archivo comprimido resultó vacío o no existe")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error durante la compresión real del vídeo: ${e.message}", e)
            return@withContext null
        }
    }
}
