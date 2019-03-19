package com.lcm.learn_av.record.handle;

import android.util.Log;

import com.lcm.learn_av.record.handle.imp.EncodeHandle;


/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/18 3:54 PM
 * Desc:
 * *****************************************************************
 */
public class FFmpegEncodeHandleImp implements EncodeHandle {

    private static final String TAG = "FFmpegEncodeHandleImp";

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

    private boolean enableDecode;
    private int minSize;

    public native int initFFmpegEncode(String outPath);

    public native int fFmpegEncode(byte[] data);

    public native int fFmpegRelease();

    public FFmpegEncodeHandleImp(String outPath) {
        initEncode(outPath);
    }

    @Override
    public void initEncode(String outPath) {
        minSize = initFFmpegEncode(outPath);
        Log.i(TAG, "minSize " + minSize);
    }

    @Override
    public void encode(byte[] data) {
//        int length = data.length;
//        while (length > minSize){
//            byte[] temp = new byte[minSize];
//            System.arraycopy(data,0,temp,0,minSize);
//            Log.w(TAG, "编码 ====>" + temp.length);
//            fFmpegEncode(temp);
//
//            byte[] temp2 = new byte[length - minSize];
//            System.arraycopy(data,minSize-1,temp2,0,length-minSize);
//            data = temp2;
//            length = data.length;
//        }
//        Log.w(TAG, "编码 ====>" + data.length);
        fFmpegEncode(data);
    }

    @Override
    public boolean isEncodeEnable() {
        return enableDecode;
    }

    @Override
    public void disableEncode() {
        enableDecode = false;
    }

    @Override
    public void enableEncode() {
        enableDecode = true;
    }

    @Override
    public void release() {
        fFmpegRelease();
    }
}
