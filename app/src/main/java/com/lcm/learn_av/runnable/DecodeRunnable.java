package com.lcm.learn_av.runnable;

import android.util.Log;

import com.lcm.learn_av.runnable.imp.DecodeHandle;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 2:17 PM
 * Desc:
 * *****************************************************************
 */

public class DecodeRunnable implements Runnable {
    private static final String TAG = "DecodeRunnable";
    private DecodeHandle mDecodeHandle;

    public DecodeRunnable(DecodeHandle mDecodeHandle) {
        this.mDecodeHandle = mDecodeHandle;
    }

    @Override
    public void run() {
        Log.w(TAG,"DecodeRunnable current thread "+Thread.currentThread().getName());
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (mDecodeHandle.isEnableDecode()){
            mDecodeHandle.decode();
        }

        Log.w(TAG,"DecodeRunnable end!");
    }
}
