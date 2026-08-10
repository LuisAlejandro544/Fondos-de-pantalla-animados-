package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WallpaperConfig
import com.example.ui.VideoResolutionInfo

data class OptionInfoDetails(
    val title: String,
    val activatedEffect: String,
    val deactivatedEffect: String,
    val pros: List<String>,
    val cons: List<String>,
    val idealUseCases: String
)

@Composable
fun AdvancedSettingsCard(
    config: WallpaperConfig,
    videoResolutionInfo: VideoResolutionInfo?,
    onUseNativeEngineChanged: (Boolean) -> Unit,
    onUseBatterySaverChanged: (Boolean) -> Unit,
    onQualityResolutionIndexChanged: (Int) -> Unit,
    onHardwareSharpnessChanged: (Boolean) -> Unit,
    onUseVideoCompressionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInfoOption by remember { mutableStateOf<OptionInfoDetails?>(null) }

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
                testTag = "use_native_engine_switch",
                onInfoClick = {
                    selectedInfoOption = OptionInfoDetails(
                        title = "Reproducción Eficiente Ultra Suave (Motor C++)",
                        activatedEffect = "Envía los cuadros del vídeo directo a la pantalla por el motor gráfico rápido del teléfono. Evita que la memoria se llene o cause tirones.",
                        deactivatedEffect = "Usa el reproductor básico de Android. Funciona bien en vídeos sencillos, pero puede pausarse un segundo en vídeos muy pesados.",
                        pros = listOf("Vídeos 4K y Full HD fluidos sin pausas", "El teléfono no se calienta", "Menor carga para el procesador"),
                        cons = listOf("Usa un poco más de memoria inicial al arrancar el fondo"),
                        idealUseCases = "Ideal si pones vídeos largos de alta calidad, grabaciones en 4K o vídeos descargados de TikTok en teléfonos de gama media y alta."
                    )
                }
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
                testTag = "use_battery_saver_switch",
                onInfoClick = {
                    selectedInfoOption = OptionInfoDetails(
                        title = "Ahorro de Batería Máximo",
                        activatedEffect = "Adapta la velocidad del vídeo a un ritmo económico (~30 FPS) y detiene el procesamiento en segundo plano cuando abres otras aplicaciones.",
                        deactivatedEffect = "El vídeo reproduce continuamente a la máxima velocidad posible de la pantalla (hasta 60 FPS).",
                        pros = listOf("Ahorra hasta un 60% de energía", "Mantiene la batería fría durante todo el día", "Aumenta las horas de uso de la batería"),
                        cons = listOf("El movimiento del vídeo se ve un poco menos acelerado"),
                        idealUseCases = "Recomendado para todos los días, especialmente si pasas mucho tiempo fuera de casa o la batería de tu teléfono dura poco."
                    )
                }
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
                testTag = "hardware_sharpness_switch",
                onInfoClick = {
                    selectedInfoOption = OptionInfoDetails(
                        title = "Mejora de Nitidez de Imagen",
                        activatedEffect = "Aplica un realce automático en los bordes de los objetos del vídeo para mantener los detalles bien definidos.",
                        deactivatedEffect = "Muestra los colores y líneas exactamente tal como están en el archivo original sin retoques.",
                        pros = listOf("Permite bajar la resolución del vídeo a 720p ahorrando batería sin perder nitidez visual", "Colores y letras más definidos"),
                        cons = listOf("Aumenta ligeramente el uso de la tarjeta gráfica"),
                        idealUseCases = "Perfecto si bajaste la calidad del vídeo a 1080p o 720p para ahorrar batería pero quieres que se siga viendo impecable."
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Switch 4: Video Compression
            SettingSwitchRow(
                icon = Icons.Default.Compress,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Compresión Inteligente de Archivo",
                subtitle = "Reduce el peso del vídeo mediante el motor Rust conservando nitidez HD nativa para menor consumo de espacio, RAM y batería",
                checked = config.useVideoCompression,
                onCheckedChange = onUseVideoCompressionChanged,
                testTag = "use_video_compression_switch",
                onInfoClick = {
                    selectedInfoOption = OptionInfoDetails(
                        title = "Compresión Inteligente de Archivo (Motor Rust)",
                        activatedEffect = "Procesa el vídeo con el motor Rust de alta velocidad, reescalando a 720p HD y preservando la nitidez nativa para un consumo mínimo de memoria RAM y almacenamiento.",
                        deactivatedEffect = "Mantiene el archivo de vídeo original con su peso y dimensiones intactos sin el procesamiento de Rust.",
                        pros = listOf("Ahorro drástico de almacenamiento y memoria RAM", "Carga ultra rápida y reproducción sin sobrecalentar la GPU", "Mantiene bordes y detalles nítidos sin borrosidad"),
                        cons = listOf("Procesa el archivo durante unos segundos al seleccionar o descargar el vídeo"),
                        idealUseCases = "Ideal para cualquier vídeo de TikTok o de la galería en teléfonos de cualquier gama para garantizar máximo rendimiento y batería."
                    )
                }
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
                config = config
            )
        }
    }

    // Modal de Información sobre la opción seleccionada
    selectedInfoOption?.let { details ->
        AlertDialog(
            onDismissRequest = { selectedInfoOption = null },
            title = {
                Text(
                    text = details.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "⚡ ¿Qué pasa al encenderla?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = details.activatedEffect,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🔌 ¿Qué pasa al apagarla?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = details.deactivatedEffect,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "✅ Ventajas principales",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    details.pros.forEach { pro ->
                        Text(
                            text = "• $pro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⚠️ A tener en cuenta",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    details.cons.forEach { con ->
                        Text(
                            text = "• $con",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💡 ¿Cuándo usarla?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = details.idealUseCases,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedInfoOption = null }) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

