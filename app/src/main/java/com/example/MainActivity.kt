package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WallpaperGalleryRepository
import com.example.data.WallpaperPreferences
import com.example.ui.WallpaperMainScreen
import com.example.ui.WallpaperViewModel
import com.example.ui.theme.VideoWallpaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = WallpaperPreferences(applicationContext)
        val galleryRepository = WallpaperGalleryRepository(applicationContext)
        val viewModel = ViewModelProvider(
            this,
            WallpaperViewModel.Factory(preferences, galleryRepository)
        )[WallpaperViewModel::class.java]

        setContent {
            val configState = viewModel.configState.collectAsStateWithLifecycle()
            VideoWallpaperTheme(appTheme = configState.value.appTheme) {
                WallpaperMainScreen(viewModel = viewModel)
            }
        }
    }
}

