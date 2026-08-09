# 🧠 AI Context - Video Fondo

Este documento resume el contexto técnico del proyecto **Video Fondo** para asistentes de IA y desarrolladores.

## 🎯 Objetivo General
Proporcionar una aplicación Android moderna, ligera y estilizada que permita seleccionar vídeos de la galería y establecerlos como fondo de pantalla animado con control de audio, previsualización interactiva y preparación para optimización nativa.

## 🎨 Guía Visual y Estilo
- **Colores prohibidos**: No utilizar tonos neón, futuristas, verdes estridentes ni estética cyberpunk.
- **Paleta elegida**: Slate Navy (`#0F172A`), Índigo sobrio (`#6366F1`) y Azul Cielo suave (`#38BDF8`).
- **Recursos separados**: Los logotipos en formato `.svg` deben almacenarse en `/assets/logos/` y la configuración general en `/assets/app_config.json`.

## ⚙️ Reglas de Comportamiento del Slider de Sonido
1. **Pulsar Silenciar / Interruptor**:
   - `isMuted = true`
   - El volumen guardado se establece en `0.0f` y la barra indicadora muestra `0%`.
   - Se preserva en memoria/SharedPreferences el valor previo no nulo (`KEY_LAST_NON_ZERO_VOLUME`).
2. **Desactivar Silencio / Mute off**:
   - `isMuted = false`
   - El volumen se restaura exactamente al último porcentaje configurado (por defecto 70%).
3. **Mover el Slider de Volumen**:
   - Mover el slider a un valor > 0 desactiva automáticamente el modo silencio.
   - Mover el slider a 0 activa el modo silencio.

## 🖼️ Respaldo y Restauración de Fondo Estático Original
1. **Captura Inicial**:
   - Antes de aplicar el fondo de pantalla animado por primera vez, se realiza un respaldo en PNG del fondo de pantalla estático original del usuario (`original_wallpaper.png`).
2. **Preservación Inmutable**:
   - Si el usuario aplica múltiples fondos de vídeo consecutivos, la app conserva únicamente el fondo estático **original inicial** sin sobreescribirlo.
3. **Restauración en un Clic**:
   - Se incluye el botón **Restaurar fondo de pantalla estático** para volver al fondo original en cualquier momento.

## 💻 Integración Nativa (C++ / Rust)
- El proyecto incluye la integración de `CMakeLists.txt` en `app/build.gradle.kts`.
- El módulo `VideoNativeBridge.kt` gestiona la carga de la librería `videowallpaper_native`.
- C++ sirve como capa de enlace JNI y Rust (`/cpp/rust`) contendrá la lógica de alto rendimiento para el procesamiento y compresión de fotogramas de vídeo para ahorro energético.

## 🚀 GitHub Actions CI/CD Pipelines
- **Compilación de APK**: `.github/workflows/build-apk.yml` (`workflow_dispatch`).
  1. Descarga el repositorio completo incluyendo código Kotlin, C++ y Rust.
  2. Configura JDK 17, Android SDK y toolchain de Rust con targets para arquitecturas Android.
  3. Aplica caché para Gradle y Cargo/Rust.
  4. Genera una llave de firma JKS "en caliente" en tiempo de ejecución de la acción.
  5. Ejecuta `./gradlew assembleDebug` y firma el APK resultante con `apksigner`.
  6. Publica el artefacto `VideoFondo-Debug-Signed`.

- **Sincronización desde ZIP**: `.github/workflows/process-zip-sync.yml` (trigger en `zips/*.zip`).
  1. Detecta la subida de un paquete `.zip` en la carpeta `zips/`.
  2. Descomprime y sincroniza el repositorio actualizando código nuevo, sobrescribiendo archivos modificados, eliminando obsoletos e **incluyendo modificaciones o adiciones a la carpeta `.github` y Workflows/Actions**.
  3. Utiliza los permisos de GitHub `contents: write` y `workflows: write`.
  4. Evalúa dinámicamente secrets para la autenticación de GitHub (`GH_PAT`, `PAT_TOKEN`, `REPO_TOKEN`, `GITHUB_TOKEN`, `TOKEN`).
  5. Realiza commit y push automático de los cambios a la rama principal.

