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
- [x] Descargador de vídeos de TikTok sin marca de agua integrado por enlace público (soporte `vt.tiktok.com` y URLs largas).
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
- [x] **Despliegue de Términos y Condiciones en GitHub Pages**: Workflow automatizado (`deploy-pages.yml`) que publica la página HTML estática de términos y condiciones en GitHub Pages.
- [x] **Ajuste para Tiendas Alternativas (Uptodown)**: Eliminación total de dependencias de Google Play Services para distribución abierta e instalación directa.
- [x] Pipeline de Integración Continua con GitHub Actions (`build-apk.yml` con `workflow_dispatch`).
- [x] Firma de APK Debug con generación de llave en caliente (`keytool`) y caché de dependencias Gradle y Cargo.
- [x] Workflow de descompresión y sincronización automática desde archivos `.zip` subidos a `zips/` (`process-zip-sync.yml`).

---

## 🔵 Fase 3: Funcionalidades Avanzadas (En Planificación)
- [ ] Recorte de segmentos de vídeo (elegir inicio y fin).
- [ ] Compatibilidad con múltiples fondos rotativos (Playlist de vídeos).
- [ ] Soporte para efectos de filtros simples en tiempo real (Brillo, Contraste, Desenfoque NDK).
- [ ] Detección automática de nivel de batería bajo para pausar la animación al llegar al 15%.
