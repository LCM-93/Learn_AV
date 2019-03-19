//
// Created by LiChenMing.Chaman on 2019/3/19.
//

#include <jni.h>
#include <string.h>
#include <stdint.h>

#include "speex/speex_preprocess.h"
#include "speex/speex_echo.h"

//打印日志
#include <android/log.h>

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"SPEEX_DSP",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"SPEEX_DSP",FORMAT,##__VA_ARGS__);


SpeexPreprocessState *st;
int nInitSuccessFlag = 0;
SpeexEchoState* m_pState;
SpeexPreprocessState* m_pPreprocessorState;
int      m_nFrameSize;
int      m_nFilterLen;
int      m_nSampleRate;
int 	 iArg;

/*************************** 噪声处理 ****************************/

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_util_SpeexUtils_cancelNoiseInit(JNIEnv *env, jobject instance,
                                                              jint frame_size, jint sample_rate) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_util_SpeexUtils_cancelNoisePreprocess(JNIEnv *env, jobject instance,
                                                                    jbyteArray data_) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    // TODO

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
}

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_util_SpeexUtils_cancelNoiseDestroy(JNIEnv *env, jobject instance) {

    // TODO

}




/*************************** 回声处理 ****************************/

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_util_SpeexUtils_initAudioAEC(JNIEnv *env, jobject instance,
                                                           jint frame_size, jint filter_length,
                                                           jint sample_rate) {

    // TODO

}

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_util_SpeexUtils_audioAECProc(JNIEnv *env, jobject instance,
                                                           jbyteArray recordData_,
                                                           jbyteArray playData_,
                                                           jbyteArray outData_) {
    jbyte *recordData = (*env)->GetByteArrayElements(env, recordData_, NULL);
    jbyte *playData = (*env)->GetByteArrayElements(env, playData_, NULL);
    jbyte *outData = (*env)->GetByteArrayElements(env, outData_, NULL);

    // TODO

    (*env)->ReleaseByteArrayElements(env, recordData_, recordData, 0);
    (*env)->ReleaseByteArrayElements(env, playData_, playData, 0);
    (*env)->ReleaseByteArrayElements(env, outData_, outData, 0);
}

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_util_SpeexUtils_exitSpeexDsp(JNIEnv *env, jobject instance) {

    // TODO

}