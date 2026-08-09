# Video Fondo (Video Wallpaper)

**Video Fondo** es una aplicación para Android desarrollada con Kotlin y Jetpack Compose que permite establecer cualquier vídeo de la galería del dispositivo como fondo de pantalla animado (*Live Wallpaper*), con controles avanzados de audio y ajuste de pantalla.

---

## 🚀 Características Principales

- **Vídeo de Galería como Fondo**: Selector con `PickVisualMedia` para elegir cualquier archivo de vídeo local.
- **Control Inteligente de Audio**:
  - Interruptor de silencio (*Mute*).
  - Al silenciar, el control deslizable de volumen se posiciona automáticamente en 0%.
  - Al reactivar el sonido, se restaura suavemente el último nivel de volumen seleccionado.
- **Modos de Ajuste de Pantalla**:
  - **Recortar (*Crop*)**: Rena toda la pantalla manteniendo la proporción.
  - **Ajustar (*Fit*)**: Muestra el vídeo completo sin cortes.
  - **Llenar (*Stretch*)**: Estira el vídeo para cubrir los bordes.
- **Respaldo y Restauración de Fondo Original**:
  - Respalda automáticamente la imagen de fondo estática que el usuario tenía antes de activar el fondo de vídeo.
  - Mantiene intacta únicamente la imagen original primaria, sin sobreescribirla si el usuario aplica varios vídeos seguidos.
  - Permite restaurar el fondo de pantalla estático anterior con un solo toque en "Restaurar fondo de pantalla estático".
- **Previsualización interactiva**: Reproductor integrado para verificar el fondo antes de aplicarlo.
- **Servicio Nativo de Wallpaper**: `VideoWallpaperService` optimizado para bajo consumo de batería en segundo plano.
- **Estructura C++/Rust preparada**: Configuración inicial con `CMakeLists.txt` y módulo `Cargo.toml` para futuras optimizaciones y compresión de vídeo a nivel nativo.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Arquitectura**: MVVM con `StateFlow` y `SharedPreferences` persistentes
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
- **Permisos Extendidos de Workflows**: Incluye los permisos `contents: write` y `workflows: write` para permitir la modificación o adición de GitHub Actions.
- **Búsqueda de Token en Secrets**: Busca tokens de GitHub entre las claves de Secrets (`GH_PAT`, `PAT_TOKEN`, `REPO_TOKEN`, `GITHUB_TOKEN`, `TOKEN`).
- **Commit Automático**: Guarda y realiza un push de los cambios sincronizados a la rama principal.

