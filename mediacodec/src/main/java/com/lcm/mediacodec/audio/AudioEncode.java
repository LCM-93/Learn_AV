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
        mEncodeInputBuffers = mEncoder.getInputBuffers();
        mEncodeOutputBuffers = mEncoder.getOutputBuffers();
        mEncodeBufferInfo = new MediaCodec.BufferInfo();

        try {
            MappedFileReader reader = new MappedFileReader(mSourcePath, 65535);
            while (reader.read() != -1) {
                byte[] bytes = reader.getArray();
                Log.e(TAG, "读取文件字节：" + bytes.length);
                encodePCM(bytes);
            }
            reader.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean initMediaFormat(String mimeType) {
        try {
            mMediaFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            if (mimeType.equals(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            }
            mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, inputBufferSize);
            mEncoder = MediaCodec.createEncoderByType(mimeType);
            mEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            fos = new FileOutputStream(mOutPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 编码字节
     * @param data
     */
    private void encodePCM(byte[] data) {
        ByteBuffer inputBuffer;
        ByteBuffer outputBuffer;
        int outPacketSize;
        int inputIndex = mEncoder.dequeueInputBuffer(-1);
        if (inputIndex >= 0) {
            inputBuffer = mEncodeInputBuffers[inputIndex];
            inputBuffer.clear();
            inputBuffer.limit(data.length);
            inputBuffer.put(data);
            mEncoder.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
        }

        int outputIndex = mEncoder.dequeueOutputBuffer(mEncodeBufferInfo, 10000);
        while (outputIndex >= 0) {
            outPacketSize = mEncodeBufferInfo.size + 7;//7为ADTS头部
            outputBuffer = mEncodeOutputBuffers[outputIndex];
            outputBuffer.position(mEncodeBufferInfo.offset);
            outputBuffer.limit(mEncodeBufferInfo.offset + mEncodeBufferInfo.size);
            byte[] outData = new byte[outPacketSize];
            addADTStoPacket(outData, outPacketSize);
            outputBuffer.get(outData, 7, mEncodeBufferInfo.size);
            outputBuffer.position(mEncodeBufferInfo.offset);

            try {
                fos.write(outData, 0, outData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mEncoder.releaseOutputBuffer(outputIndex, false);
            outputIndex = mEncoder.dequeueOutputBuffer(mEncodeBufferInfo, 10000);
        }

    }


    /**
     * Add ADTS header at the beginning of each and every AAC packet. This is
     * needed as MediaCodec encoder generates a packet of raw AAC data.
     * <p>
     * Note the packetLen must count in the ADTS header itself.
     **/
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 1; // Channel Configurations

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
