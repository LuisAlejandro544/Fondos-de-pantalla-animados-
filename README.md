# Video Fondo (Video Wallpaper)

**Video Fondo** es una aplicación para Android desarrollada con Kotlin y Jetpack Compose que permite establecer cualquier vídeo de la galería del dispositivo o **descargado por enlace desde TikTok (sin marca de agua)** como fondo de pantalla animado (*Live Wallpaper*), con controles avanzados de audio, ajuste de pantalla y reducción inteligente de resolución para ahorro de batería.

---

## 🚀 Características Principales

- **Vídeo de Galería como Fondo**: Selector con `PickVisualMedia` para elegir cualquier archivo de vídeo local.
- **Descargador de TikTok Integrado**:
  - Pega cualquier enlace público de TikTok (formatos cortos `vt.tiktok.com` o estándar).
  - Descarga automática del vídeo **sin marca de agua** directamente en el almacenamiento interno de la app.
  - Indicador de progreso en tiempo real y mensaje de estado.
  - Aplicación automática del vídeo descargado como fondo animado con acceso instantáneo a todas las opciones de personalización.
- **Control Inteligente de Audio**:
  - Interruptor de silencio (*Mute*).
  - Al silenciar, el control deslizable de volumen se posiciona automáticamente en 0%.
  - Al reactivar el sonido, se restaura suavemente el último nivel de volumen seleccionado.
- **Modos de Ajuste de Pantalla**:
  - **Recortar (*Crop*)**: Llena toda la pantalla manteniendo la proporción.
  - **Ajustar (*Fit*)**: Muestra el vídeo completo sin cortes.
  - **Llenar (*Stretch*)**: Estira el vídeo para cubrir los bordes.
- **Respaldo y Restauración de Fondo Original**:
  - Respalda automáticamente la imagen de fondo estática que el usuario tenía antes de activar el fondo de vídeo.
  - Mantiene intacta únicamente la imagen original primaria, sin sobreescribirla si el usuario aplica varios vídeos seguidos.
  - Permite restaurar el fondo de pantalla estático anterior con un solo toque en "Restaurar fondo de pantalla estático".
- **Previsualización interactiva**: Reproductor integrado para verificar el fondo antes de aplicarlo.
- **Servicio Nativo de Wallpaper**: `VideoWallpaperService` optimizado para bajo consumo de batería en segundo plano.
- **Estructura C++/Rust NDK integrada**: Motor nativo directo con `ANativeWindow` y `NdkMediaCodec` (HEVC / H.264 Hardware Direct Decoding) para renderizado de alto rendimiento sin pausas de memoria (Zero-GC abstractions).
- **Protección Térmica Anti-Sobrecalentamiento y Ahorro Batería**:
  - **Pausa Inmediata en Invisibilidad y Apagado de Pantalla**: Suspensión instantánea del decodificador NDK al apagar la pantalla o al abrir otra aplicación a pantalla completa.
  - **Decodificación Hardware Directa (NATIVE HEVC / H.264)**: Pasa los fotogramas directamente de la GPU al búfer de `ANativeWindow`.
  - **Desactivación de Subsistemas de Audio (AudioFlinger Bypass)**: Al silenciar el fondo, desactiva por completo los hilos de decodificación y mezcla de audio DSP.
- **Detección de Resolución en Tiempo Real & Control Inteligente**:
  - Detección automática en tiempo real de las dimensiones originales del vídeo (ancho, alto y orientación), tanto para vídeos locales como descargados de TikTok.
  - Bloqueo de selecciones de resolución superiores al vídeo de origen para evitar reescalados innecesarios y consumo absurdo de GPU.
- **Ajustes Avanzados & Calidad 4K (Control de Batería y Resolución)**:
  - Interruptor para activar/desactivar el **Motor Nativo C++/Rust NDK**.
  - Interruptor para **Ahorro Extremo de Batería** (reducción de consumo hasta un 60%).
  - Interruptor para **Filtro de Nitidez Perceptual NDK** (mantiene apariencia tipo 4K en pantallas móviles).
  - Selector de resoluciones dinámicas: **Original (Nativa)**, **1080p Inteligente**, **720p Ecológico** y **540p Máximo Ahorro** (con deshabilitación automática de opciones no aptas).
- **Respaldo y Gestión de Fondo de Pantalla**:
  - Respalda automáticamente el fondo estático previo en alta calidad.
  - Botón de **Restaurar fondo de pantalla estático respaldado** (con fallback automático a limpiar el live wallpaper de fábrica).
  - Botón directo para **Abrir selector de fondos de Android** del sistema.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Arquitectura**: MVVM con `StateFlow` y `SharedPreferences` persistentes
- **Networking & Downloading**: `OkHttp3`, `JSONObject`, permisos de `INTERNET` para la extracción de vídeos de TikTok sin marca de agua.
- **Multimedia**: Android `MediaPlayer` & `WallpaperService` (`SurfaceHolder`)
- **Nativo**: C++ (`CMakeLists.txt`, `native-lib.cpp`) y Rust (`Cargo.toml`, `lib.rs`)

---

## 📦 Requisitos Previos e Instalación

1. **Android Studio**: versión Jellyfish o superior.
2. **SDK de Android**: `compileSdk = 36`, `minSdk = 24`.
3. **NDK de Android**: (Opcional) para compilación de los componentes nativos C++/Rust.

### Ejecución
```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/video-fondo.git

# Abrir en Android Studio y sincronizar Gradle
./gradlew assembleDebug
```

---

## 🤖 Workflows de GitHub Actions

El repositorio incluye dos **Workflows de GitHub Actions**:

### 1. Compilación de APK Firmado (`build-apk.yml`)
Configurado para activación manual (*manual trigger* / `workflow_dispatch`):
- **Descarga completa de código**: Compila la app completa, incluyendo dependencias de Kotlin, C++ (`CMake`) y Rust (`Cargo`).
- **Sistema de Caché**: Utiliza caché para Gradle y para las dependencias de Cargo/Rust.
- **Generación de Firma en Caliente**: Genera dinámicamente un almacén de claves (`debug_action.jks`) en la propia ejecución para firmar el APK resultador con `apksigner`.
- **Artefacto Descargable**: Produce el APK firmado como un artefacto `VideoFondo-Debug-Signed` descargable directamente desde la pestaña **Actions** en GitHub.

### 2. Sincronización Automática desde ZIP (`process-zip-sync.yml`)
Se activa automáticamente cuando se sube un archivo `.zip` a la carpeta `zips/`:
- **Procesamiento de Código Completo**: Descarga y descomprime el código contenido en el archivo `.zip`.
- **Sincronización Total (Incluyendo `.github`)**: Añade nuevos archivos, reemplaza código antiguo y actualiza/modifica acciones y workflows en la carpeta `.github` (conservando únicamente `.git` y `zips/`).
- **Permisos Elevados**: Incluye los permisos `contents: write` y `actions: write` para permitir la modificación o adición de código y GitHub Actions.
- **Búsqueda de Token en Secrets**: Busca tokens de GitHub entre las claves de Secrets (`GH_PAT`, `PAT_TOKEN`, `REPO_TOKEN`, `GITHUB_TOKEN`, `TOKEN`).
- **Commit Automático**: Guarda y realiza un push de los cambios sincronizados a la rama principal.

