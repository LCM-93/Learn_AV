package com.lcm.learn_av.record;

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
        if (isRecording || mAudioRecord == null) return;
        mAudioRecord.startRecording();
        isRecording = true;
        mRecordThread.start();
    }

    public void init() {
        createAudioRecord();
        mRecordThread = new Thread(mRecordRunnable);
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
        mRecordThread.interrupt();
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
    }


    private class RecordRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
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
                        Log.i(TAG, "recordData::" + data.length);
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
