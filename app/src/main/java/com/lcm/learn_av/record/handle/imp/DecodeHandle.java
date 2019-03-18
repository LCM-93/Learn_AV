package com.lcm.learn_av.record.handle.imp;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 5:33 PM
 * Desc:
 * *****************************************************************
 */
public interface DecodeHandle {


    /**
     * 初始化
     */
    void initDecode(String bgMusicSrc);

    /**
     * 解码
     */
    void decode();

    /**
     * 禁止解码
     */
    void disableDecode();

    void enableDecode();

    /**
     * 是否解码
     */
    boolean isEnableDecode();

    /**
     * 释放资源
     */
    void release();
}
