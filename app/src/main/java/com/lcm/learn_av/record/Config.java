package com.lcm.learn_av.record;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/16 4:45 PM
 * Desc:
 * *****************************************************************
 */
public class Config {

    //采样率
    public static int SAMPLE_RATE_IN_HZ = 44100;

    //比特率
    public static int BITE_RATE = 128000;

    //声道数
    public static int CHANNEL_COUNT = 2;

    //双声道
    public static int AUDIOFORMAT_IN_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;

    public static int AUDIOFORMAT_OUT_CHANNEL = AudioFormat.CHANNEL_OUT_STEREO;

    //
    public static int AUDIOFORAM_BIT = AudioFormat.ENCODING_PCM_16BIT;


    public static int RECORD_SOURCE = MediaRecorder.AudioSource.MIC;

    public static int RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
}
