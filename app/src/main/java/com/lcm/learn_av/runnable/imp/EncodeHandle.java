package com.lcm.learn_av.runnable.imp;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 5:39 PM
 * Desc:
 * *****************************************************************
 */
public interface EncodeHandle {


    /**
     * 初始化
     */
    void initEncode(String outPath);


    /**
     * 编码
     * @param data
     */
    void encode(byte[] data);

    /**
     * 是否允许编码
     * @return
     */
    boolean isEncodeEnable();

    /**
     * 禁止编码
     */
    void disableEncode();

    void enableEncode();

    /**
     * 释放资源
     */
    void release();
}
