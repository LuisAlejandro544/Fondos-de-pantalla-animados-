package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WallpaperConfig
import com.example.ui.VideoResolutionInfo

@Composable
fun AdvancedSettingsCard(
    config: WallpaperConfig,
    videoResolutionInfo: VideoResolutionInfo?,
    onUseNativeEngineChanged: (Boolean) -> Unit,
    onUseBatterySaverChanged: (Boolean) -> Unit,
    onQualityResolutionIndexChanged: (Int) -> Unit,
    onHardwareSharpnessChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Rendimiento y Ahorro de Batería",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rendimiento y Ahorro de Batería",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ajusta la fluidez del vídeo y el consumo de energía para que tu teléfono funcione rápido y la batería dure más.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Switch 1: Native Engine
            SettingSwitchRow(
                icon = Icons.Default.Memory,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Reproducción Eficiente Ultra Suave",
                subtitle = "Usa el motor de alta velocidad para que el vídeo se reproduzca fluido y sin interrupciones",
                checked = config.useNativeEngine,
                onCheckedChange = onUseNativeEngineChanged,
                testTag = "use_native_engine_switch"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Switch 2: Battery Saver
            SettingSwitchRow(
                icon = Icons.Default.BatterySaver,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Ahorro de Batería Máximo",
                subtitle = "Reduce el consumo de energía hasta un 60% optimizando la velocidad del vídeo",
                checked = config.useBatterySaver,
                onCheckedChange = onUseBatterySaverChanged,
                testTag = "use_battery_saver_switch"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Switch 3: Hardware Sharpness
            SettingSwitchRow(
                icon = Icons.Default.HighQuality,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Mejora de Nitidez de Imagen",
                subtitle = "Mantiene el vídeo nítido y claro incluso si eliges una resolución menor",
                checked = config.hardwareSharpness,
                onCheckedChange = onHardwareSharpnessChanged,
                testTag = "hardware_sharpness_switch"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resolution Selector
            ResolutionSelector(
                videoResolutionInfo = videoResolutionInfo,
                currentQualityIndex = config.qualityResolutionIndex,
                onQualityResolutionIndexChanged = onQualityResolutionIndexChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            EngineStatusCard(
                useNativeEngine = config.useNativeEngine,
                useBatterySaver = config.useBatterySaver,
                qualityResolutionIndex = config.qualityResolutionIndex
            )
        }
    }
}
