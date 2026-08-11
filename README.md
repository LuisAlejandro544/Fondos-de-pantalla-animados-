# Video Fondo (Video Wallpaper)

**Video Fondo** es una aplicación para Android desarrollada con Kotlin y Jetpack Compose que permite establecer cualquier vídeo de la galería del dispositivo o **descargado por enlace desde TikTok (sin marca de agua)** como fondo de pantalla animado (*Live Wallpaper*), con galería independiente de fondos guardados, motor de reducción y optimización nativa en **Rust**, controles avanzados de audio y reducción inteligente de consumo de batería.

---

## 🚀 Características Principales

- **Filtros Visuales y Efecto Blur en Tiempo Real (Ajuste para el Launcher)**:
  - **Motor OpenGL ES 2.0 con Shaders GLSL**: Procesa el vídeo en tiempo real en la GPU a través de `VideoGlFilterRenderer` para un impacto mínimo en la batería.
  - **Efecto Desenfoque (Blur para Launcher)**: Permite ajustar el radio de desenfoque del 0% al 100% para difuminar suavemente el fondo animado, destacando los iconos y widgets del launcher del usuario mientras el vídeo sigue siendo claramente visible.
  - **Ajustes de Brillo, Contraste y Saturación**:
    - **Brillo / Oscurecimiento**: Ajustable de -50% a +50% para atenúar vídeos brillantes y asegurar la lectura de nombres de aplicaciones.
    - **Contraste**: Control deslizable de 0.5x a 1.5x.
    - **Saturación**: Ajustable de 0% (B&N Escala de Grises) a 200% (Modo Ultra Vívido).
  - **Modos de Filtros de Color**: Selección rápida con fichas interactiva entre *Normal*, *Oscuro Launcher*, *Sepia Cálido*, *Cyberpunk* y *Noche Confort*.
  - **Restablecimiento**: Botón para restaurar todos los valores de filtro por defecto con un toque.
- **Cambio de Fondo por Clima y Salida/Puesta de Sol Real (Sin Google Play Services)**:
  - **Compatible con Uptodown**: Implementado 100% con APIs estándar de Android (`LocationManager`), geolocalización por IP y la API libre e ilimitada de **Open-Meteo**. Cero dependencia de `play-services-location`.
  - **Vídeos según Condición Climática**: Asignación independiente de vídeos para *Soleado / Despejado ☀️*, *Lluvia / Tormenta 🌧️*, *Nublado / Niebla ☁️* y *Nieve / Helada ❄️*. Conmutación automática en tiempo real al cambiar el tiempo atmosférico.
  - **Cálculo Astronómico Sol Real (Amanecer / Atardecer)**: Motor offline en Kotlin que calcula la hora exacta del amanecer y atardecer real según la latitud y longitud del usuario. Funciona 100% sin conexión a internet.
  - **Tarjeta de Estado del Clima**: Muestra la ubicación detectada, temperatura actual (°C), icono climático y horarios de amanecer y atardecer con botón para forzar la actualización de ubicación y clima.
- **Motor de Compresión Real de Vídeo H.264/AVC (`RealVideoCompressor`)**:
  - Re-codificación física por hardware utilizando las APIs nativas de Android `MediaCodec`, `MediaExtractor` y `MediaMuxer`.
  - Re-escalado de dimensiones y ajuste de bitrate objetivo (p. ej. 1.5 Mbps @ 720p/540p) para generar un archivo MP4 comprimido genuino con reducción real de megabytes.
  - Sincronización automática con el motor de nitidez nativa en Rust (`RustVideoOptimizer`).
- **Gestión Inteligente de Audio**:
  - **Enfoque de Audio (Spotify / YouTube / Llamadas)**: Detecta reproducciones de música externas o llamadas activas mediante `AudioManager.OnAudioFocusChangeListener` e `isMusicActive`, silenciando o pausando el audio del fondo automáticamente para evitar interferencias.
  - **Fundido Suave de Audio (Fade-In)**: Aplica una rampa de volumen progresiva al regresar a la pantalla de inicio para una transición auditiva placentera.
  - **Modo Noche Silencioso**: Silencia el fondo de pantalla de forma automática en horario nocturno (22:00 - 07:00).
  - **Coherencia con el Slider de Volumen**: El control deslizable indica 0% al silenciar y restaura el nivel de volumen exacto al reactivarse.
- **Modo Fondo Dinámico Día / Noche Automático**:
  - Cambio automático de fondo animado según la hora del sistema (Día: 06:00 - 18:00 h / Noche: 18:00 - 06:00 h).
  - Receptor en tiempo real (`Intent.ACTION_TIME_TICK`) que conmuta el vídeo activo exactamente cuando cambia el horario sin reinicios bruscos.
  - **Selector Integrado con Badges `☀️ DÍA` y `🌙 NOCHE`**: Diálogo de selección nativo (`SelectWallpaperDialog`) que identifica con insignias claras qué fondos corresponden a Día o Noche y permite asignarlos con 1 solo toque.
  - **Indicadores Visibles y Botón "✏️ Editar" (1-Click)**: Tarjetas e ítems de galería que muestran qué vídeo está asignado como Día/Noche (y cuál está "ACTIVO AHORA") con botón dedicado "✏️ Editar" para ajustar directamente sus filtros visuales, velocidad, volumen o escala en la pantalla del motor.
  - **Almacenamiento Interno Persistente (Cero Problemas de Caché/Permisos)**: Todos los vídeos asignados a modos día/noche o clima se copian al almacenamiento interno permanente de la app (`files/wallpapers/`), resolviendo problemas de permisos expirados de `content://` y evitando que el vídeo antiguo se quede atascado en reproducción.
  - Indicador visual de estado y previsualización dinámica en tiempo real que refleja el fondo activo del slot.
- **Galería Independiente de Fondos con Detección "En Uso" y Acciones Rápidas**:
  - **Etiqueta "EN USO" Automática**: Identifica en tiempo real el fondo de pantalla animado activo en el sistema y resalta su tarjeta en la galería.
  - **Restauración al Fondo Estático Original**: Opción directa desde la galería para restaurar el fondo de pantalla estático original del sistema con un solo toque.
  - **Botones de Acción Rápida**: Accesos directos persistentes **"[📹 Añadir Vídeo]"** y **"[🔗 Link TikTok]"** en la cabecera para incorporar nuevo contenido sin pasos intermedios.
  - **Presentación en Miniatura Animada**: Cada tarjeta de la galería cuenta con un reproductor en bucle silencioso (`VideoGalleryItemThumbnail`) de bajo consumo de RAM que muestra una vista previa en movimiento del vídeo.
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
  - Acceso directo a los **Términos y Condiciones** y **Política de Privacidad** alojados en GitHub Pages ([Términos](https://luisalejandro544.github.io/Fondos-de-pantalla-animados-/) | [Política de Privacidad](https://luisalejandro544.github.io/Fondos-de-pantalla-animados-/privacy.html)).
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

---

## 📄 Licencia

Este proyecto está bajo la licencia **PolyForm Noncommercial License 1.0.0** (`PolyForm-Noncommercial-1.0.0`). 
Permite ver, usar localmente, modificar y redistribuir el código exclusivamente para **fines no comerciales** (uso personal, proyectos personales, investigación y estudio). No se permite el uso ni la publicación comercial. Para más detalles, consulta el archivo [`LICENSE`](./LICENSE).

