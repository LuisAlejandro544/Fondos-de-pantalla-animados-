package com.example.ui

data class VideoResolutionInfo(
    val width: Int,
    val height: Int
) {
    val effectiveHeight: Int get() = minOf(width, height)
    val aspectRatioLabel: String get() {
        val minDim = minOf(width, height)
        val maxDim = maxOf(width, height)
        return when {
            minDim >= 2160 -> "$maxDim × $minDim (4K Ultra HD)"
            minDim >= 1440 -> "$maxDim × $minDim (2K / 1440p QHD)"
            minDim >= 1080 -> "$maxDim × $minDim (1080p Full HD)"
            minDim >= 720 -> "$maxDim × $minDim (720p HD)"
            else -> "$maxDim × $minDim (${minDim}p SD)"
        }
    }
}
