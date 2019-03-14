package com.lcm.learn_av.queue;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/14 4:49 PM
 * Desc:
 * *****************************************************************
 */
public class AudioData {
    public static final int DATA_SIZE = 1024 * 100;

    public AudioData(byte[] data) {
        this.data = data;
    }

    protected byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
