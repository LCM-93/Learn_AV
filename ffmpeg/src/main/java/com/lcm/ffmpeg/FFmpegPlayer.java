package com.lcm.ffmpeg;

import android.util.Log;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/7 3:10 PM
 * Desc:
 * *****************************************************************
 */
public class FFmpegPlayer {

    static {
        System.loadLibrary("avcodec");
        System.loadLibrary("avdevice");
        System.loadLibrary("avfilter");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("swscale");
        System.loadLibrary("cmffmpeg");
    }

    public native void testMyMedia(String url);

    //pcm编码为AAC
    public native int encodePCMToAAC(String pcmPath,String outPath);

    //解码音频文件
    public native int decodeToPCM(String inPath,String outPath);


}
