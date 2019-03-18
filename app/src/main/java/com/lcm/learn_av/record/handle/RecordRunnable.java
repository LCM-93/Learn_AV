package com.lcm.learn_av.record.handle;


import android.util.Log;

import com.lcm.learn_av.record.handle.imp.RecordHandle;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 11:17 AM
 * Desc:
 * *****************************************************************
 */
public class RecordRunnable implements Runnable {
    private static final String TAG = "RecordRunnable";

    private RecordHandle mRecordHandle;


    public RecordRunnable(RecordHandle mRecordHandle) {
        this.mRecordHandle = mRecordHandle;
    }

    @Override
    public void run() {
        Log.w(TAG,"RecordRunnable current thread "+Thread.currentThread().getName());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (mRecordHandle.isRecording()) {
            mRecordHandle.readData();
        }
        Log.w(TAG,"RecordRunnable end!");
    }

}
