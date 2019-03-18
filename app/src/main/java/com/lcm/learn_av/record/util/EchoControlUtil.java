package com.lcm.learn_av.record.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.lcm.learn_av.record.Config;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/16 5:21 PM
 * Desc: 回音消除
 * *****************************************************************
 */
public class EchoControlUtil {
    private static final String TAG = "EchoControlUtil";


    private static EchoControlUtil instance;

    public static EchoControlUtil getInstance() {
        if (instance == null) {
            synchronized (EchoControlUtil.class) {
                if (instance == null) {
                    instance = new EchoControlUtil();
                }
            }
        }
        return instance;
    }


    private boolean isAECEnable;
    private AcousticEchoCanceler canceler;

    private boolean isRemoveEcho;


    public boolean isAECEnable() {
        isAECEnable = AcousticEchoCanceler.isAvailable();
        return isAECEnable;
    }


    public void setRemoveEchoAEC(int audioSessionId, boolean isRemoveEcho) {
        this.isRemoveEcho = isRemoveEcho;
        setAEC(audioSessionId, isRemoveEcho);
    }

    public void setRemoveEchoAudioManager(Context context, boolean isRemoveEcho) {
        this.isRemoveEcho = isRemoveEcho;
        setAudioManager(context, isRemoveEcho);
    }


    public byte[] handleData(byte[] data) {
//        if (!isAECEnable && isRemoveEcho) {
//            return byteMerger(data);
//        }
        return data;
    }


    public void release() {
        if (canceler != null) {
            canceler.setEnabled(false);
            canceler.release();
        }
    }


    /**
     * 16bit 单声道转双声道数据
     *
     * @param byte_1
     * @return
     */
    private static byte[] byteMerger(byte[] byte_1) {
        byte[] byte_2 = new byte[byte_1.length * 2];
        for (int i = 0; i < byte_1.length; i++) {
            if (i % 2 == 0) {
                byte_2[2 * i] = byte_1[i];
                byte_2[2 * i + 1] = byte_1[i + 1];
            } else {
                byte_2[2 * i] = byte_1[i - 1];
                byte_2[2 * i + 1] = byte_1[i];
            }
        }
        return byte_2;
    }


    /**
     * 使用 AcousticEchoCanceler 消除回音
     *
     * @param isRemoveEcho
     */
    private void setAEC(int audioSessionId, boolean isRemoveEcho) {
        Log.i(TAG, "setAEC audioSessionId -> " + audioSessionId + "  " + isRemoveEcho);
        if (canceler == null) {
            canceler = AcousticEchoCanceler.create(audioSessionId);
        }
        if (canceler != null) {
            canceler.setEnabled(isRemoveEcho);
        }
    }

    /**
     * 配置 AudioManager消除回音
     *
     * @param context
     * @param isRemoveEcho
     */
    private void setAudioManager(Context context, boolean isRemoveEcho) {
        Log.i(TAG, "setAudioManager " + isRemoveEcho);
        if (isRemoveEcho) {
            Config.RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;//单通道
            Config.RECORD_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;//音频源
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        } else {
            Config.RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;//立体声
            Config.RECORD_SOURCE = MediaRecorder.AudioSource.MIC;//音频源
        }
    }

}
