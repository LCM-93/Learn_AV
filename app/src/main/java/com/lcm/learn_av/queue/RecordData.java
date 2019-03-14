package com.lcm.learn_av.queue;


import java.util.Arrays;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/14 4:48 PM
 * Desc:
 * *****************************************************************
 */
public class RecordData extends AudioData {

    private float bjVolume;

    public RecordData(byte[] data) {
        super(data);
    }


    public float getBjVolume() {
        return bjVolume;
    }

    public void setBjVolume(float bjVolume) {
        this.bjVolume = bjVolume;
    }

    @Override
    public String toString() {
        return "RecordData{" +
                "bjVolume=" + bjVolume +
                ", data=" + Arrays.toString(data) +
                '}';
    }


}
