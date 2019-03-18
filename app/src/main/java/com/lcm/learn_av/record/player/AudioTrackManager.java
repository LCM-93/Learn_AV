package com.lcm.learn_av.record.player;

import android.media.AudioManager;
import android.media.AudioTrack;

import com.lcm.learn_av.record.Config;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/16 4:43 PM
 * Desc:
 * *****************************************************************
 */
public class AudioTrackManager {
    private static final String TAG = "AudioTrackManager";

    private static AudioTrackManager instance;

    public static AudioTrackManager getInstance() {
        if (instance == null) {
            synchronized (AudioTrackManager.class) {
                if (instance == null) {
                    instance = new AudioTrackManager();
                }
            }
        }
        return instance;
    }

    private AudioTrack mAudioTrack;

    private int sampleRateInHz = Config.SAMPLE_RATE_IN_HZ;
    private int outChannel = Config.AUDIOFORMAT_OUT_CHANNEL;
    private int codingBit = Config.AUDIOFORAM_BIT;

    /**
     * 初始化AudioTrack
     */
    public void init(int audioSessionId) {
        //计算需要最小缓冲区大小
        int bufSize = AudioTrack.getMinBufferSize(sampleRateInHz, outChannel, codingBit);
        if (audioSessionId == -1) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, outChannel, codingBit, bufSize *2, AudioTrack.MODE_STREAM);
        } else {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, outChannel, codingBit, bufSize *2 , AudioTrack.MODE_STREAM, audioSessionId);
        }
        mAudioTrack.play();
    }


    /**
     * 播放
     *
     * @param data
     * @param offset
     * @param length
     */
    public void play(byte[] data, int offset, int length) {
        if (mAudioTrack == null) {
            return;
        }
        mAudioTrack.write(data, offset, length);
    }


    public void release() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

}
