cmake_minimum_required(VERSION 3.22.1)

project("videowallpaper_native")

# Configuración del motor nativo C++ y enlaces para Rust
add_library(
    videowallpaper_native
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    videowallpaper_native
    ${log-lib}
)
