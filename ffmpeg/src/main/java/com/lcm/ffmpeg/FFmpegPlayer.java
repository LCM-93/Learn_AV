package com.lcm.ffmpeg;

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

    public native void playMyMedia(String url);
}
