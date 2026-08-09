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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdvancedSettingsCard
import com.example.ui.components.ApplyWallpaperBottomBar
import com.example.ui.components.MainHeaderBar
import com.example.ui.components.RestoreWallpaperCard
import com.example.ui.components.SoundControlsCard
import com.example.ui.components.TikTokDownloadCard
import com.example.ui.components.VideoPreviewCard
import com.example.ui.components.WallpaperStatusCard

@Composable
fun WallpaperMainScreen(
    viewModel: WallpaperViewModel
) {
    val context = LocalContext.current
    val config by viewModel.configState.collectAsState()
    val hasOriginalBackup by viewModel.hasOriginalBackup.collectAsState()
    val videoResolutionInfo by viewModel.videoResolutionInfo.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    var isWallpaperActive by remember { mutableStateOf(false) }

    // Video Gallery Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.backupOriginalWallpaperIfNeeded(context)
            viewModel.onVideoSelected(context, uri, context.contentResolver)
            Toast.makeText(context, "Vídeo cargado correctamente", Toast.LENGTH_SHORT).show()
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Bar
            MainHeaderBar()

            Spacer(modifier = Modifier.height(20.dp))

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
                onHardwareSharpnessChanged = { viewModel.onHardwareSharpnessChanged(it) }
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
}
