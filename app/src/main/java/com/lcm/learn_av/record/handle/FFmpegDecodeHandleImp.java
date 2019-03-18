package com.lcm.learn_av.record.handle;

import android.util.Log;

import com.lcm.learn_av.queue.DecodeQueueManager;
import com.lcm.learn_av.record.handle.imp.DecodeHandle;
import com.lcm.learn_av.record.player.AudioTrackManager;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/18 2:39 PM
 * Desc:
 * *****************************************************************
 */
public class FFmpegDecodeHandleImp implements DecodeHandle {
    private static final String TAG = "FFmpegDecodeHandleImp";

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



    public native int initFFmpegDecode(String bgMusicSrc);

    public native byte[] fFmpegDecode();

    public native void fFmpegRelease();

    private boolean enableDecode;


    public FFmpegDecodeHandleImp(int audioSessionId, String bgMusicSrc) {
        AudioTrackManager.getInstance().init(audioSessionId);
        initDecode(bgMusicSrc);
    }


    @Override
    public void initDecode(String bgMusicSrc) {
        initFFmpegDecode(bgMusicSrc);
    }

    @Override
    public void decode() {
        byte[] data = fFmpegDecode();
        Log.i(TAG,"decode data size "+data.length);
        DecodeQueueManager.getInstance().put(data, 0, data.length);
        AudioTrackManager.getInstance().play(data, 0, data.length);
    }

    @Override
    public void disableDecode() {
        enableDecode = false;
    }

    @Override
    public void enableDecode() {
        enableDecode = true;
    }

    @Override
    public boolean isEnableDecode() {
        return enableDecode;
    }

    @Override
    public void release() {
        fFmpegRelease();
    }
}
