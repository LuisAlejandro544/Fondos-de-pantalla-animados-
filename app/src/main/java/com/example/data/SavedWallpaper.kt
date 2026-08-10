package com.example.data

data class SavedWallpaper(
    val id: String,
    val title: String,
    val uriString: String,
    val isLiveVideo: Boolean,
    val resolutionText: String,
    val fileSizeMB: Float,
    val timestamp: Long,
    val isCurrent: Boolean = false
)
