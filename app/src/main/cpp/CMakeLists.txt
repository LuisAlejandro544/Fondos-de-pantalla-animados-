cmake_minimum_required(VERSION 3.22.1)

project("videowallpaper_native")

# Configuración del motor nativo C++ (NDK MediaCodec & ANativeWindow)
add_library(
    videowallpaper_native
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

find_library(
    android-lib
    android
)

find_library(
    mediandk-lib
    mediandk
)

target_link_libraries(
    videowallpaper_native
    ${log-lib}
    ${android-lib}
    ${mediandk-lib}
)
