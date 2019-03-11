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

JNIEXPORT void JNICALL
Java_com_lcm_ffmpeg_FFmpegPlayer_testMyMedia(JNIEnv *env, jobject instance, jstring url_) {

    const char *url = (*env)->GetStringUTFChars(env, url_, 0);
    LOGI("url:%s", url)
    av_register_all();
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                LOGI("[Video]:%s", c_temp->name);
                break;
            case AVMEDIA_TYPE_AUDIO:
                LOGI("[Audio]:%s", c_temp->name);
                break;
            default:
                LOGI("[Other]:%s", c_temp->name);
                break;
        }
        c_temp = c_temp->next;
    }

    (*env)->ReleaseStringUTFChars(env, url_, url);
}

/**
 * PCM 编码为AAC
 * @param env
 * @param instance
 * @param pcmPath_
 * @param outPath_
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_lcm_ffmpeg_FFmpegPlayer_encodePCMToAAC(JNIEnv *env, jobject instance, jstring pcmPath_,
                                                jstring outPath_) {
    const char *pcmPath = (*env)->GetStringUTFChars(env, pcmPath_, 0);
    const char *outPath = (*env)->GetStringUTFChars(env, outPath_, 0);

    AVFormatContext *pFormatCtx;
    AVOutputFormat *fmt;
    AVStream *audio_st;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;
    int ret = 0;
    uint8_t *frame_buf;
    AVFrame *frame;
    int size;

    FILE *in_file = fopen(pcmPath, "rb"); //音频PCM采样数据
    const char *out_file = outPath; //输出文件路径

    //PCM音频默认格式为AV_SAMPLE_FMT_S16  16bit/s
    enum AVSampleFormat inSampleFmt = AV_SAMPLE_FMT_S16;
    //FFmpge默认编码器支持的输入格式只能是AV_SAMPLE_FMT_FLTP  所以这里需要进行重采样转换格式
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_FLTP;
    const int sampleRate = 44100;
    const int channels = 2;
    const int sampleByte = 2;
    const int bitRate = 128000;
    int readSize;

    //初始化
    av_register_all();
    avformat_alloc_output_context2(&pFormatCtx, NULL, NULL, outPath);
    fmt = pFormatCtx->oformat;

    if (avio_open(&pFormatCtx->pb, outPath, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("输出文件打开失败!");
        return -1;
    }

    //打开编码器
    pCodec = avcodec_find_encoder(AV_CODEC_ID_AAC);
//    pCodec = avcodec_find_encoder_by_name("libfdk_aac");
    if (!pCodec) {
        LOGE("没有找到合适的编码器!");
        return -1;
    }

    //新建一个流
    audio_st = avformat_new_stream(pFormatCtx, pCodec);
    if (audio_st == NULL) {
        LOGE("avformat_new_stream error")
        return -1;
    }

    //设置编码器参数
    pCodecCtx = audio_st->codec;
    pCodecCtx->codec_id = fmt->audio_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    pCodecCtx->sample_fmt = outSampleFmt;
    pCodecCtx->sample_rate = sampleRate;
    pCodecCtx->channel_layout = AV_CH_LAYOUT_STEREO;
    pCodecCtx->channels = av_get_channel_layout_nb_channels(pCodecCtx->channel_layout);
    pCodecCtx->bit_rate = bitRate;

    //输出格式信息
    av_dump_format(pFormatCtx, 0, out_file, 1);

    //音频重采样 上下文初始化
    SwrContext *asc = NULL;
    asc = swr_alloc_set_opts(asc,
                             av_get_default_channel_layout(channels), outSampleFmt,
                             sampleRate,
                             av_get_default_channel_layout(channels), inSampleFmt,
                             sampleRate, 0, 0);

    if (!asc) {
        LOGE("swr_alloc_set_opts error")
        return -1;
    }

    ret = swr_init(asc);
    if (ret < 0) {
        LOGE("swr_init error")
        return -1;
    }



    //打开编码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("编码器打开失败");
        return -1;
    }


    //初始化AVFrame
    frame = av_frame_alloc();
    frame->nb_samples = pCodecCtx->frame_size;
    frame->format = pCodecCtx->sample_fmt;
    LOGI("sample_rate:%d,frame_size:%d,channels:%d", sampleRate, frame->nb_samples,
         frame->channels);

    //计算编码每一帧输入给编码器需要多少字节
    size = av_samples_get_buffer_size(NULL, pCodecCtx->channels, pCodecCtx->frame_size,
                                      pCodecCtx->sample_fmt, 1);

    frame_buf = av_malloc(size);
    //一次读取一帧音频的字节数
    readSize = frame->nb_samples * channels * sampleByte;
    char buf[readSize];

    avcodec_fill_audio_frame(frame, pCodecCtx->channels, pCodecCtx->sample_fmt, frame_buf, size, 1);


    audio_st->codecpar->codec_tag = 0;
    audio_st->time_base = audio_st->codec->time_base;

    //从编码器复制参数
    avcodec_parameters_from_context(audio_st->codecpar, pCodecCtx);
    //写文件头
    avformat_write_header(pFormatCtx, NULL);


    AVPacket pkt;
    av_new_packet(&pkt, size);

    int apts = 0;

    //编码
    for (int i = 0;; i++) {
        //读取原始数据
        if (fread(buf, 1, readSize, in_file) < 0) {
            LOGI("文件读取错误！")
            return -1;
        } else if (feof(in_file)) {
            break;
        }

        //设置pts
        frame->pts = apts;
        AVRational av;
        av.num = 1;
        av.den = sampleRate;

        apts += av_rescale_q(frame->nb_samples, av, pCodecCtx->time_base);
        int got_frame = 0;

        //重采样数据
        const uint8_t *indata[AV_NUM_DATA_POINTERS] = {0};
        indata[0] = buf;
        int len = swr_convert(asc, frame->data, frame->nb_samples, //输出参数，输出存储地址和样本数量
                              indata, frame->nb_samples);

        //编码
        ret = avcodec_send_frame(pCodecCtx, frame);
        if (ret < 0) {
            LOGE("avcodec_send_frame error")
        }

        ret = avcodec_receive_packet(pCodecCtx, &pkt);
        if (ret < 0) {
            LOGE("avcodec_receive_packet！error");
            continue;
        }

        pkt.stream_index = audio_st->index;
        LOGI("第%d帧", i);
        pkt.pts = av_rescale_q(pkt.pts, pCodecCtx->time_base, audio_st->time_base);
        pkt.dts = av_rescale_q(pkt.dts, pCodecCtx->time_base, audio_st->time_base);
        pkt.duration = av_rescale_q(pkt.duration, pCodecCtx->time_base, audio_st->time_base);

        ret = av_write_frame(pFormatCtx, &pkt);
        if (ret < 0) {
            LOGE("av_write_frame error!");
        }
        av_packet_unref(&pkt);
    }

    //写文件尾
    av_write_trailer(pFormatCtx);
    //清理
    avcodec_close(audio_st->codec);

    av_free(frame);
    av_free(frame_buf);
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    fclose(in_file);
    LOGI("finish!");

    (*env)->ReleaseStringUTFChars(env, pcmPath_, pcmPath);
    (*env)->ReleaseStringUTFChars(env, outPath_, outPath);
}



/**
 * 解码音频文件
 * @param env
 * @param instance
 * @param inPath_
 * @param outPath_
 * @return
 */
JNIEXPORT jint JNICALL
Java_com_lcm_ffmpeg_FFmpegPlayer_decodeToPCM(JNIEnv *env, jobject instance, jstring inPath_,
                                             jstring outPath_) {
    const char *inPath = (*env)->GetStringUTFChars(env, inPath_, 0);
    const char *outPath = (*env)->GetStringUTFChars(env, outPath_, 0);

    AVFormatContext *pFormatCtx = NULL;

    //注册所有组件
    av_register_all();
    //封装格式上下文
    pFormatCtx = avformat_alloc_context();

    //打开输入音频文件
    if (avformat_open_input(&pFormatCtx, inPath, NULL, NULL) != 0) {
        LOGE("打开输入音频文件失败！")
        return -1;
    }

    //获取音频信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("获取音频信息失败")
        return -1;
    }

    //音频解码时，需要找到对应的AVStream所在的pFormatCtx->streams的索引位置
    int audio_stream_idx = -1;
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_idx = i;
            break;
        }
    }

    //获取解码器
    //根据索引拿到对应的流 根据流拿到解码器上下文
    AVCodecContext *pCodecCtx = pFormatCtx->streams[audio_stream_idx]->codec;
    //再根据上下文拿到编解码id，通过id拿到解码器
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

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
    AVPacket *packet = av_malloc(sizeof(AVPacket));
    //解压缩数据
    AVFrame *frame = av_frame_alloc();

    //重采样
    //重采样设置选项-------------------------------------start
    SwrContext *swrCtx = swr_alloc();
    //输入的采样格式
    enum AVSampleFormat inSampleFmt = pCodecCtx->sample_fmt;
    //输出的采样格式 16bit PCM
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_S16;
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
    int outChannelNb = av_get_channel_layout_nb_channels(outChanelLayout);
    //存储PCM数据
    uint8_t *out_buffer = av_malloc(2 * 44100);
    //打开输出文件
    FILE *fp_pcm = fopen(outPath, "wb");

    int ret, got_frame, frameCount = 0;

    //一帧帧的读取压缩的音频数据
    while (av_read_frame(pFormatCtx, packet) >= 0) {
        if (packet->stream_index == audio_stream_idx) {
            //解码AVPacket-> AVFrame
            ret = avcodec_decode_audio4(pCodecCtx, frame, &got_frame, packet);
            if (ret < 0) {
                LOGI("解码完成")
            }

            if (got_frame) {
                LOGI("解码第%d帧", frameCount++);
                swr_convert(swrCtx, &out_buffer, 2 * 44100, frame->data, frame->nb_samples);
                //获取sample的大小
                int out_buffer_size = av_samples_get_buffer_size(NULL, outChannelNb,
                                                                 frame->nb_samples, outSampleFmt,
                                                                 1);
                //写入文件
                fwrite(out_buffer, 1, out_buffer_size, fp_pcm);
            }
        }
        av_free_packet(packet);
    }

    fclose(fp_pcm);
    av_frame_free(&frame);
    av_free(out_buffer);
    swr_free(&swrCtx);
    avcodec_close(pCodecCtx);
    avformat_close_input(&pFormatCtx);

    (*env)->ReleaseStringUTFChars(env, inPath_, inPath);
    (*env)->ReleaseStringUTFChars(env, outPath_, outPath);
    return 0;
}