package com.lcm.learn_av.manager;

import android.content.Context;

import com.lcm.learn_av.queue.DecodeQueueManager;
import com.lcm.learn_av.queue.RecordQueueManager;
import com.lcm.learn_av.record.handle.DecodeHandleImp;
import com.lcm.learn_av.record.handle.DecodeRunnable;
import com.lcm.learn_av.record.handle.EncodeHandleImp;
import com.lcm.learn_av.record.handle.EncodeRunnable;
import com.lcm.learn_av.record.handle.FFmpegDecodeHandleImp;
import com.lcm.learn_av.record.handle.FFmpegEncodeHandleImp;
import com.lcm.learn_av.record.handle.RecordHandleImp;
import com.lcm.learn_av.record.handle.RecordRunnable;
import com.lcm.learn_av.record.handle.imp.DecodeHandle;
import com.lcm.learn_av.record.handle.imp.EncodeHandle;
import com.lcm.learn_av.record.handle.imp.RecordHandle;

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


    public void init(Context context, boolean isRemoveEcho, String bgPath, String outPath) {
        mRecordHandle = new RecordHandleImp();
        mRecordHandle.initRecord(context, isRemoveEcho);

        mDecodeHandle = new FFmpegDecodeHandleImp(-1, bgPath);
        mEncodeHandle = new EncodeHandleImp(outPath);

        mDecodeRunnable = new DecodeRunnable(mDecodeHandle);
        mEncodeRunnable = new EncodeRunnable(mEncodeHandle);
        mRecordRunnable = new RecordRunnable(mRecordHandle);
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
