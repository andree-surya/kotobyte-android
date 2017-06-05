//
// Created by Andree Surya on 2017/06/04.
//

#include <jni.h>
#include <string.h>

#include <android/log.h>
#include <android/asset_manager_jni.h>

static const char* kTAG = "search_database";

#define LOGD(...) \
    ((void) __android_log_print(ANDROID_LOG_DEBUG, kTAG, __VA_ARGS__))

JNIEXPORT void JNICALL
Java_com_kotobyte_utils_SearchDatabase_nativeCreateDatabase(JNIEnv *env, jobject instance, jobject javaAssetManager) {

    AAssetManager *assetManager = AAssetManager_fromJava(env, javaAssetManager);
}