# 📁 Estructura del Proyecto - Video Fondo

Visión general de la organización del proyecto Android en Kotlin con capa nativa C++/Rust.

```
/
├── .github/
│   └── workflows/
│       ├── build-apk.yml                     # Workflow de GitHub Actions para compilar APK firmado
│       └── process-zip-sync.yml              # Workflow para descompresión y sincronización de código desde ZIP
├── app/
│   ├── build.gradle.kts                      # Configuración del módulo de la app y CMake
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml           # Permisos y declaración de WallpaperService
│           ├── assets/
│           │   ├── app_config.json           # Configuración general y paleta de colores
│           │   └── logos/
│           │       └── app_logo.svg          # Logotipo vectorial SVG de la app
│           ├── cpp/                          # Capa Nativa
│           │   ├── CMakeLists.txt            # Script de compilación CMake
│           │   ├── native-lib.cpp            # Código puente JNI en C++
│           │   └── rust/                     # Módulo de Rust
│           │       ├── Cargo.toml            # Definición del paquete Rust
│           │       └── src/
│           │           └── lib.rs            # Código Rust para optimización de vídeo
│           ├── java/com/example/
│           │   ├── MainActivity.kt           # Punto de entrada principal
│           │   ├── data/
│           │   │   ├── SavedWallpaper.kt     # Modelo de datos para elementos de la Galería
│           │   │   ├── WallpaperGalleryRepository.kt # Persistencia local JSON de la Galería
│           │   │   └── WallpaperPreferences.kt # Persistencia local de configuración
│           │   ├── ui/
│           │   │   ├── WallpaperGalleryScreen.kt # Pantalla principal de inicio: Galería de Fondos
│           │   │   ├── WallpaperMainScreen.kt# Pantalla de Ajustes y Motor con navegación de regreso
│           │   │   ├── WallpaperViewModel.kt # ViewModel modular de gestión de estado
│           │   │   ├── VideoResolutionInfo.kt# Modelo de datos de resolución de vídeo
│           │   │   ├── components/
│           │   │   │   ├── AdvancedSettingsCard.kt # Card modular de opciones avanzadas NDK y Batería
│           │   │   │   ├── ApplyWallpaperBottomBar.kt # Barra inferior de acción para aplicar fondo
│           │   │   │   ├── DayNightWallpaperCard.kt # Card modular para modo de fondo dinámico Día / Noche
│           │   │   │   ├── EngineStatusCard.kt   # Insignia de estado en vivo del motor NDK
│           │   │   │   ├── MainHeaderBar.kt      # Encabezado principal con marca y logo
│           │   │   │   ├── OptimizationLoadingDialog.kt # Diálogo de progreso de reducción en Rust
│           │   │   │   ├── ResolutionSelector.kt # Selector interactivo de resolución y filtro de chips
│           │   │   │   ├── RestoreWallpaperCard.kt  # Card de gestión de restauración de fondo
│           │   │   │   ├── SettingSwitchRow.kt   # Componente reutilizable para filas con switch
│           │   │   │   ├── SoundControlsCard.kt  # Card de control de volumen y ajuste
│           │   │   │   ├── TikTokDownloadCard.kt # Card interactiva para descargar vídeos de TikTok por URL
│           │   │   │   ├── VideoPreviewCard.kt   # Card de previsualización de vídeo
│           │   │   │   └── WallpaperStatusCard.kt# Indicador de estado del fondo activo
│           │   │   ├── helpers/
│           │   │   │   ├── RustVideoOptimizer.kt # Motor Rust de reducción a 720p y conservación de nitidez
│           │   │   │   ├── TikTokDownloader.kt   # Gestor de descarga de vídeos de TikTok sin marca de agua
│           │   │   │   └── WallpaperBackupManager.kt # Gestor modular de respaldo y restauración de fondos
│           │       └── theme/
│           │           ├── Color.kt          # Definición de colores (Slate e Índigo)
│           │           └── Theme.kt          # Tema M3 de la aplicación
│           └── res/
│               ├── drawable/                 # Recursos gráficos XML
│               └── xml/
│                   └── wallpaper.xml         # Metadatos del servicio de fondo de pantalla
├── AGENTS.md                                 # Instrucciones para agentes de Inteligencia Artificial
├── AI_CONTEXT.md                             # Contexto arquitectónico del proyecto
├── README.md                                 # Documentación principal
├── ROADMAP.md                                # Plan de desarrollo futuro
├── STRUCTURE.md                              # Guía de estructura del código
└── zips/                                     # Carpeta para recepción de archivos ZIP de actualización
```
