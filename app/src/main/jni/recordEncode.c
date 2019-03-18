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


/**
 * =============================================================
 *
 *          Android设备实时采集，FFmpeg编码输出到文件
 *
 * =============================================================
 */



AVFormatContext *pFormatCtxEncode;
AVOutputFormat *fmt;
AVStream *audio_st;
AVCodecContext *pCodecEncodeCtxEncode;
AVCodec *pCodecEncode;
SwrContext *pSwrCtxEncode;
AVFrame *audio_frame;
AVPacket audio_packet;
uint8_t *audio_frame_buf;
int audio_buf_size = 0;
int audio_frame_count = 0;
int apts = 0;
int readSize = -1;

/**
 * 初始化音频录制  打开ffmpeg
 * @param env
 * @param instance
 * @param outPath_
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_handle_FFmpegEncodeHandleImp_initFFmpegEncode(JNIEnv *env, jobject instance, jstring outPath_) {
    const char *outPath = (*env)->GetStringUTFChars(env, outPath_, 0);
    LOGI("音频文件%s", outPath)

    enum AVSampleFormat inSampleFmt = AV_SAMPLE_FMT_S16;
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_FLTP;
    const int sampleRate = 44100;
    const int channels = 1;
    const int sampleByte = 2;
    const int bitRate = 128000;

    av_register_all();

    if (avformat_alloc_output_context2(&pFormatCtxEncode, NULL, NULL, outPath) < 0) {
        LOGE("avformat_alloc_output_context2  error!");
        return -1;
    }

    fmt = pFormatCtxEncode->oformat;

    if (avio_open(&pFormatCtxEncode->pb, outPath, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("打开文件失败！");
        return -1;
    }

    //打开编码器
    pCodecEncode = avcodec_find_encoder(AV_CODEC_ID_AAC);
    if (!pCodecEncode) {
        LOGE("没有找到合适的编码器！")
        return -1;
    }

    audio_st = avformat_new_stream(pFormatCtxEncode, pCodecEncode);
    if (audio_st == NULL) {
        LOGE("avformat_new_stream  error")
        return -1;
    }

    //设置编码器参数
    pCodecEncodeCtxEncode = audio_st->codec;
    pCodecEncodeCtxEncode->codec_id = fmt->audio_codec;
    pCodecEncodeCtxEncode->codec_type = AVMEDIA_TYPE_AUDIO;
    pCodecEncodeCtxEncode->sample_fmt = outSampleFmt;
    pCodecEncodeCtxEncode->sample_rate = sampleRate;
    pCodecEncodeCtxEncode->channel_layout = AV_CH_LAYOUT_MONO;
    pCodecEncodeCtxEncode->channels = av_get_channel_layout_nb_channels(pCodecEncodeCtxEncode->channel_layout);
    pCodecEncodeCtxEncode->bit_rate = bitRate;


    //音频重采样  上下文初始化
    pSwrCtxEncode = swr_alloc_set_opts(pSwrCtxEncode,
                                 av_get_default_channel_layout(channels), outSampleFmt,
                                 sampleRate,
                                 av_get_default_channel_layout(channels), inSampleFmt,
                                 sampleRate, 0, 0);

    if (!pSwrCtxEncode) {
        LOGE("swr_alloc_set_opts error")
        return -1;
    }

    if (swr_init(pSwrCtxEncode) < 0) {
        LOGE("swr_init error")
        return -1;
    }


    if (avcodec_open2(pCodecEncodeCtxEncode, pCodecEncode, NULL) < 0) {
        LOGE("编码器打开失败！")
        return -1;
    }

    //初始化AVFrame
    audio_frame = av_frame_alloc();
    audio_frame->nb_samples = pCodecEncodeCtxEncode->frame_size;
    audio_frame->format = pCodecEncodeCtxEncode->sample_fmt;
    LOGI("sample_rate:%d,frame_size:%d,channels:%d", sampleRate, audio_frame->nb_samples,
         audio_frame->channels);


    //获取AVFrame数据缓冲区大小
    audio_buf_size = av_samples_get_buffer_size(NULL, pCodecEncodeCtxEncode->channels, pCodecEncodeCtxEncode->frame_size,
                                                pCodecEncodeCtxEncode->sample_fmt, 1);

    //创建缓冲区
    audio_frame_buf = av_malloc((size_t) audio_buf_size);
    avcodec_fill_audio_frame(audio_frame, pCodecEncodeCtxEncode->channels, pCodecEncodeCtxEncode->sample_fmt,
                             audio_frame_buf, audio_buf_size, 1);

    //设置编码器上下文参数
    audio_st->codecpar->codec_tag = 0;
    audio_st->time_base = audio_st->codec->time_base;

    //从编码器复制参数
    avcodec_parameters_from_context(audio_st->codecpar, pCodecEncodeCtxEncode);
    //写文件头
    avformat_write_header(pFormatCtxEncode, NULL);


    av_new_packet(&audio_packet, NULL);


    (*env)->ReleaseStringUTFChars(env, outPath_, outPath);

    readSize = audio_frame->nb_samples * channels * sampleByte;
    return readSize;
}


int countEncode = 0;

/**
 * 编码每一组音频数据
 * @param env
 * @param instance
 * @param buffer_
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_handle_FFmpegEncodeHandleImp_fFmpegEncode(JNIEnv *env, jobject instance,
                                                   jbyteArray buffer_) {
    int ret = 0;
    jbyte *buffer = (*env)->GetByteArrayElements(env, buffer_, NULL);
    jsize theArrayLengthJ = (*env)->GetArrayLength(env, buffer_);
    LOGI("开始编码：%d", theArrayLengthJ);

    countEncode++;

    //设置pts
    audio_frame->pts = apts;
    AVRational av;
    av.num = 1;
    av.den = audio_frame->nb_samples;

    apts += av_rescale_q(audio_frame->nb_samples, av, pCodecEncodeCtxEncode->time_base);

    //重采样数据
    uint8_t *indata[AV_NUM_DATA_POINTERS] = {0};
    indata[0] = (uint8_t *) buffer;
    int len = swr_convert(pSwrCtxEncode, audio_frame->data, audio_frame->nb_samples, indata,
                          audio_frame->nb_samples);
    if (len < 0) {
        LOGE("swr_convert error")
        return -1;
    }

    //编码
    ret = avcodec_send_frame(pCodecEncodeCtxEncode, audio_frame);
    if (ret < 0) {
        LOGE("avcodec_send_frame error!")
        return -1;
    }

    //获取编码后的数据
    ret = avcodec_receive_packet(pCodecEncodeCtxEncode, &audio_packet);
    if (ret < 0) {
        LOGE("avcodec_receive_packet error!")
        return -1;
    }

    audio_packet.pts = av_rescale_q(audio_packet.pts, pCodecEncodeCtxEncode->time_base, audio_st->time_base);
    audio_packet.dts = av_rescale_q(audio_packet.dts, pCodecEncodeCtxEncode->time_base, audio_st->time_base);
    audio_packet.duration = av_rescale_q(audio_packet.duration, pCodecEncodeCtxEncode->time_base,
                                         audio_st->time_base);

    //写入数据
    ret = av_write_frame(pFormatCtxEncode, &audio_packet);
    if (ret < 0) {
        LOGE("av_write_frame error!")
    }

    //清空packet数据
    av_packet_unref(&audio_packet);

    LOGI("encode success %d", countEncode);

    (*env)->ReleaseByteArrayElements(env, buffer_, buffer, 0);
    return ret;
}





/**
 * 关闭
 * @param env
 * @param instance
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_lcm_learn_1av_record_handle_FFmpegEncodeHandleImp_fFmpegRelease(JNIEnv *env, jobject instance) {
    LOGI("编码结束")
    if (audio_st) {
        avcodec_close(audio_st->codec);
    }
    if (pFormatCtxEncode) {
        av_write_trailer(pFormatCtxEncode);
        avio_close(pFormatCtxEncode->pb);
        avformat_free_context(pFormatCtxEncode);
        pFormatCtxEncode = NULL;
    }
    av_free(audio_frame);
    av_free(audio_frame_buf);
    return 0;
}

