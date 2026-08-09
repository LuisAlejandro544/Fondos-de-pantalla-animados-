package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.VideoResolutionInfo

@Composable
fun ResolutionSelector(
    videoResolutionInfo: VideoResolutionInfo?,
    currentQualityIndex: Int,
    onQualityResolutionIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Detected Resolution Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HighQuality,
                contentDescription = "Resolución del vídeo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Resolución del vídeo detectada en tiempo real:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = videoResolutionInfo?.aspectRatioLabel ?: "1080 × 1920 (Ejemplo por defecto)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title and Info
        Text(
            text = "Resolución y Calidad del Vídeo",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Si tienes un vídeo muy pesado o de alta resolución (4K/HD), puedes ajustarlo para ahorrar batería:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        val sourceMinDim = videoResolutionInfo?.effectiveHeight ?: 1080

        val options = listOf(
            Triple("Original (Máxima Calidad)", 0, true),
            Triple("1080p Alta (-45% Batería)", 1, sourceMinDim > 1080),
            Triple("720p Normal (-65% Batería)", 2, sourceMinDim > 720),
            Triple("540p Máximo Ahorro", 3, sourceMinDim > 540)
        )

        Column {
            options.chunked(2).forEach { rowOptions ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowOptions.forEach { (label, index, isAllowed) ->
                        val isSelected = currentQualityIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isAllowed) {
                                    onQualityResolutionIndexChanged(index)
                                }
                            },
                            enabled = isAllowed,
                            label = {
                                Text(
                                    text = if (isAllowed) label else "$label (No apto)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("resolution_chip_$index"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}
