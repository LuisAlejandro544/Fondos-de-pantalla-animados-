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
│           │   │   └── WallpaperPreferences.kt # Persistencia local de configuración
│           │   ├── native/
│           │   │   └── VideoNativeBridge.kt  # Clase puente Kotlin <-> C++/Rust JNI
│           │   ├── service/
│           │   │   └── VideoWallpaperService.kt # Engine del Live Wallpaper
│           │   └── ui/
│           │       ├── WallpaperMainScreen.kt# Pantalla principal con Jetpack Compose
│           │       ├── WallpaperViewModel.kt # ViewModel de estado
│           │       ├── components/
│           │       │   ├── AdvancedSettingsCard.kt # Card de opciones avanzadas NDK, Batería y Resoluciones
│           │       │   ├── SoundControlsCard.kt    # Card de control de volumen y ajuste
│           │       │   └── VideoPreviewCard.kt     # Card de previsualización de vídeo
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
