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

## 🟡 Fase 2: Integración Nativa C++ / Rust y CI/CD Pipeline (En Progreso)
- [x] Estructura inicial CMake (`CMakeLists.txt`) y archivo nativo C++ (`native-lib.cpp`).
- [x] Módulo Rust (`Cargo.toml` y `lib.rs`) preparado para optimización de fotogramas.
- [x] Pipeline de Integración Continua con GitHub Actions (`build-apk.yml` con `workflow_dispatch`).
- [x] Firma de APK Debug con generación de llave en caliente (`keytool`) y caché de dependencias Gradle y Cargo.
- [x] Workflow de descompresión y sincronización automática desde archivos `.zip` subidos a `zips/` (`process-zip-sync.yml`).
- [ ] Enlace de llamadas FFI de Rust a través de JNI C++ para compresión de buffers en tiempo real.
- [ ] Reducción de tasa de fotogramas (*frame rate throttling*) para reducir el consumo de CPU/Batería cuando la pantalla esté estática.

---

## 🔵 Fase 3: Funcionalidades Avanzadas
- [ ] Recorte de segmentos de vídeo (elegir inicio y fin).
- [ ] Compatibilidad con múltiples fondos rotativos (Playlist de vídeos).
- [ ] Soporte para efectos de filtros simples en tiempo real (Brillo, Contraste, Desenfoque).
- [ ] Detección de nivel de batería bajo para pausar automáticamente la animación.
