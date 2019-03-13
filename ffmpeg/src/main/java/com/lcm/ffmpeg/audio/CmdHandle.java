package com.lcm.ffmpeg.audio;


/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/13 10:52 PM
 * Desc:
 * *****************************************************************
 */
public enum CmdHandle {
    instance;

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


    /**
     * 执行cmd命令
     * @param cmds
     * @return
     */
    public native int executeCmd(String[] cmds);

}
