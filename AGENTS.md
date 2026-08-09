# 🤖 Reglas y Guía para Agentes de IA - Video Fondo

Este archivo contiene reglas persistentes para cualquier agente de IA que trabaje en esta base de código.

## 📋 Reglas de Proyecto

1. **Estilo de Diseño**:
   - Mantener la paleta de colores limpia y confortable (Slate Navy & Indigo). No incluir colores neón o verdes chillones.
   - Usar Jetpack Compose con Material Design 3.

2. **Garantía del Comportamiento del Slider de Sonido**:
   - Cuando la app esté en modo silenciado (*muted*), el slider debe marcar 0%.
   - Al quitar el silencio (*unmuted*), debe recordar el porcentaje anterior y restaurarlo.
   - Mover el slider actualiza el estado de mute de forma coherente.

3. **Arquitectura y Archivos**:
   - No eliminar la carpeta `/assets/logos/` con los archivos `.svg` ni el `/assets/app_config.json`.
   - Mantener intacta la configuración de CMake en `app/build.gradle.kts` para la interoperabilidad con C++ y Rust en `app/src/main/cpp/`.

4. **Compilación y Verificación**:
   - Correr siempre `compile_applet` tras realizar modificaciones de código para confirmar que no haya errores de compilación en Kotlin ni Gradle.

5. **Workflows y CI/CD**:
   - Mantener el archivo `.github/workflows/build-apk.yml` alineado para descargas completas de código Kotlin/C++/Rust con caché y generación de clave en caliente.
   - Conservar el archivo `.github/workflows/process-zip-sync.yml` y la estructura de la carpeta `zips/` para permitir actualizaciones automatizadas por lotes desde paquetes comprimidos.
