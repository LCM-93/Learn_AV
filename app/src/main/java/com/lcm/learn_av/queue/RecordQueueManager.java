package com.lcm.learn_av.queue;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/14 4:53 PM
 * Desc:
 * *****************************************************************
 */
public class RecordQueueManager {
    private static final String TAG = "RecordQueueManager";
    private ByteBuffer buf;
    private LinkedBlockingQueue<RecordData> queue;

    private static RecordQueueManager instance;

    private boolean enable;

    public void setEnable(boolean enable) {
        this.enable = enable;
        queue.clear();
    }

    public static RecordQueueManager getInstance() {
        if (instance == null) {
            synchronized (RecordQueueManager.class) {
                if (instance == null) {
                    instance = new RecordQueueManager();
                }
            }
        }
        return instance;
    }

    private RecordQueueManager() {
        buf = ByteBuffer.allocate(AudioData.DATA_SIZE * 5);
        buf.mark();
        queue = new LinkedBlockingQueue<>(2);
        enable = true;
    }

    public void put(byte[] data, int offset, int size) {
        if (!enable) return;
        Log.i(TAG,"put record "+size);
        buf.put(data, offset, size);
        int position = buf.position();

        while (position >= AudioData.DATA_SIZE) {
            byte[] frameBuf = new byte[AudioData.DATA_SIZE];
            buf.reset();
            buf.get(frameBuf, 0, AudioData.DATA_SIZE);

            try {
                queue.put(new RecordData(frameBuf));
                Log.e(TAG, "<<<<<<<< 录音数据 "+queue.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] all = buf.array();
            buf.reset();
            buf.put(all, AudioData.DATA_SIZE, position - AudioData.DATA_SIZE);
            position = buf.position();
        }
    }

    public RecordData getRecordData() {
        if (!enable) {
            return null;
        }
        try {

            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }
}
