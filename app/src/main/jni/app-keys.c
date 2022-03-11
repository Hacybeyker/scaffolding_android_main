#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_hacybeyker_main_di_module_NetworkModule_getApiKeyRelease(JNIEnv *env, jobject thiz) {
    //TODO change for your api_key
    return (*env)->NewStringUTF(env, "YOUR_API_KEY");
}

JNIEXPORT jstring JNICALL
Java_com_hacybeyker_main_di_module_NetworkModule_getApiKeyQA(JNIEnv *env, jobject thiz) {
    //TODO change for your api_key
    return (*env)->NewStringUTF(env, "YOUR_API_KEY");
}