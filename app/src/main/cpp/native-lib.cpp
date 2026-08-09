#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "VideoWallpaperNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_native_VideoNativeBridge_getNativeEngineInfo(
        JNIEnv* env,
        jobject /* this */) {
    std::string info = "Motor nativo C++/Rust configurado para compresión de vídeo";
    return env->NewStringUTF(info.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_native_VideoNativeBridge_isHardwareAccelerationSupported(
        JNIEnv* env,
        jobject /* this */) {
    return JNI_TRUE;
}
