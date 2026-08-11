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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedWallpaper
import com.example.data.WallpaperConfig
import java.util.Calendar

@Composable
fun DayNightWallpaperCard(
    config: WallpaperConfig,
    onDayNightToggle: (Boolean) -> Unit,
    onSelectDayVideoClicked: () -> Unit,
    onSelectNightVideoClicked: () -> Unit,
    savedWallpapers: List<SavedWallpaper> = emptyList(),
    onEditVideoClicked: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isDay = currentHour in config.dayStartHour until config.nightStartHour

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_night_wallpaper_card"),
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (config.isDayNightEnabled)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = "Icono Día Noche",
                        tint = if (isDay) Color(0xFFFFB300) else Color(0xFF7C4DFF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fondo Día / Noche Automático",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cambia el vídeo según la hora del sistema",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = config.isDayNightEnabled,
                    onCheckedChange = onDayNightToggle,
                    modifier = Modifier.testTag("day_night_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            AnimatedVisibility(visible = config.isDayNightEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Status Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDay) Color(0xFFFFF8E1) else Color(0xFF311B92).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = if (isDay) Color(0xFFF57F17) else Color(0xFFB388FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isDay)
                                    "☀️ Es de DÍA (%02d:00 h). Fondo activo: Día".format(currentHour)
                                else
                                    "🌙 Es de NOCHE (%02d:00 h). Fondo activo: Noche".format(currentHour),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDay) Color(0xFFE65100) else Color(0xFFD1C4E9)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Day Video Card Slot
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = Color(0xFFFF8F00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fondo de Día (%02d:00 - %02d:00)".format(config.dayStartHour, config.nightStartHour),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isDay) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFF8F00)
                                    ) {
                                        Text(
                                            text = "ACTIVO AHORA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            val dayWallpaper = savedWallpapers.find { it.uriString == config.dayVideoUri }
                            val dayTitle = dayWallpaper?.title ?: if (config.dayVideoUri.isNotBlank()) config.dayVideoUri.substringAfterLast("/") else "Sin vídeo de Día"

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (config.dayVideoUri.isNotBlank())
                                    "☀️ Vídeo asignado: $dayTitle"
                                else
                                    "Sin vídeo específico de Día (usando vídeo principal)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (config.dayVideoUri.isNotBlank()) Color(0xFFFF8F00) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (config.dayVideoUri.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onSelectDayVideoClicked,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("select_day_video_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Asignar Día", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (config.dayVideoUri.isNotBlank() && onEditVideoClicked != null) {
                                    Button(
                                        onClick = { onEditVideoClicked(config.dayVideoUri) },
                                        modifier = Modifier.testTag("edit_day_video_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Editar Día",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✏️ Editar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Night Video Card Slot
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = Color(0xFF7C4DFF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fondo de Noche (%02d:00 - %02d:00)".format(config.nightStartHour, config.dayStartHour),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!isDay) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF7C4DFF)
                                    ) {
                                        Text(
                                            text = "ACTIVO AHORA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            val nightWallpaper = savedWallpapers.find { it.uriString == config.nightVideoUri }
                            val nightTitle = nightWallpaper?.title ?: if (config.nightVideoUri.isNotBlank()) config.nightVideoUri.substringAfterLast("/") else "Sin vídeo de Noche"

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (config.nightVideoUri.isNotBlank())
                                    "🌙 Vídeo asignado: $nightTitle"
                                else
                                    "Sin vídeo específico de Noche (usando vídeo principal)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (config.nightVideoUri.isNotBlank()) Color(0xFF7C4DFF) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (config.nightVideoUri.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onSelectNightVideoClicked,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("select_night_video_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Asignar Noche", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (config.nightVideoUri.isNotBlank() && onEditVideoClicked != null) {
                                    Button(
                                        onClick = { onEditVideoClicked(config.nightVideoUri) },
                                        modifier = Modifier.testTag("edit_night_video_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Editar Noche",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✏️ Editar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
