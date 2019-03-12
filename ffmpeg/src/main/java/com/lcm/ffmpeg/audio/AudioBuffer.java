package com.lcm.ffmpeg.audio;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/12 3:56 PM
 * Desc:
 * *****************************************************************
 */
public class AudioBuffer {
    private int encodeFrameSize;
    private ByteBuffer buf;
    private ArrayBlockingQueue<byte[]> queue;
    private int count;

    public AudioBuffer(int encodeFrameSize) {
        this.encodeFrameSize = encodeFrameSize;
        buf = ByteBuffer.allocate(encodeFrameSize * 5);
        buf.mark();
        queue = new ArrayBlockingQueue<>(200);
    }


    public void put(byte[] data, int offset, int size) {
        buf.put(data, offset, size);
        int position = buf.position();

        while (position >= encodeFrameSize) {
            byte[] frameBuf = new byte[encodeFrameSize];
            buf.reset();
            buf.get(frameBuf, 0, encodeFrameSize);
            try {
                count++;
                queue.put(frameBuf);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] all = buf.array();
            buf.reset();
            buf.put(all, encodeFrameSize, position - encodeFrameSize);
            position = buf.position();
        }
    }


    public byte[] getFrameBuf() {
        if (!isEmpty()) {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }
}
