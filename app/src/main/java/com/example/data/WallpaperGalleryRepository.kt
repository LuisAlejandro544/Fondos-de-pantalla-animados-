package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperGalleryRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, SavedWallpaper::class.java)
    private val adapter = moshi.adapter<List<SavedWallpaper>>(listType)

    private val _wallpapersFlow = MutableStateFlow<List<SavedWallpaper>>(emptyList())
    val wallpapersFlow: StateFlow<List<SavedWallpaper>> = _wallpapersFlow.asStateFlow()

    init {
        loadWallpapers()
    }

    fun loadWallpapers() {
        val json = prefs.getString(KEY_GALLERY_JSON, null)
        if (json.isNullOrBlank()) {
            _wallpapersFlow.value = emptyList()
        } else {
            try {
                val list = adapter.fromJson(json) ?: emptyList()
                _wallpapersFlow.value = list
            } catch (e: Exception) {
                _wallpapersFlow.value = emptyList()
            }
        }
    }

    fun addWallpaper(wallpaper: SavedWallpaper) {
        val currentList = _wallpapersFlow.value.toMutableList()
        // Remove existing if same ID
        currentList.removeAll { it.id == wallpaper.id }
        
        // Mark others as not current if this new one is set as current
        val updatedList = if (wallpaper.isCurrent) {
            currentList.map { it.copy(isCurrent = false) }.toMutableList()
        } else {
            currentList
        }
        
        updatedList.add(0, wallpaper)
        saveList(updatedList)
    }

    fun setCurrentWallpaper(id: String) {
        val updated = _wallpapersFlow.value.map { item ->
            item.copy(isCurrent = item.id == id)
        }
        saveList(updated)
    }

    fun deleteWallpaper(id: String) {
        val updated = _wallpapersFlow.value.filterNot { it.id == id }
        saveList(updated)
    }

    private fun saveList(list: List<SavedWallpaper>) {
        try {
            val json = adapter.toJson(list)
            prefs.edit().putString(KEY_GALLERY_JSON, json).apply()
            _wallpapersFlow.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PREFS_NAME = "wallpaper_gallery_prefs"
        private const val KEY_GALLERY_JSON = "key_gallery_json"
    }
}
