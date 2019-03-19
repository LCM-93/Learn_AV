package com.lcm.learn_av.record.util;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/19 2:42 PM
 * Desc:
 * *****************************************************************
 */
public class SpeexUtils {

    private static final String TAG = "SpeexUtils";

    static {
        System.loadLibrary("cmffmpeg");
    }

    public native int open(int compression);
    public native int getFrameSize();
    public native int decode(byte encoded[], short lin[], int size);
    public native int encode(short lin[], int offset, byte encoded[], int size);
    public native void close();


    public native int cancelNoiseInit(int frame_size,int sample_rate);

    public native int cancelNoisePreprocess(byte[] data);

    public native int cancelNoiseDestroy();

    public native int initAudioAEC(int frame_size,int filter_length,int sample_rate);

    public native int audioAECProc(byte[] recordData,byte[] playData,byte[] outData);

    public native int exitSpeexDsp();

}
