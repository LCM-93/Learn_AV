package com.lcm.learn_av.runnable.imp;

import android.content.Context;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 5:41 PM
 * Desc:
 * *****************************************************************
 */
public interface RecordHandle {

    /**
     * 初始化
     */
    void initRecord();

    /**
     * 开始录音
     */
    void startRecord();


    /**
     * 设置消除回声
     * @param context
     * @param bool
     */
    void setAcousticEcho(Context context, boolean bool);

    /**
     * 停止录音
     */
    void stopRecord();

    void release();

    /**
     * 读取录音数据
     */
    void readData();

    /**
     * 是否在录音
     * @return
     */
    boolean isRecording();
}
