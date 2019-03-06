package com.lcm.mediacodec.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/6 3:34 PM
 * Desc: 音频录制
 * *****************************************************************
 */
public class RecordCaptor {
    private static final String TAG = "RecordCaptor";

    private AudioRecord mAudioRecord;

    private int sampleRate = 44100;
    private int channelCount = AudioFormat.CHANNEL_IN_STEREO; //双声道   测试手机上录音双声道效果不好  单声道好点
    private int minBufferSize;

    private Runnable mRecordRunnable = new RecordRunnable();
    private Thread mRecordThread;

    private boolean isRecording;


    private RecordListener mRecordListener;

    public void setRecordListener(RecordListener recordListener) {
        this.mRecordListener = recordListener;
    }


    public void startRecord() {
        if (isRecording || !createAudioRecord() || mAudioRecord == null) return;
        mAudioRecord.startRecording();
        isRecording = true;
        mRecordThread = new Thread(mRecordRunnable);
        mRecordThread.start();
    }


    /**
     * 创建AudioRecord
     *
     * @return
     */
    private boolean createAudioRecord() {
        try {
            minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelCount, AudioFormat.ENCODING_PCM_16BIT);
            if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                return false;
            }
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelCount, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            return mAudioRecord.getState() != AudioRecord.STATE_UNINITIALIZED;
        } catch (Exception e) {
            return false;
        }
    }


    public void stopRecord() {
        isRecording = false;
        try {
            if (mRecordThread != null) {
                mRecordThread.interrupt();
                mRecordThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
    }


    private class RecordRunnable implements Runnable {
        @Override
        public void run() {
            while (isRecording && mAudioRecord != null) {
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
                        Log.e(TAG, "recordData::" + data.length);
                        if (mRecordListener != null) mRecordListener.flushData(data);
                        break;
                }
            }
        }
    }

    public interface RecordListener {
        void flushData(byte[] data);
    }

}
