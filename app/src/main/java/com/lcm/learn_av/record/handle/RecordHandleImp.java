package com.lcm.learn_av.record.handle;

import android.content.Context;
import android.media.AudioRecord;
import android.util.Log;

import com.lcm.learn_av.queue.RecordQueueManager;
import com.lcm.learn_av.record.Config;
import com.lcm.learn_av.record.handle.imp.RecordHandle;
import com.lcm.learn_av.record.util.EchoControlUtil;


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

    private int sampleRate = Config.SAMPLE_RATE_IN_HZ;
    private int encodingBit = Config.AUDIOFORAM_BIT;
    private int minBufferSize;

    private boolean isRecording;
    private int audioSessionId = -1;


    @Override
    public void initRecord(Context context, boolean isRemoveEcho) {
        if (!EchoControlUtil.getInstance().isAECEnable()) {
            EchoControlUtil.getInstance().setRemoveEchoAudioManager(context, isRemoveEcho);
        }
        if (!createAudioRecord()) {
            Log.e(TAG, "createAudioRecord error");
        }
        if (audioSessionId != -1 && EchoControlUtil.getInstance().isAECEnable()) {
            EchoControlUtil.getInstance().setRemoveEchoAEC(audioSessionId, isRemoveEcho);
        }
    }

    @Override
    public int getAudioSessionId() {
        return EchoControlUtil.getInstance().isAECEnable() ? audioSessionId : -1;
    }

    @Override
    public void startRecord() {
        if (isRecording || mAudioRecord == null) return;
        mAudioRecord.startRecording();
        isRecording = true;
    }

    @Override
    public void setAcousticEcho(Context context, boolean bool) {

    }

    /**
     * 创建AudioRecord
     *
     * @return
     */

    private boolean createAudioRecord() {
        try {
            Log.i(TAG, "RECORD_CHANNEL_CONFIG:" + Config.RECORD_CHANNEL_CONFIG + "  RECORD_SOURCE" + Config.RECORD_SOURCE);
            minBufferSize = AudioRecord.getMinBufferSize(sampleRate, Config.RECORD_CHANNEL_CONFIG, encodingBit) ;
            if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                return false;
            }
            mAudioRecord = new AudioRecord(Config.RECORD_SOURCE, sampleRate, Config.RECORD_CHANNEL_CONFIG, encodingBit, minBufferSize);
            audioSessionId = mAudioRecord.getAudioSessionId();
            return mAudioRecord.getState() != AudioRecord.STATE_UNINITIALIZED;
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public void stopRecord() {
        isRecording = false;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
    }

    @Override
    public void release() {
        stopRecord();
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        EchoControlUtil.getInstance().release();
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
                byte[] bytes = EchoControlUtil.getInstance().handleData(data);
                RecordQueueManager.getInstance().put(bytes, 0, bytes.length);
                break;
        }
    }


}
