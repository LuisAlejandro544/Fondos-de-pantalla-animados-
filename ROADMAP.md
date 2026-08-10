# 🗺️ Roadmap de Desarrollo - Video Fondo

Este documento detalla las fases de desarrollo planificadas para la evolución de la aplicación **Video Fondo**.

---

## 🟢 Fase 1: MVP Funcional (Completado)
- [x] Selector de vídeo desde la galería mediante Android Photo Picker (`PickVisualMedia`).
- [x] Motor de fondo de pantalla en vivo mediante `WallpaperService` y `MediaPlayer`.
- [x] Control de volumen con slider y comportamiento inteligente de silencio (al silenciar vuelve a 0%, al desilenciar restaura el valor previo).
- [x] Previsualización interactiva con botón de reproducción/pausa.
- [x] Respaldo y restauración del fondo de pantalla estático original de la imagen anterior del usuario.
- [x] Paleta de colores cómoda y sobria (Índigo/Azul Pizarra) sin neones ni cyberpunk.
- [x] Archivo `app_config.json` e imágenes/vectores SVG independientes en `/assets/`.

---

## 🟡 Fase 2: Integración Nativa C++ / Rust NDK y Optimización Batería/4K (Completado)
- [x] Estructura CMake (`CMakeLists.txt`) y archivo nativo C++ (`native-lib.cpp`) vinculado con bibliotecas Android `android` y `mediandk`.
- [x] Módulo Rust (`Cargo.toml` y `lib.rs`) integrado para optimización y abstracciones de cero costo de memoria (Zero GC).
- [x] Acceso directo a `ANativeWindow` y ajuste geométrico de búfer directo (`configureNativeWindowSurface`).
- [x] Pausa inmediata en invisibilidad y apagado de pantalla mediante `BroadcastReceiver` (`ACTION_SCREEN_OFF` / `ACTION_SCREEN_ON`) para control térmico anti-sobrecalentamiento.
- [x] Decodificación Hardware Directa (NATIVE HEVC / H.264) con salida directa a Surface.
- [x] Desactivación y bypass de subsistemas de audio (AudioFlinger / AudioTrack) cuando el fondo está silenciado.
- [x] Detección en tiempo real de la resolución del vídeo (`MediaMetadataRetriever`) y bloqueo automático de opciones de resolución mayores a la fuente.
- [x] Escalado inteligente de resolución para vídeos 4K/HD con selector multirresolución (1080p, 720p, 540p) conservando densidad de nitidez.
- [x] Controles de encendido/apagado para Motor Nativo C++, Ahorro de Batería Máximo, Nitidez de Imagen y Compresión Inteligente de Archivo.
- [x] Descargador de vídeos de TikTok sin marca de agua integrado mediante el modal **"Poner link de TikTok"** en la sección "Añadir Nuevo Vídeo", con barra de progreso en tiempo real y redirección automática al editor al completar la descarga.
- [x] Integración de descargas con detección de resolución automática, downscaling (1080p, 720p, 540p) y controles de audio.
- [x] Botón directo para restaurar el fondo respaldado o abrir el selector de fondos nativo del sistema.
- [x] **Galería Independiente de Fondos (`WallpaperGalleryScreen`)**: Pantalla inicial predeterminada con colección de fondos guardados, filtros (Todos, Animados, Estáticos) y eliminación.
- [x] **Navegación Directa con Botón de Regreso**: Transición a la pantalla de Ajustes y Motor con botón de regreso "Regresar a Galería" sin barra inferior invasiva.
- [x] **Motor NDK Rust de Downscaling (`RustVideoOptimizer`)**: Reducción permanente automática de resolución a 720p con preservación de nitidez nativa perceptual (Lanczos / Unsharp Mask).
- [x] **Modo Fondo Dinámico Día / Noche Automático**:
  - [x] Selección dual de vídeos para día y noche con horario personalizable (predeterminado 06:00 - 18:00 h / 18:00 - 06:00 h).
  - [x] Conmutación automática sin interrupciones mediante `BroadcastReceiver` (`ACTION_TIME_TICK` / `ACTION_TIME_CHANGED`).
  - [x] Componentes de interfaz dedicados (`DayNightWallpaperCard`) y asignación instantánea en la Galería (botones "Día ☀️" y "Noche 🌙").
- [x] **Pantalla Completa de Ajustes de la Aplicación (`AppSettingsScreen`)**: Interfaz dedicada para personalización de temas visuales (Slate Navy, AMOLED, Material You, etc.) y términos legales en lugar de un modal emergente.
- [x] **Despliegue de Términos y Condiciones en GitHub Pages**: Enlace oficial de Términos y Condiciones (https://luisalejandro544.github.io/Fondos-de-pantalla-animados-/) publicado mediante el workflow automatizado (`deploy-pages.yml`).
- [x] **Motor de Compresión Real de Vídeo H.264/AVC (`RealVideoCompressor`)**: Re-codificación física por hardware con `MediaCodec`, `MediaExtractor` y `MediaMuxer` para transcodificación con reducción genuina de MB.
- [x] **Gestión Inteligente de Audio**:
  - [x] Enfoque de audio automático para silenciar/pausar ante reproducción de Spotify, YouTube o llamadas entrantes.
  - [x] Fundido suave de audio (Fade-In) al regresar a la pantalla de inicio.
  - [x] Modo Noche Silencioso automático entre las 22:00 y las 07:00 h.
  - [x] Sincronización perfecta del slider de volumen y restauración de volumen no nulo.
- [x] **Acceso Directo a Añadir Vídeo en la Cabecera**: Reemplazado el botón superior de ajustes por el botón directo "[📹 Añadir Vídeo]" en la Galería de Fondos.
- [x] **Presentaciones Animadas en la Galería (`VideoGalleryItemThumbnail`)**: Miniaturas en movimiento en bucle silencioso dentro de cada tarjeta de la galería para explorar visualmente sin adivinar nombres.
- [x] **Previsualización Reactiva Instantánea**: Actualización en tiempo real del reproductor de previsualización al cambiar o seleccionar cualquier vídeo.
- [x] **Detección Automática de Fondo "EN USO"**: Identificación activa en tiempo real con distintivo destacado "✔ EN USO" y botones dinámicos en las tarjetas de la galería.
- [x] **Restauración Directa de Fondo Estático Original**: Tarjeta persistente y botón dedicado en la Galería para cambiar al fondo de pantalla estático original con un toque.
- [x] **Botones de Acción Rápida en la Cabecera de la Galería**: Accesos directos permanentes `[Añadir Vídeo]` y `[Link TikTok]` para cargar nuevo contenido de forma inmediata.
- [x] **Selección Directa de Vídeo y Compresión Diferida**: Carga instantánea del vídeo original en la pantalla de edición (`ENGINE_SETTINGS`) al seleccionar de la galería sin comprimir previamente; la compresión de vídeo Rust solo se ejecuta al presionar "ESTABLECER COMO FONDO".
- [x] **Detección y Pausa Automática por Batería Baja (Ahorro +15%)**: Congelado automático de la reproducción de vídeo al llegar al 15% o menos de carga (sin estar cargando) con reanudación al conectar el cargador.
- [x] **Persistencia de Permisos de URIs Locales**: Otorgamiento y conservación persistente de permisos de lectura (`takePersistableUriPermission`) en la selección de vídeos locales, modo Día/Noche y Galería guardada.
- [x] **Ajuste para Tiendas Alternativas (Uptodown)**: Eliminación total de dependencias de Google Play Services para distribución abierta e instalación directa.
- [x] **Filtros Visuales y Efecto Blur en Tiempo Real (Ajuste para el Launcher)**:
  - [x] Motor OpenGL ES 2.0 con Shaders GLSL personalizados en `VideoGlFilterRenderer`.
  - [x] Deslizador de Desenfoque (*Blur*) de 0% a 100% para difuminar el fondo animado facilitando la lectura de iconos y widgets del launcher.
  - [x] Controles de Brillo (-50% a +50%), Contraste (0.5x a 1.5x) y Saturación (0% B&N a 200% Ultra Vívido).
  - [x] Fichas de Filtro de Color (*Normal*, *Oscuro Launcher*, *Sepia*, *Cyberpunk*, *Noche Confort*).
  - [x] Botón de restablecimiento de filtros.
- [x] **Cambio de Fondo por Clima y Salida/Puesta de Sol Real (Sin Google Play Services)**:
  - [x] Integración con la API de Open-Meteo, IP Geolocation (`ip-api.com`) y `LocationManager` nativo de Android sin depender de `play-services-location`.
  - [x] Asignación de vídeos por condición climática (*Soleado*, *Lluvia*, *Nublado*, *Nieve*).
  - [x] Cálculo Astronómico Offline de Salida y Puesta de Sol en Kotlin (*Solar Astronomical Calculator*).
  - [x] Tarjeta interactiva de estado del clima en tiempo real (`WeatherSolarCard`) con temperatura, ubicación y botón de actualización manual.
- [x] **Páginas Legales y Política de Privacidad en GitHub Pages**:
  - [x] Creación de `privacy.html` en `/docs/` con el mismo estilo responsivo e identidad visual que `index.html`.
  - [x] Incorporación del botón de acceso a la Política de Privacidad en `AppSettingsScreen.kt` con enlace a GitHub Pages y fallback informativo.
- [x] Pipeline de Integración Continua con GitHub Actions (`build-apk.yml` con `workflow_dispatch`).
- [x] Firma de APK Debug con generación de llave en caliente (`keytool`) y caché de dependencias Gradle y Cargo.
- [x] Workflow de descompresión y sincronización automática desde archivos `.zip` subidos a `zips/` (`process-zip-sync.yml`).

---

## 🔵 Fase 3: Funcionalidades Avanzadas (En Planificación)
- [ ] Recorte de segmentos de vídeo (elegir inicio y fin).
- [ ] Compatibilidad con múltiples fondos rotativos (Playlist de vídeos).
- [ ] Soporte para efectos de filtros simples en tiempo real (Brillo, Contraste, Desenfoque NDK).
