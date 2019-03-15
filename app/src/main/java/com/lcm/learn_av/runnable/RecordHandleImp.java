package com.lcm.learn_av.runnable;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.lcm.learn_av.queue.RecordQueueManager;
import com.lcm.learn_av.runnable.imp.RecordHandle;


/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 2:04 PM
 * Desc:
 * *****************************************************************
 */
public class RecordHandleImp implements RecordHandle {

    private static final String TAG = "RecordHandleImp";

    private AudioRecord mAudioRecord;

    private int sampleRate = 44100;
    private int minBufferSize;

    private boolean isRecording;

    private boolean isSupportAcousticEcho = false;
    private int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;


    public RecordHandleImp() {
        initRecord();
    }

    @Override
    public void initRecord() {
        if (!createAudioRecord()) {
            Log.e(TAG, "createAudioRecord error!");
        }
    }

    @Override
    public void startRecord() {
        if (isRecording || mAudioRecord == null) return;
        mAudioRecord.startRecording();
        isRecording = true;
    }

    /**
     * 创建AudioRecord
     *
     * @return
     */

    private boolean createAudioRecord() {
        try {
            minBufferSize = AudioRecord.getMinBufferSize(sampleRate, DEFAULT_CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT);
            if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                return false;
            }
            mAudioRecord = new AudioRecord(DEFAULT_SOURCE, sampleRate, DEFAULT_CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            return mAudioRecord.getState() != AudioRecord.STATE_UNINITIALIZED;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置回声消除
     *
     * @param bool
     */
    @Override
    public void setAcousticEcho(Context context, boolean bool) {
        isSupportAcousticEcho = bool;
        if (isSupportAcousticEcho) {
            DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//单通道
            DEFAULT_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;//音频源
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        } else {
            DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;//立体声
            DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;//音频源
        }
    }


    @Override
    public void stopRecord() {
        isRecording = false;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
        mAudioRecord.stop();

    }

    @Override
    public void release() {
        startRecord();
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }


    @Override
    public boolean isRecording() {
        return mAudioRecord != null && isRecording;
    }

    @Override
    public void readData() {
        byte[] data = new byte[minBufferSize];
        int read = mAudioRecord.read(data, 0, minBufferSize);
        switch (read) {
            case AudioRecord.ERROR_INVALID_OPERATION:
                Log.w(TAG, "ERROR_INVALID_OPERATION");
                break;
            case AudioRecord.ERROR_BAD_VALUE:
                Log.w(TAG, "ERROR_BAD_VALUE");
                break;
            default:
//                Log.i(TAG, "recordData::" + data.length);
                RecordQueueManager.getInstance().put(data, 0, data.length);
                break;
        }
    }
}
