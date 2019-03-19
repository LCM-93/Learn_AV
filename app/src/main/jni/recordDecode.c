#include <jni.h>
#include <string.h>
#include <stdint.h>

//ffmpeg库
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"


//打印日志
#include <android/log.h>

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"JNIPlayer",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"JNIPlayer",FORMAT,##__VA_ARGS__);

#include <jni.h>


AVFormatContext *pFormatCtx = NULL;
int audio_stream_idx = -1;

AVCodecContext *pCodecCtx = NULL;
AVCodec *pCodec = NULL;

AVPacket *packet = NULL;
AVFrame *frame = NULL;
SwrContext *swrCtx = NULL;
uint8_t *out_buffer;
int outChannelNb;

enum AVSampleFormat inSampleFmt;
//输出的采样格式 16bit PCM
enum AVSampleFormat outSampleFmt;

JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_handle_FFmpegDecodeHandleImp_initFFmpegDecode(JNIEnv *env,
                                                                            jobject instance,
                                                                            jstring bgMusicSrc_) {
    const char *bgMusicSrc = (*env)->GetStringUTFChars(env, bgMusicSrc_, 0);

    av_register_all();

    pFormatCtx = avformat_alloc_context();

    if (avformat_open_input(&pFormatCtx, bgMusicSrc, NULL, NULL) != 0) {
        LOGE("打开输入音频文件失败！")
        return -1;
    }

    //获取音频信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("获取音频信息失败")
        return -1;
    }

    //音频解码时，需要找到对应的AVStream所在的pFormatCtx->streams的索引位置
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_idx = i;
            break;
        }
    }

    //获取解码器
    //根据索引拿到对应的流 根据流拿到解码器上下文
    pCodecCtx = pFormatCtx->streams[audio_stream_idx]->codec;
    //再根据上下文拿到编解码id，通过id拿到解码器
    pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

    if (pCodec == NULL) {
        LOGE("获取解码器失败！无法解码！")
        return -1;
    }

    //打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("解码器无法打开")
        return -1;
    }

    //编码数据
    packet = av_malloc(sizeof(AVPacket));
    //解压缩数据
    frame = av_frame_alloc();


    //重采样
    //重采样设置选项-------------------------------------start
    swrCtx = swr_alloc();
    //输入的采样格式
    inSampleFmt = pCodecCtx->sample_fmt;
    //输出的采样格式 16bit PCM
    outSampleFmt = AV_SAMPLE_FMT_S16;
    //输入的采样率
    int inSampleRate = pCodecCtx->sample_rate;
    //输出的采样率
    int outSampleRate = 44100;
    //输入的声道布局
    uint64_t inChannelLayout = pCodecCtx->channel_layout;
    //输出的声道布局
    uint64_t outChanelLayout = AV_CH_LAYOUT_STEREO;

    swr_alloc_set_opts(swrCtx, outChanelLayout, outSampleFmt, outSampleRate,
                       inChannelLayout, inSampleFmt, inSampleRate, 0, NULL);
    swr_init(swrCtx);
    //重采样设置选项-------------------------------------end


    //获取输出的声道个数
    outChannelNb = av_get_channel_layout_nb_channels(outChanelLayout);
    //存储PCM数据
    out_buffer = av_malloc(2 * 44100);


    // TODO

    (*env)->ReleaseStringUTFChars(env, bgMusicSrc_, bgMusicSrc);

    return 1;
}


JNIEXPORT jbyteArray JNICALL
Java_com_lcm_learn_1av_record_handle_FFmpegDecodeHandleImp_fFmpegDecode(JNIEnv *env,
                                                                        jobject instance) {
    int ret, got_frame = 0;

    if (av_read_frame(pFormatCtx, packet) < 0) {
        LOGE("解码失败")
        return NULL;
    }
    if (packet->stream_index != audio_stream_idx) {
        LOGE("音频轨道不符");
        av_free_packet(packet);
        return NULL;
    }
    ret = avcodec_decode_audio4(pCodecCtx, frame, &got_frame, packet);
    if (ret < 0) {
        LOGI("解码完成！")
    }
    int out_buffer_size = 0;
    if (got_frame) {
        swr_convert(swrCtx, &out_buffer, 2 * 44100, frame->data, frame->nb_samples);
        out_buffer_size = av_samples_get_buffer_size(NULL, outChannelNb,
                                                     frame->nb_samples, outSampleFmt,
                                                     1);
        LOGE("decode size: %d", out_buffer_size);
    }

    jbyteArray outData = (*env)->NewByteArray(env, out_buffer_size);
    (*env)->SetByteArrayRegion(env, outData, 0, out_buffer_size, out_buffer);
    av_free_packet(packet);
    return outData;
}


JNIEXPORT void JNICALL
Java_com_lcm_learn_1av_record_handle_FFmpegDecodeHandleImp_fFmpegRelease(JNIEnv *env,
                                                                         jobject instance) {
    if(frame){
        av_frame_free(&frame);
    }
    if(swrCtx){
        swr_free(&swrCtx);
    }

    if(pCodecCtx){
        avcodec_close(pCodecCtx);
    }
    if(pFormatCtx){
        avformat_close_input(&pFormatCtx);
    }
}

