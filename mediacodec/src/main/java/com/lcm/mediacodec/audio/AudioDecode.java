package com.lcm.mediacodec.audio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/4 3:19 PM
 * Desc: 解码音频信息
 * *****************************************************************
 */
public class AudioDecode {
    private static final String TAG = "AudioDecode";
    private String mSourcePath;
    private MediaFormat mMediaFormat;
    private MediaCodec mCodec;
    private MediaCodec.BufferInfo mDecodeBufferInfo;
    private MediaExtractor mMediaExtractor;

    public AudioDecode(String sourcePath) {
        this.mSourcePath = sourcePath;
        initMediaDecode();
    }

    private void initMediaDecode() {
        try {
            //获取音视频文件中的关键信息
            mMediaExtractor = new MediaExtractor();
            //设置数据源
            mMediaExtractor.setDataSource(mSourcePath);
            //获取媒体轨道个数
            int trackCount = mMediaExtractor.getTrackCount();
            Log.i(TAG, "媒体轨道数: " + trackCount);

            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = mMediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME); //格式的类型
                if (mime.startsWith("audio")) {
                    mMediaExtractor.selectTrack(i);
                    mMediaFormat = format;
                    break;
                }
            }

            String mime = mMediaFormat.getString(MediaFormat.KEY_MIME); //格式的类型
            Log.i(TAG, "MIME: " + mime);
            int rate = mMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE); //采样率
            Log.i(TAG, "采样率: " + rate);
            int channelCount = mMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT); //通道数
            Log.i(TAG, "通道数: " + channelCount);
            long duration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);//时长
            Log.i(TAG, "时长: " + duration);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decodeAudio(String outPath) {
        try {
            mCodec = MediaCodec.createDecoderByType(mMediaFormat.getString(MediaFormat.KEY_MIME)); //根据type创建MediaCodec
            mCodec.configure(mMediaFormat, null, null, 0); //配置 MediaCodec
            mCodec.start(); //开始解码

            FileOutputStream fosStream = new FileOutputStream(outPath);

            ByteBuffer[] inputBuffers = mCodec.getInputBuffers(); //获取输入的缓存区
            ByteBuffer[] outputBuffers = mCodec.getOutputBuffers(); //获取输出的缓冲区
            Log.w(TAG, "buffers: " + inputBuffers.length);

            //用于描述解码得到的byte信息
            mDecodeBufferInfo = new MediaCodec.BufferInfo();

            boolean bIsEos = false;
            final long kTimeOutUs = 5000;

            while (!Thread.interrupted()) {
                if (!bIsEos) {
                    int inputBufIndex = mCodec.dequeueInputBuffer(kTimeOutUs);
                    if (inputBufIndex >= 0) {
                        //读取一帧数据至buffer
                        ByteBuffer buffer = inputBuffers[inputBufIndex];
                        int sampleSize = mMediaExtractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            mCodec.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            bIsEos = true;
                        } else {
                            //通知MediaDecode解码刚刚传入的数据
                            mCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, mMediaExtractor.getSampleTime(), 0);
                            mMediaExtractor.advance(); //继续下一取样
                        }
                    }
                }

                int outIndex = mCodec.dequeueOutputBuffer(mDecodeBufferInfo, 0);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        outputBuffers = mCodec.getOutputBuffers();
                        Log.w(TAG, "INFO_OUTPUT_BUFFERS_CHANGED " + outputBuffers.length);
                        break;

                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.w(TAG, "New format " + mCodec.getOutputFormat());
                        break;

                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.w(TAG, "dequeueOutputBuffer timed out!");
                        break;

                    default:
                        ByteBuffer outputBuffer = outputBuffers[outIndex];


                        byte[] data = new byte[mDecodeBufferInfo.size];
                        outputBuffer.get(data);
                        outputBuffer.clear();
                        fosStream.write(data);

                        Log.d(TAG, "write data :" + data.length);
                        mCodec.releaseOutputBuffer(outIndex, false);
                        break;
                }

                if ((mDecodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.w(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
           if( mCodec != null){
               mCodec.release();
               mCodec = null;
           }
           if(mDecodeBufferInfo != null){
               mDecodeBufferInfo = null;
           }
        }
    }
}
