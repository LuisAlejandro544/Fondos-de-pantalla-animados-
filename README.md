# Video Fondo (Video Wallpaper)

**Video Fondo** es una aplicación para Android desarrollada con Kotlin y Jetpack Compose que permite establecer cualquier vídeo de la galería del dispositivo o **descargado por enlace desde TikTok (sin marca de agua)** como fondo de pantalla animado (*Live Wallpaper*), con galería independiente de fondos guardados, motor de reducción y optimización nativa en **Rust**, controles avanzados de audio y reducción inteligente de consumo de batería.

---

## 🚀 Características Principales

- **Modo Fondo Dinámico Día / Noche Automático**:
  - Cambio automático de fondo animado según la hora del sistema (Día: 06:00 - 18:00 h / Noche: 18:00 - 06:00 h).
  - Receptor en tiempo real (`Intent.ACTION_TIME_TICK`) que conmuta el vídeo activo exactamente cuando cambia el horario sin reinicios bruscos.
  - Asignación rápida con un toque desde la Galería de Fondos (botones "Día ☀️" y "Noche 🌙") o desde la tarjeta dedicada en Ajustes.
  - Indicador visual de estado que muestra el período activo actual y el vídeo en reproducción.
- **Galería Independiente de Fondos con Presentación Animada (Pantalla Principal)**:
  - Pantalla inicial directa al abrir la app con colección persistente de fondos de pantalla.
  - **Presentación en Miniatura Animada**: Cada tarjeta de la galería cuenta con un reproductor en bucle silencioso (`VideoGalleryItemThumbnail`) de bajo consumo de RAM que muestra una vista previa en movimiento del vídeo.
  - Botón directo **"[📹 Añadir Vídeo]"** ubicado en el encabezado superior para seleccionar o descargar nuevos vídeos al instante.
  - Filtros para explorar fondos **Todos**, **Animados (Live Video)** o **Estáticos**.
  - Tarjeta de aplicación rápida y gestión de eliminación de fondos guardados.
- **Menú de Ajustes de la Aplicación (Pantalla Independiente)**:
  - Pantalla dedicada y completa de **Ajustes de la Aplicación** (`AppSettingsScreen`) en lugar de un cuadro emergente.
  - Acceso directo mediante el **icono de engranaje** ubicado en el encabezado de la Galería y en la pantalla de Ajustes.
  - Cambio de tema de color en tiempo real con persistencia automática en las preferencias de la app:
    - **Slate & Índigo (Original)**: Tono elegante predeterminado Slate Navy e índigo.
    - **Oscuro 100% (Negro Puro AMOLED)**: Fondo `#000000` para máximo ahorro de batería en pantallas OLED.
    - **Material You (Colores Dinámicos)**: Se sincroniza dinámicamente con la paleta de tu sistema Android 12+.
    - **Azul Océano**: Tonalidades marítimas profundas con acentos cian brillantes.
    - **Verde Esmeralda**: Estilo bosque oscuro elegante con tonos verde esmeralda.
    - **Violeta Ciberpunk**: Atmósfera nocturna púrpura con destellos neón magenta.
  - Acceso directo a los **Términos y Condiciones** alojados en GitHub Pages ([Ver Términos Oficiales](https://luisalejandro544.github.io/Fondos-de-pantalla-animados-/)).
- **Navegación Fluida con Botón de Regreso**:
  - Transición limpia hacia la pantalla de **Ajustes y Motor**.
  - Botón superior de navegación **"Regresar a Galería"** para volver atrás instantáneamente sin barras inferiores invasivas.
- **Motor de Optimización en Rust (`RustVideoOptimizer`)**:
  - Reducción permanente automática de resolución a 720p HD conservando la nitidez nativa perceptual (*Unsharp Mask / Lanczos*).
  - Ahorro drástico de memoria RAM y temperatura de la GPU al ejecutar vídeos animados en segundo plano.
  - Diálogo interactivo de procesamiento con porcentaje de progreso en tiempo real.
- **Compatibilidad con Tiendas Alternativas (Uptodown)**:
  - Cero dependencia de Google Play Services ni paquetes propietarios de Google.
  - Preparado para distribución libre en plataformas como **Uptodown**.
- **Vídeo de Galería como Fondo**: Selector con `PickVisualMedia` para elegir cualquier archivo de vídeo local.
- **Descargador de TikTok Integrado ("Poner link de TikTok")**:
  - Opción destacada con diálogo modal desplegable en la sección "Añadir Nuevo Vídeo" de la Galería.
  - Pega cualquier enlace público de TikTok (formatos cortos `vt.tiktok.com` o estándar).
  - Descarga automática del vídeo **sin marca de agua** directamente en el almacenamiento interno de la app.
  - Muestra una barra de carga e indicador de progreso porcentual en tiempo real durante la descarga.
  - Al completarse la descarga, redirige automáticamente al usuario a la pantalla del editor para previsualizar y configurar el fondo animado.
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
  - Permite restaurar el fondo de pantalla estático anterior con un solo toque desde la pantalla de Ajustes o desde la Galería.
- **Previsualización interactiva y Selección Directa**:
  - Al seleccionar un vídeo de la galería, se abre directamente en la pantalla de edición sin comprimirse previamente.
  - La compresión de vídeo Rust sólo se ejecuta cuando el usuario presiona "ESTABLECER COMO FONDO".
- **Servicio Nativo de Wallpaper**: `VideoWallpaperService` optimizado para bajo consumo de batería en segundo plano.
- **Estructura C++/Rust NDK integrada**: Motor nativo directo con `ANativeWindow` y `NdkMediaCodec` (HEVC / H.264 Hardware Direct Decoding) para renderizado de alto rendimiento sin pausas de memoria (Zero-GC abstractions).
- **Protección Térmica Anti-Sobrecalentamiento y Ahorro Batería**:
  - **Pausa por Batería Baja (Ahorro +15%)**: Monitorea el estado de la batería mediante `BroadcastReceiver` (`ACTION_BATTERY_CHANGED`) y congela la animación cuando la carga es de 15% o menor (sin estar cargando), ahorrando un 15% extra de energía para emergencias. Se reanuda automáticamente al conectar el cargador.
  - **Pausa Inmediata en Invisibilidad y Apagado de Pantalla**: Suspensión instantánea del decodificador NDK al apagar la pantalla o al abrir otra aplicación a pantalla completa.
  - **Decodificación Hardware Directa (NATIVE HEVC / H.264)**: Pasa los fotogramas directamente de la GPU al búfer de `ANativeWindow`.
  - **Desactivación de Subsistemas de Audio (AudioFlinger Bypass)**: Al silenciar el fondo, desactiva por completo los hilos de decodificación y mezcla de audio DSP.
- **Persistencia de Permisos de URIs Locales**:
  - Solicita y conserva permisos persistentes de lectura (`takePersistableUriPermission`) para todos los archivos de vídeo seleccionados en la galería local, modo Día/Noche y biblioteca de fondos guardados, previniendo fallos de acceso tras reiniciar el dispositivo.
- **Detección de Resolución en Tiempo Real & Control Inteligente**:
  - Detección automática en tiempo real de las dimensiones originales del vídeo (ancho, alto y orientación).
  - Bloqueo de selecciones de resolución superiores al vídeo de origen para evitar reescalados innecesarios y consumo absurdo de GPU.
- **Ajustes Avanzados & Rendimiento**:
  - Interruptor para activar/desactivar el **Motor Nativo C++/Rust NDK**.
  - Interruptor para **Ahorro de Batería Máximo** (reduce el consumo hasta un 60%).
  - Interruptor para **Mejora de Nitidez de Imagen** (mantiene nitidez y claridad).
  - Interruptor para **Compresión Inteligente de Archivo** (reorganiza y reescala el vídeo mediante Rust a 720p HD conservando nitidez perceptual para un consumo mínimo de espacio, memoria RAM y energía).
  - Selector de resoluciones dinámicas: **Original (Máxima Calidad)**, **1080p Alta**, **720p Normal** y **540p Máximo Ahorro**.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin & Rust / C++ NDK
- **UI Framework**: Jetpack Compose (Material Design 3 - Paleta Slate & Indigo)
- **Arquitectura**: MVVM con `StateFlow`, `SharedPreferences` y persistencia JSON local
- **Networking & Downloading**: `OkHttp3`, `JSONObject`, permisos de `INTERNET` para extracción de TikTok sin marca de agua
- **Motor NDK / Rust**: `ANativeWindow`, `NdkMediaCodec`, Rust Downscaler (720p Sharpness Engine)
- **Distribución Objetivo**: Uptodown / APK firmado independiente (Cero Google Play Services required)

---

## 📦 Requisitos Previos e Instalación

1. **Android Studio**: versión Jellyfish o superior.
2. **SDK de Android**: `compileSdk = 36`, `minSdk = 24`.
3. **NDK de Android**: para compilación de los componentes nativos C++/Rust.

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
- **Artefacto Descargable**: Produce el APK firmado como un artefacto `VideoFondo-Debug-Signed` descargable directamente para subir a Uptodown o instalar directamente.

### 2. Sincronización Automática desde ZIP (`process-zip-sync.yml`)
Se activa automáticamente cuando se sube un archivo `.zip` a la carpeta `zips/`:
- **Procesamiento de Código Completo**: Descarga y descomprime el código contenido en el archivo `.zip`.
- **Sincronización Total (Incluyendo `.github`)**: Añade nuevos archivos, reemplaza código antiguo y actualiza/modifica acciones y workflows en la carpeta `.github`.
- **Commit Automático**: Guarda y realiza un push de los cambios sincronizados a la rama principal.

