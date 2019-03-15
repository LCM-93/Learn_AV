package com.lcm.learn_av.runnable;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.lcm.learn_av.runnable.imp.EncodeHandle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/15 2:23 PM
 * Desc:
 * *****************************************************************
 */
public class EncodeHandleImp implements EncodeHandle {
    private static final String TAG = "EncodeHandleImp";

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
    private int inputBufferSize = 10 * 1024; //inputBuffer的大小
    private int aacProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    private FileOutputStream fos;

    private boolean isEncodeEnable;


    public EncodeHandleImp(String outPath) {
        initEncode(outPath);
    }


    @Override
    public void initEncode(String outPath) {
        this.mOutPath = outPath;
        if (!initMediaFormat()) {
            Log.e(TAG, "initMediaFormat error");
            return;
        }
        mEncoder.start();
        isEncodeEnable = true;
        mEncodeInputBuffers = mEncoder.getInputBuffers(); // 获取输入的缓冲区
        mEncodeOutputBuffers = mEncoder.getOutputBuffers(); //获取输出的缓冲区
        mEncodeBufferInfo = new MediaCodec.BufferInfo(); //用于描述编码得到的byte信息
    }


    @Override
    public boolean isEncodeEnable() {
        return isEncodeEnable && mEncoder != null;
    }
    @Override
    public void disableEncode() {
        isEncodeEnable = false;
    }

    @Override
    public void enableEncode() {
        isEncodeEnable = true;
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

    @Override
    public void encode(byte[] data) {
        if (!isEncodeEnable) {
            return;
        }
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


    @Override
    public void release() {
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
        }
        mEncodeBufferInfo = null;
        mEncodeInputBuffers = null;
        mEncodeOutputBuffers = null;
        mMediaFormat = null;
    }


}
