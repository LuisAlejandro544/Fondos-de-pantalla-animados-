package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WallpaperConfig
import com.example.helpers.WeatherSolarManager
import kotlinx.coroutines.launch

@Composable
fun WeatherSolarCard(
    config: WallpaperConfig,
    onWeatherToggle: (Boolean) -> Unit,
    onRealSolarToggle: (Boolean) -> Unit,
    onSelectSunnyVideoClicked: () -> Unit,
    onSelectRainyVideoClicked: () -> Unit,
    onSelectCloudyVideoClicked: () -> Unit,
    onSelectSnowyVideoClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_solar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0288D1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbCloudy,
                        contentDescription = "Fondo por Clima y Sol",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cambio por Clima y Sol Real",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Open-Meteo & Sol Astronómico (Sin Google Play)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch 1: Weather Wallpaper Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fondo según el Clima Actual",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cambia automáticamente el vídeo en días lluviosos, soleados o nublados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.isWeatherEnabled,
                    onCheckedChange = onWeatherToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF0288D1)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch 2: Real Astronomical Solar Sunrise/Sunset Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Salida/Puesta de Sol Real",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Usa la hora exacta del amanecer y atardecer real de tu ubicación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.isRealSolarEnabled,
                    onCheckedChange = onRealSolarToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF8F00)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Banner / Info Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = config.lastCityName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = config.lastWeatherTemp,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val condLabel = when (config.lastWeatherCondition) {
                            "RAIN" -> "🌧️ Lluvia / Tormenta"
                            "CLOUDS" -> "☁️ Nublado / Niebla"
                            "SNOW" -> "❄️ Nieve"
                            else -> "☀️ Despejado / Soleado"
                        }
                        Text(
                            text = "Estado: $condLabel",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🌅 Amanecer: ${config.lastSunriseTime} h  |  🌇 Atardecer: ${config.lastSunsetTime} h",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                WeatherSolarManager.updateWeatherAndSolar(context)
                                isRefreshing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRefreshing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0288D1)
                        )
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Consultando Clima y GPS...", fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Actualizar Clima y Ubicación", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Weather Video Slots
            AnimatedVisibility(visible = config.isWeatherEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Asignación de Vídeos por Clima",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Slot 1: Soleado
                    WeatherVideoSlotItem(
                        icon = Icons.Default.WbSunny,
                        iconTint = Color(0xFFFFB300),
                        title = "Soleado / Despejado ☀️",
                        assignedUri = config.sunnyVideoUri,
                        isActive = config.lastWeatherCondition == "CLEAR",
                        onAssignClick = onSelectSunnyVideoClicked
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slot 2: Lluvia
                    WeatherVideoSlotItem(
                        icon = Icons.Default.Thunderstorm,
                        iconTint = Color(0xFF0288D1),
                        title = "Lluvia / Tormenta 🌧️",
                        assignedUri = config.rainyVideoUri,
                        isActive = config.lastWeatherCondition == "RAIN",
                        onAssignClick = onSelectRainyVideoClicked
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slot 3: Nublado
                    WeatherVideoSlotItem(
                        icon = Icons.Default.Cloud,
                        iconTint = Color(0xFF78909C),
                        title = "Nublado / Niebla ☁️",
                        assignedUri = config.cloudyVideoUri,
                        isActive = config.lastWeatherCondition == "CLOUDS",
                        onAssignClick = onSelectCloudyVideoClicked
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slot 4: Nieve
                    WeatherVideoSlotItem(
                        icon = Icons.Default.AcUnit,
                        iconTint = Color(0xFF29B6F6),
                        title = "Nieve / Helada ❄️",
                        assignedUri = config.snowyVideoUri,
                        isActive = config.lastWeatherCondition == "SNOW",
                        onAssignClick = onSelectSnowyVideoClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherVideoSlotItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    assignedUri: String,
    isActive: Boolean,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = iconTint
                        ) {
                            Text(
                                text = "EN USO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (assignedUri.isNotBlank()) assignedUri.substringAfterLast("/") else "Sin vídeo asignado (usa el principal)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onAssignClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Asignar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
