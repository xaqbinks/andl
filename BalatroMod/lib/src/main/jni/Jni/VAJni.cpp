#include <jni.h>
#include <Foundation/IOUniformer.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

static jstring ToJstring(JNIEnv *env, const char *pstr) {
    if (pstr == NULL) {
        return NULL;
    }
    return env->NewStringUTF(pstr);
}

static char *ToCstr(JNIEnv *env, jstring jstr) {
    if (jstr == NULL) {
        return NULL;
    }
    char *cstr = NULL;
    jclass class_string = env->FindClass("java/lang/String");
    jstring str_encode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(class_string, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, str_encode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        cstr = (char *) malloc(alen + 1);
        memcpy(cstr, ba, alen);
        cstr[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return cstr;
}


void Java_com_lody_virtual_client_core_VirtualCore_addRedirect(JNIEnv *env, jobject, jstring origPath, jstring newPath) {
    const char *orig_path = ToCstr(env, origPath);
    const char *new_path = ToCstr(env, newPath);
    IOUniformer::redirect(orig_path, new_path);
    free((void *) orig_path);
    free((void *) new_path);
}

void Java_com_lody_virtual_client_core_VirtualCore_removeRedirect(JNIEnv *env, jobject, jstring origPath) {
    // The original VirtualApp does not seem to have a removeRedirect function.
    // For this implementation, we will simply forbid the path, which will prevent it from being accessed.
    // A more complete implementation would require modifying the native lists.
    const char *orig_path = ToCstr(env, origPath);
    IOUniformer::forbid(orig_path);
    free((void *) orig_path);
}


void Java_com_lody_virtual_client_core_VirtualCore_forbid(JNIEnv *env, jobject, jstring path) {
    const char *c_path = ToCstr(env, path);
    IOUniformer::forbid(c_path);
    free((void*)c_path);
}


static const JNINativeMethod gMethods[] = {
        {"addRedirect", "(Ljava/lang/String;Ljava/lang/String;)V", (void *) Java_com_lody_virtual_client_core_VirtualCore_addRedirect},
        {"removeRedirect", "(Ljava/lang/String;)V", (void *) Java_com_lody_virtual_client_core_VirtualCore_removeRedirect},
        {"forbid", "(Ljava/lang/String;)V", (void *) Java_com_lody_virtual_client_core_VirtualCore_forbid},
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    jclass clazz = env->FindClass("com/lody/virtual/client/core/VirtualCore");
    if (clazz) {
        env->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
        env->DeleteLocalRef(clazz);
    }
    return JNI_VERSION_1_6;
}


#ifdef __cplusplus
}
#endif
