package com.lcm.mediacodec.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.lcm.mediacodec.utils.MappedFileReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/4 6:13 PM
 * Desc:
 * *****************************************************************
 */
public class AudioEncode {
    private static final String TAG = "AudioEncode";

    private String mSourcePath;
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
    private FileOutputStream fos;


    public void encode(String sourcePath, String outPath, String mimeType) {
        this.mSourcePath = sourcePath;
        this.mOutPath = outPath;
        if (!initMediaFormat(mimeType) || mEncoder == null) {
            Log.e(TAG, "create mediaEncode failed");
            return;
        }

        mEncoder.start();
        mEncodeInputBuffers = mEncoder.getInputBuffers(); // 获取输入的缓冲区
        mEncodeOutputBuffers = mEncoder.getOutputBuffers(); //获取输出的缓冲区
        mEncodeBufferInfo = new MediaCodec.BufferInfo(); //用于描述编码得到的byte信息

        try {
            MappedFileReader reader = new MappedFileReader(mSourcePath, 1024 * 100);
            while (reader.read() != -1) {
                byte[] bytes = reader.getArray();
                Log.e(TAG, "读取文件字节：" + bytes.length);
                encodePCM(bytes);
            }
            mEncoder.stop();
            mEncoder.release();
            reader.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean initMediaFormat(String mimeType) {
        try {
            mMediaFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount); //根据格式类型、取样率、声道数创建MediaFormat
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate); //设置比特率
            if (mimeType.equals(MediaFormat.MIMETYPE_AUDIO_AAC)) { //仅编码AAC文件需要配置
                //描述要使用的AAC配置文件的键
                mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            }
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


    /**
     * 编码字节
     *
     * @param data
     */
    private void encodePCM(byte[] data) {
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


    /**
     * Add ADTS header at the beginning of each and every AAC packet. This is
     * needed as MediaCodec encoder generates a packet of raw AAC data.
     * <p>
     * Note the packetLen must count in the ADTS header itself.
     * 编码AAC音频需要在每一块的头部添加ADTS，包含了AAC文件的采样率、通道数、帧数据长度等信息
     **/
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // Channel Configurations

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
