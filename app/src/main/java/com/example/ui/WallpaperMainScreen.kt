package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdvancedSettingsCard
import com.example.ui.components.AppSettingsDialog
import com.example.ui.components.ApplyWallpaperBottomBar
import com.example.ui.components.MainHeaderBar
import com.example.ui.components.OptimizationLoadingDialog
import com.example.ui.components.RestoreWallpaperCard
import com.example.ui.components.SoundControlsCard
import com.example.ui.components.TikTokDownloadCard
import com.example.ui.components.VideoPreviewCard
import com.example.ui.components.WallpaperStatusCard
import com.example.ui.helpers.OptimizationState

@Composable
fun WallpaperMainScreen(
    viewModel: WallpaperViewModel
) {
    val context = LocalContext.current
    val config by viewModel.configState.collectAsState()
    val hasOriginalBackup by viewModel.hasOriginalBackup.collectAsState()
    val videoResolutionInfo by viewModel.videoResolutionInfo.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val optimizationState by viewModel.optimizationState.collectAsState()
    val savedWallpapers by viewModel.savedWallpapers.collectAsState()

    var isWallpaperActive by remember { mutableStateOf(false) }
    // First screen is Gallery by default
    var showGallery by remember { mutableStateOf(true) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Video Gallery Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.backupOriginalWallpaperIfNeeded(context)
            viewModel.onVideoSelected(context, uri, context.contentResolver)
        }
    }

    LaunchedEffect(Unit) {
        isWallpaperActive = viewModel.isServiceActiveWallpaper(context)
        viewModel.checkOriginalBackup(context)
        viewModel.backupOriginalWallpaperIfNeeded(context)
    }

    LaunchedEffect(config.videoUri) {
        if (config.videoUri.isNotBlank()) {
            viewModel.detectVideoResolution(context, config.videoUri)
        }
    }

    // Optimization State Listener
    LaunchedEffect(optimizationState) {
        when (val state = optimizationState) {
            is OptimizationState.Success -> {
                Toast.makeText(
                    context,
                    "¡Vídeo reducido permanentemente a ${state.downscaledHeight}p conservando nitidez nativa!",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetOptimizationState()
            }
            is OptimizationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetOptimizationState()
            }
            else -> {}
        }
    }

    // Loading Screen for Rust Downscaling Processing
    if (optimizationState is OptimizationState.Processing) {
        OptimizationLoadingDialog(
            optimizationState = optimizationState as OptimizationState.Processing,
            onDismissRequest = {}
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!showGallery) {
                ApplyWallpaperBottomBar(
                    hasVideoSelected = config.videoUri.isNotBlank(),
                    onApplyClicked = {
                        if (config.videoUri.isBlank()) {
                            Toast.makeText(
                                context,
                                "Por favor elige primero un vídeo de la galería",
                                Toast.LENGTH_LONG
                            ).show()
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        } else {
                            viewModel.openWallpaperPicker(context)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (showGallery) {
            // Screen 1: Wallpaper Gallery (Default First Screen)
            WallpaperGalleryScreen(
                savedWallpapers = savedWallpapers,
                hasOriginalBackup = hasOriginalBackup,
                onApplyWallpaper = { savedItem ->
                    viewModel.applySavedWallpaper(savedItem)
                    viewModel.openWallpaperPicker(context)
                },
                onRestoreOriginalStaticWallpaper = {
                    viewModel.restoreOriginalWallpaper(context) { success ->
                        if (success) {
                            isWallpaperActive = viewModel.isServiceActiveWallpaper(context)
                            Toast.makeText(
                                context,
                                "Fondo estático original restaurado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onDeleteWallpaper = { id ->
                    viewModel.deleteSavedWallpaper(id)
                },
                onOpenGalleryPicker = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onNavigateToSettings = {
                    showGallery = false
                },
                onOpenAppSettings = {
                    showSettingsDialog = true
                }
            )
        } else {
            // Screen 2: Settings & NDK Engine ("Ajustes y Motor")
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Back to Gallery Button Row
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showGallery = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("back_to_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar a la Galería",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Regresar a Galería",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Header Bar
                MainHeaderBar(
                    onOpenSettings = { showSettingsDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Wallpaper Status Card
                WallpaperStatusCard(isWallpaperActive = isWallpaperActive)

                Spacer(modifier = Modifier.height(20.dp))

                // TikTok Downloader Card
                TikTokDownloadCard(
                    downloadState = downloadState,
                    onDownloadRequested = { url ->
                        viewModel.downloadTikTokVideo(context, url)
                    },
                    onResetDownloadState = {
                        viewModel.resetDownloadState()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Select Video CTA Button
                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("select_video_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Elegir vídeo de galería",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (config.videoUri.isBlank()) "Seleccionar vídeo de Galería" else "Cambiar Vídeo de Galería",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Video Preview Card
                VideoPreviewCard(
                    config = config,
                    onSelectVideoClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sound Controls & Scale Settings
                SoundControlsCard(
                    config = config,
                    onVolumeChanged = { viewModel.onVolumeChanged(it) },
                    onMuteToggled = { viewModel.onMuteToggled() },
                    onScaleModeChanged = { viewModel.onScaleModeChanged(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Advanced NDK & Battery / Resolution Settings Card
                AdvancedSettingsCard(
                    config = config,
                    videoResolutionInfo = videoResolutionInfo,
                    onUseNativeEngineChanged = { viewModel.onUseNativeEngineChanged(it) },
                    onUseBatterySaverChanged = { viewModel.onUseBatterySaverChanged(it) },
                    onQualityResolutionIndexChanged = { viewModel.onQualityResolutionIndexChanged(it) },
                    onHardwareSharpnessChanged = { viewModel.onHardwareSharpnessChanged(it) },
                    onUseVideoCompressionChanged = { viewModel.onUseVideoCompressionChanged(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Restore & System Wallpaper Management Card
                RestoreWallpaperCard(
                    hasOriginalBackup = hasOriginalBackup,
                    onRestoreClicked = {
                        viewModel.restoreOriginalWallpaper(context) { success ->
                            if (success) {
                                isWallpaperActive = viewModel.isServiceActiveWallpaper(context)
                                Toast.makeText(
                                    context,
                                    "Fondo de pantalla estático restaurado con éxito",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "No se pudo restaurar el fondo original",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onOpenSystemPickerClicked = {
                        viewModel.openSystemWallpaperPicker(context)
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (showSettingsDialog) {
            AppSettingsDialog(
                currentTheme = config.appTheme,
                onThemeSelected = { newTheme ->
                    viewModel.onAppThemeChanged(newTheme)
                },
                onDismissRequest = {
                    showSettingsDialog = false
                }
            )
        }
    }
}

