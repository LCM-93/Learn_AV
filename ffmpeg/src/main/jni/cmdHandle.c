#include <jni.h>
#include <string.h>
#include <stdint.h>

#include "cmdutils.h"
#include "ffmpeg.h"

//打印日志
#include <android/log.h>

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"JNIPlayer",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"JNIPlayer",FORMAT,##__VA_ARGS__);


JNIEXPORT jint JNICALL
Java_com_lcm_ffmpeg_audio_CmdHandle_executeCmd(JNIEnv *env, jobject instance, jobjectArray cmds) {

    int argc = (*env)->GetArrayLength(env,cmds);
    char *argv[argc];
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env,cmds, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env,js, 0);
    }
    return ffmpeg_exec(argc, argv);
}