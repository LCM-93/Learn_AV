package com.lcm.learn_av.record;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.lcm.learn_av.queue.AudioData;
import com.lcm.learn_av.queue.DecodeQueueManager;
import com.lcm.learn_av.queue.RecordData;
import com.lcm.learn_av.queue.RecordQueueManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/14 5:45 PM
 * Desc:
 * *****************************************************************
 */
public class AudioEncode {
    private static AudioEncode instance;
    private Thread thread;

    private AudioEncode() {
    }

    public static AudioEncode getInstance() {
        if (instance == null) {
            synchronized (AudioEncode.class) {
                if (instance == null) {
                    instance = new AudioEncode();
                }
            }
        }
        return instance;
    }


    private static final String TAG = "AudioEncode";

    private String mOutPath;
    private MediaFormat mMediaFormat;
    private String mimeType;
    private MediaCodec mEncoder;

    private ByteBuffer[] mEncodeInputBuffers;
    private ByteBuffer[] mEncodeOutputBuffers;
    private MediaCodec.BufferInfo mEncodeBufferInfo;

    private int presentationTimeUs = 0;

    private int sampleRate = 44100; //采样率
    private int channelCount = 2; //声道数
    private int bitRate = 128000; //比特率
    private int inputBufferSize = 100 * 1024; //inputBuffer的大小
    private int aacProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    private FileOutputStream fos;

    private boolean startEncode;

    private AudioTrack mAudioTrack;


    public void init(String outPath) {
        this.mOutPath = outPath;
        if (!initMediaFormat()) {
            return;
        }
        initAudioTrack();
        mEncoder.start();
        mEncodeInputBuffers = mEncoder.getInputBuffers(); // 获取输入的缓冲区
        mEncodeOutputBuffers = mEncoder.getOutputBuffers(); //获取输出的缓冲区
        mEncodeBufferInfo = new MediaCodec.BufferInfo(); //用于描述编码得到的byte信息


        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                encode();
            }
        });
    }


    private boolean initMediaFormat() {
        try {
            mimeType = MediaFormat.MIMETYPE_AUDIO_AAC;
            mMediaFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount); //根据格式类型、取样率、声道数创建MediaFormat
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate); //设置比特率
            //描述要使用的AAC配置文件的键
            mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile);
            mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, inputBufferSize); //输入缓存区的最大大小
            mEncoder = MediaCodec.createEncoderByType(mimeType); //根据格式类型创建MediaCodec
            mEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); //设置配置文件

            fos = new FileOutputStream(mOutPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void start() {
        mAudioTrack.play();
        startEncode = true;
        thread.start();
    }

    public void stop() {
        startEncode = false;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(mAudioTrack != null){
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }


    private void encode() {
        while (startEncode) {
            RecordData recordData = RecordQueueManager.getInstance().getRecordData();
            AudioData audioData = DecodeQueueManager.getInstance().getAudioData();
            if(audioData!=null){
                mAudioTrack.write(audioData.getData(),0,audioData.getData().length);
            }

            if(recordData != null && audioData != null) {
                Log.e(TAG, "recordData:" + recordData.getData().length + "  audioData:" + audioData.getData().length);
                byte[] bytes = mixAudio(recordData.getData(),audioData.getData(),  0.3f);
                encodeData(bytes);
            }
        }
    }

    /**
     * 初始化AudioTrack
     */
    private void initAudioTrack(){
        //计算需要最小缓冲区大小
        int bufSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        //创建AudioTrack
        //AudioFormat.CHANNEL_OUT_STEREO 双声道  AudioFormat.CHANNEL_OUT_MONO 单声道
        //AudioFormat.ENCODING_PCM_16BIT  AudioFormat.ENCODING_PCM_8BIT  采样精度
        //AudioTrack.MODE_STREAM 通过write()方法将数据一次次写入到AudioTrack中进行播放    AudioTrack.MODE_STATIC 把音频放入一个固定的buffer 一次性传给AudioTrack
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT,bufSize,AudioTrack.MODE_STREAM);
    }


    private byte[] mixAudio(byte[] audioData, byte[] bytes, float volume) {
        short[] cret = new short[audioData.length / 2];
        for (int i = 0; i < audioData.length / 2; i++) {
            short c = (short) ((audioData[i * 2] & 0xFF) | ((audioData[i * 2 + 1] & 0xFF) << 8));
            short c1 = (short) ((bytes[i * 2] & 0xFF) | ((bytes[i * 2 + 1] & 0xFF) << 8));
            cret[i] = (short) ((c + c1 * 0.5 * volume) / 2);
        }

        byte[] ret = new byte[audioData.length];
        for (int i = 0; i < cret.length; i++) {
            ret[i * 2] = (byte) ((cret[i] & 0x00FF));
            ret[i * 2 + 1] = (byte) ((cret[i] & 0xFF00) >> 8);
        }
        return ret;
    }


    private void encodeData(byte[] data) {
        ByteBuffer inputBuffer;
        ByteBuffer outputBuffer;
        int outPacketSize;
        int inputIndex = mEncoder.dequeueInputBuffer(-1); //获取可用的inputBuffer -1代表一直等待，0表示不等待  建议-1避免丢帧
        if (inputIndex >= 0) {
            inputBuffer = mEncodeInputBuffers[inputIndex]; //拿到inputBuffer
            inputBuffer.clear();
            inputBuffer.limit(data.length);
            inputBuffer.put(data); //填入待编码数据
            mEncoder.queueInputBuffer(inputIndex, 0, data.length, 0, 0); //通知编码器进行编码
        }

        //获取编码得到的byte[]数据，并将描述信息附加到mEncodeBufferInfo中， timeoutUs为等待时间， -1代表一直等待，0代表不等待，此处单位为微秒
        // 建议不要填-1 有些时候并没有数据输出，那么就会一直卡在这
        int outputIndex = mEncoder.dequeueOutputBuffer(mEncodeBufferInfo, 10000);
        //可能一次读取不完缓存区的信息，分多次去读
        while (outputIndex >= 0) {
            outPacketSize = mEncodeBufferInfo.size + 7;//7为ADTS头部
            outputBuffer = mEncodeOutputBuffers[outputIndex]; //获取outputBuffer
            outputBuffer.position(mEncodeBufferInfo.offset);
            outputBuffer.limit(mEncodeBufferInfo.offset + mEncodeBufferInfo.size);
            byte[] outData = new byte[outPacketSize];
            addADTStoPacket(outData, outPacketSize); //添加ADTS头部信息到数组中
            outputBuffer.get(outData, 7, mEncodeBufferInfo.size); //将编码后的byte数据添加到数组中
            outputBuffer.position(mEncodeBufferInfo.offset);

            try {
                fos.write(outData, 0, outData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //释放资源 此方法必须调用
            mEncoder.releaseOutputBuffer(outputIndex, false);
            outputIndex = mEncoder.dequeueOutputBuffer(mEncodeBufferInfo, 10000);
        }
    }


    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = aacProfile; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = channelCount; // Channel Configurations

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

}
