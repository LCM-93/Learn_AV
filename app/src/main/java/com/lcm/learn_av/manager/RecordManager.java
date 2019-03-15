package com.lcm.learn_av.manager;

import android.content.Context;

import com.lcm.learn_av.queue.DecodeQueueManager;
import com.lcm.learn_av.queue.RecordQueueManager;
import com.lcm.learn_av.runnable.DecodeHandleImp;
import com.lcm.learn_av.runnable.DecodeRunnable;
import com.lcm.learn_av.runnable.EncodeHandleImp;
import com.lcm.learn_av.runnable.EncodeRunnable;
import com.lcm.learn_av.runnable.RecordHandleImp;
import com.lcm.learn_av.runnable.RecordRunnable;
import com.lcm.learn_av.runnable.imp.DecodeHandle;
import com.lcm.learn_av.runnable.imp.EncodeHandle;
import com.lcm.learn_av.runnable.imp.RecordHandle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 2:44 PM
 * Desc:
 * *****************************************************************
 */
public class RecordManager {

    private DecodeHandle mDecodeHandle;
    private DecodeRunnable mDecodeRunnable;

    private EncodeHandle mEncodeHandle;
    private EncodeRunnable mEncodeRunnable;

    private RecordHandle mRecordHandle;
    private RecordRunnable mRecordRunnable;

    private ExecutorService mExecutorService;

    


    public void init(String bgPath, String outPath) {
        mDecodeHandle = new DecodeHandleImp(bgPath);
        mEncodeHandle = new EncodeHandleImp(outPath);
        mRecordHandle = new RecordHandleImp();
        mDecodeRunnable = new DecodeRunnable(mDecodeHandle);
        mEncodeRunnable = new EncodeRunnable(mEncodeHandle);
        mRecordRunnable = new RecordRunnable(mRecordHandle);
    }

    /**
     * 设置回声消除
     *
     * @param bool
     */
    public void setAcousticEcho(Context context, boolean bool) {
        mRecordHandle.setAcousticEcho(context, bool);
    }


    public void startRecord() {

        mEncodeHandle.enableEncode();
        mDecodeHandle.enableDecode();
        mRecordHandle.startRecord();
        mExecutorService = Executors.newFixedThreadPool(3);
        mExecutorService.submit(mDecodeRunnable);
        mExecutorService.submit(mEncodeRunnable);
        mExecutorService.submit(mRecordRunnable);
    }

    public void stopRecord() {
        mExecutorService.shutdownNow();
        DecodeQueueManager.getInstance().setEnable(false);
        RecordQueueManager.getInstance().setEnable(false);
        mEncodeHandle.disableEncode();
        mDecodeHandle.disableDecode();
        mRecordHandle.stopRecord();
    }


}
