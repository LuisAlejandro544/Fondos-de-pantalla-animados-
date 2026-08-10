# 🧠 AI Context - Video Fondo

Este documento resume el contexto técnico del proyecto **Video Fondo** para asistentes de IA y desarrolladores.

## 🎯 Objetivo General
Proporcionar una aplicación Android moderna, ligera y estilizada que permita seleccionar vídeos de la galería y establecerlos como fondo de pantalla animado con control de audio, previsualización interactiva y preparación para optimización nativa.

## 🎨 Guía Visual y Estilo
- **Colores prohibidos**: No utilizar tonos neón, futuristas, verdes estridentes ni estética cyberpunk.
- **Paleta elegida**: Slate Navy (`#0F172A`), Índigo sobrio (`#6366F1`) y Azul Cielo suave (`#38BDF8`).
- **Recursos separados**: Los logotipos en formato `.svg` deben almacenarse en `/assets/logos/` y la configuración general en `/assets/app_config.json`.

## 🖼️ Galería Independiente de Fondos y Navegación
1. **Inicio Predeterminado**:
   - La aplicación inicia directamente en la **Galería de Fondos** (`WallpaperGalleryScreen`), mostrando todos los fondos de vídeo o estáticos guardados por el usuario.
   - Incluye filtros interactivos (**Todos**, **Animados**, **Estáticos**) y un acceso directo destacado **"[📹 Añadir Vídeo]"** en la cabecera superior para seleccionar o descargar nuevos vídeos sin rodeos.
   - Presentación de miniatura animada (`VideoGalleryItemThumbnail`) en cada tarjeta de la galería que reproduce el vídeo en un bucle silencioso para que el usuario pueda explorar visualmente sin adivinar por nombre.
2. **Navegación con Botón de Regreso**:
   - Al pulsar el icono de motor o engranaje, navega a la pantalla de **Ajustes y Motor** (`WallpaperMainScreen`) o de **Ajustes de la App** (`AppSettingsScreen`).
   - En la pantalla de Ajustes, un botón superior **"Regresar a Galería"** permite volver instantáneamente a la Galería sin barras inferiores fijas.
3. **Pantalla Independiente de Ajustes de la Aplicación (`AppSettingsScreen`)**:
   - Acceso desde el icono de engranaje en la barra superior.
   - Pantalla completa independiente para cambiar temas cromáticos y consultar términos y condiciones desplegados en GitHub Pages (`https://luisalejandro544.github.io/Fondos-de-pantalla-animados-/`).
4. **Persistencia Local (`WallpaperGalleryRepository`)**:
   - Guarda los metadatos de los fondos seleccionados/descargados en un archivo JSON local (`wallpaper_gallery.json`) en la memoria interna de la app.

## ☀️🌙 Modo Fondo Dinámico Día / Noche Automático
1. **Detección por Hora del Sistema**:
   - `WallpaperPreferences` evalúa la hora del dispositivo (`Calendar.HOUR_OF_DAY`).
   - Período de Día por defecto: `06:00` a `18:00`. Período de Noche: `18:00` a `06:00`.
2. **Receptor de Cambio de Tiempo (`Intent.ACTION_TIME_TICK`)**:
   - `VideoWallpaperService` escucha latidos de tiempo del sistema (`ACTION_TIME_TICK`, `ACTION_TIME_CHANGED`).
   - Conmuta automáticamente y de forma imperceptible la reproducción entre el vídeo de Día (`dayVideoUri`) y el vídeo de Noche (`nightVideoUri`) al cruzar el umbral horario.
3. **Gestión en Interfaz (`DayNightWallpaperCard`)**:
   - Selector global para activar/desactivar el modo.
   - Banner de estado en vivo indicando la hora actual y qué vídeo está reproduciéndose.
   - Tarjetas dedicadas con botones de selección independiente para vídeo de Día y de Noche.
   - Asignación directa con un toque en los fondos de la Galería mediante los botones **Día ☀️** y **Noche 🌙**.

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

## 🎵 Descargador de TikTok sin Marca de Agua
1. **Módulo HTTP (`TikTokDownloader.kt`)**:
   - Extrae automáticamente la URL del vídeo a partir de enlaces compartidos de TikTok (`vt.tiktok.com` o `www.tiktok.com/@...`).
   - Consulta APIs de extracción sin marca de agua con OkHttp (TikWM API y fallback TikLyDown).
   - Descarga el MP4 directamente a la memoria interna protegida (`context.filesDir`) y notifica el progreso mediante `StateFlow<DownloadState>`.
2. **Componente de Interfaz Modal y Tarjeta (`TikTokDownloadDialog.kt` & `TikTokDownloadCard.kt`)**:
   - Botón modal **"Poner link de TikTok"** integrado en la sección "Añadir Nuevo Vídeo" de la Galería.
   - Diálogo desplegable modal con campo de texto interactivo, botón de pegar portapapeles y botón de limpiar.
   - Muestra una barra de carga e indicador de progreso porcentual en tiempo real durante la descarga.
   - Al finalizar la descarga, redirige automáticamente al usuario a la pantalla del editor de ajustes (`ENGINE_SETTINGS`) para previsualizar el vídeo descargado.
   - Integración automática con `WallpaperViewModel`: al completar la descarga, se asigna como vídeo activo, se activa el respaldo previo de fondo estático y se ejecuta `detectVideoResolution()` para ajustar las opciones de resolución.

## 💻 Integración Nativa NDK (C++ / Rust) & Control Térmico
- `CMakeLists.txt` vincula la biblioteca `videowallpaper_native` con `android` (`ANativeWindow`) y `mediandk` (`NdkMediaCodec`).
- **Prevención Térmica y Anti-Sobrecalentamiento**:
  - `BroadcastReceiver` registrado para `ACTION_SCREEN_OFF` / `ACTION_SCREEN_ON` para pausar la decodificación de vídeo de inmediato cuando la pantalla está apagada.
  - Suspensión instantánea en `onVisibilityChanged(false)` cuando otra app está en pantalla completa.
  - Bypass completo de `AudioFlinger` / `AudioTrack` al estar silenciado, liberando el chip DSP de audio.
- `VideoNativeBridge.kt` ofrece los métodos nativos:
  - `configureNativeWindowSurface`: Ajuste geométrico directo de búferes en C++ sin recolección de basura (Zero-GC).
  - `calculateOptimalResolution`: Ajuste de escalado de vídeos 4K/HD a 1080p, 720p o 540p conservando nitidez perceptual.
  - `getEngineStats`: Reporte de rendimiento en tiempo real del motor C++/Rust.
- Opciones configurables en `WallpaperPreferences.kt`:
  - `useNativeEngine` (Boolean): Activa/desactiva la aceleración NDK C++.
  - `useBatterySaver` (Boolean): Activa/desactiva el modo de bajo consumo energético.
  - `pauseOnLowBattery` (Boolean): Pausa automáticamente la reproducción del vídeo cuando la batería baja del 15% (sin estar cargando) para ahorrar un 15% adicional de energía.
  - `qualityResolutionIndex` (Int): Selecciona entre 0 (Original Nativa), 1 (1080p Inteligente), 2 (720p Eco) y 3 (540p Máx Batería).
- **Flujo de Selección Directa y Compresión Diferida**:
  - Al seleccionar un vídeo de la galería o TikTok, la aplicación carga directamente el archivo original sin comprimir y navega a la pantalla del editor (`ENGINE_SETTINGS`).
  - La compresión y el filtro de nitidez Rust (`RustVideoOptimizer.downscaleAndOptimizeVideo`) ocurren únicamente cuando el usuario presiona "ESTABLECER COMO FONDO" (`openWallpaperPicker`).
- **Detección de Nivel de Batería Bajo (Pausa por Batería Baja - Ahorro 15%)**:
  - `VideoWallpaperService` escucha `Intent.ACTION_BATTERY_CHANGED`, `ACTION_POWER_CONNECTED` y `ACTION_POWER_DISCONNECTED`.
  - Cuando el nivel de batería desciende a <= 15% y no está conectado a la corriente, congela el reproductor de vídeo para reservar energía hasta que se reconecte el cargador.
- **Persistencia de Permisos de URIs Locales**:
  - `WallpaperViewModel.tryPersistUriPermission` ejecuta `takePersistableUriPermission` con `FLAG_GRANT_READ_URI_PERMISSION` para vídeos de Galería, fondos guardados y vídeos de Día/Noche.
  - Garantiza que la aplicación conserve el acceso de lectura persistente a los archivos de vídeo seleccionados incluso tras reiniciar el dispositivo.
- **Detección de Resolución & Deshabilitación de Opciones Mayores**:
  - `WallpaperViewModel` extrae la resolución en tiempo real con `MediaMetadataRetriever`.
  - Deshabilita los chips de resolución superiores al vídeo cargado (`enabled = false`), impidiendo reescalados innecesarios y garantizando fallback a la resolución original si se cambia de vídeo.
  - `hardwareSharpness` (Boolean): Filtro de conservación de nitidez perceptual en resolución reducida.

## 🚀 GitHub Actions CI/CD Pipelines
- **Compilación de APK**: `.github/workflows/build-apk.yml` (`workflow_dispatch`).
  1. Descarga el repositorio completo incluyendo código Kotlin, C++ y Rust.
  2. Configura JDK 17, Android SDK y toolchain de Rust con targets para arquitecturas Android.
  3. Aplica caché para Gradle y Cargo/Rust.
  4. Genera una llave de firma JKS "en caliente" en tiempo de ejecución de la acción.
  5. Ejecuta `gradle assembleDebug` y firma el APK resultante con `apksigner`.
  6. Publica el artefacto `VideoFondo-Debug-Signed`.

- **Sincronización desde ZIP**: `.github/workflows/process-zip-sync.yml` (trigger en `zips/*.zip`).
  1. Detecta la subida de un paquete `.zip` en la carpeta `zips/`.
  2. Descomprime y sincroniza el repositorio actualizando código nuevo, sobrescribiendo archivos modificados, eliminando obsoletos e **incluyendo modificaciones o adiciones a la carpeta `.github` y Workflows/Actions**.
  3. Utiliza los permisos de GitHub `contents: write` y `actions: write`.
  4. Evalúa dinámicamente secrets para la autenticación de GitHub (`GH_PAT`, `PAT_TOKEN`, `REPO_TOKEN`, `GITHUB_TOKEN`, `TOKEN`).
  5. Realiza commit y push automático de los cambios a la rama principal.

