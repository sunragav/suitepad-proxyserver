//
// Created by Sundararaghavan on 04-01-2020.
//

#include <string>
#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_sunragav_suitepad_nativelib_KeyStoreHelper_getPass(JNIEnv *env, jobject) {
    std::string pass = "suitepad";
    return env->NewStringUTF(pass.c_str());
}

