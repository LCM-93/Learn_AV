package com.lcm.learn_av.record.handle;

import android.util.Log;

import com.lcm.learn_av.queue.AudioData;
import com.lcm.learn_av.queue.DecodeQueueManager;
import com.lcm.learn_av.queue.RecordData;
import com.lcm.learn_av.queue.RecordQueueManager;
import com.lcm.learn_av.record.handle.imp.EncodeHandle;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 2:23 PM
 * Desc:
 * *****************************************************************
 */
public class EncodeRunnable implements Runnable {
    private static final String TAG = "EncodeRunnable";

    private EncodeHandle mEncodeHandle;

    public EncodeRunnable(EncodeHandle mEncodeHandle) {
        this.mEncodeHandle = mEncodeHandle;
    }

    @Override
    public void run() {
        Log.w(TAG, "EncodeRunnable current thread " + Thread.currentThread().getName());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while (mEncodeHandle.isEncodeEnable()) {
            RecordData recordData = RecordQueueManager.getInstance().getRecordData();
            Log.e(TAG, ">>>>>>> 读取一个解码数据");
            AudioData audioData = DecodeQueueManager.getInstance().getAudioData();
            Log.e(TAG, ">>>>>>> 读取一段录音数据");
            if (audioData == null && recordData == null) {
                continue;
            }

            if (recordData != null && audioData != null) {
                byte[] bytes = mixAudio(recordData.getData(), audioData.getData(), 0.3f);
                Log.w(TAG, "<=====  合成一段音频 " + bytes.length + "  =====>");
                mEncodeHandle.encode(bytes);
            }
        }
        mEncodeHandle.release();
        Log.w(TAG, "EncodeRunnable end!");
    }


    private byte[] mixAudio(byte[] audioData, byte[] bytes, float volume) {
        short[] cret = new short[audioData.length / 2];
        for (int i = 0; i < audioData.length / 2; i++) {
            short c = (short) ((audioData[i * 2] & 0xFF) | ((audioData[i * 2 + 1] & 0xFF) << 8));
            short c1 = (short) ((bytes[i * 2] & 0xFF) | ((bytes[i * 2 + 1] & 0xFF) << 8));
            cret[i] = (short) ((c + c1 * 0.5 * volume) / 2);
        }

        byte[] ret = new byte[audioData.length];
        for (int i = 0; i < cret.length; i++) {
            ret[i * 2] = (byte) ((cret[i] & 0x00FF));
            ret[i * 2 + 1] = (byte) ((cret[i] & 0xFF00) >> 8);
        }
        return ret;
    }


}
