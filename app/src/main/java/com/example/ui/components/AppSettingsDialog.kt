package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ThemeOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val accentColor: Color
)

private val THEME_OPTIONS = listOf(
    ThemeOption(
        id = "SLATE_INDIGO",
        title = "Slate & Índigo (Original)",
        subtitle = "Paleta sobria con fondo slate azulado e índigo suave",
        primaryColor = Color(0xFF6366F1),
        backgroundColor = Color(0xFF0F172A),
        accentColor = Color(0xFF38BDF8)
    ),
    ThemeOption(
        id = "AMOLED_DARK",
        title = "Oscuro 100% (Negro Puro AMOLED)",
        subtitle = "Negro absoluto (#000000) para ahorrar el máximo de batería en pantallas OLED",
        primaryColor = Color(0xFF818CF8),
        backgroundColor = Color(0xFF000000),
        accentColor = Color(0xFF38BDF8)
    ),
    ThemeOption(
        id = "MATERIAL_YOU",
        title = "Material You (Colores Dinámicos)",
        subtitle = "Se adapta automáticamente al fondo de pantalla de tu dispositivo Android 12+",
        primaryColor = Color(0xFF38BDF8),
        backgroundColor = Color(0xFF1E293B),
        accentColor = Color(0xFF818CF8)
    ),
    ThemeOption(
        id = "OCEAN_BLUE",
        title = "Azul Océano",
        subtitle = "Tonalidades marítimas profundas con acentos cian brillantes",
        primaryColor = Color(0xFF00D2FF),
        backgroundColor = Color(0xFF031525),
        accentColor = Color(0xFF3A7BD5)
    ),
    ThemeOption(
        id = "EMERALD_GREEN",
        title = "Verde Esmeralda",
        subtitle = "Estilo bosque oscuro elegante con tonos verdes relajantes",
        primaryColor = Color(0xFF10B981),
        backgroundColor = Color(0xFF062016),
        accentColor = Color(0xFF34D399)
    ),
    ThemeOption(
        id = "CYBER_VIOLET",
        title = "Violeta Ciberpunk",
        subtitle = "Atmósfera nocturna púrpura con destellos neón magenta",
        primaryColor = Color(0xFFD946EF),
        backgroundColor = Color(0xFF180A2A),
        accentColor = Color(0xFFA855F7)
    )
)

@Composable
fun AppSettingsDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Ajustes de la Aplicación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Personaliza el color y la apariencia visual",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Tema",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tema de Color del Sistema:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                THEME_OPTIONS.forEach { theme ->
                    val isSelected = currentTheme == theme.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onThemeSelected(theme.id) }
                            .testTag("theme_option_${theme.id.lowercase()}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                            )
                        } else null,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onThemeSelected(theme.id) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = theme.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Palette Badges
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(theme.backgroundColor)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(theme.accentColor)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("close_settings_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Listo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Listo", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
