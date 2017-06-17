//
// Created by Andree Surya on 2017/06/04.
//

#include <jni.h>

#include <android/log.h>

static const char* kTAG = "dictionary_database";

#define LOGD(...) \
    ((void) __android_log_print(ANDROID_LOG_DEBUG, kTAG, __VA_ARGS__))

JNIEXPORT void JNICALL
Java_com_kotobyte_utils_DictionaryDatabase_nativeRebuildIndexes(JNIEnv *env, jobject instance) {
    LOGD("Something");
}