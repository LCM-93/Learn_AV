package com.lcm.mediacodec.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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

    private AudioTrack mAudioTrack;

    public AudioDecode(String sourcePath) {
        this.mSourcePath = sourcePath;
        initMediaDecode();
        initAudioTrack();
    }


    /**
     * 初始化AudioTrack
     */
    private void initAudioTrack(){
        //计算需要最小缓冲区大小
        int bufSize = AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        //创建AudioTrack
        //AudioFormat.CHANNEL_OUT_STEREO 双声道  AudioFormat.CHANNEL_OUT_MONO 单声道
        //AudioFormat.ENCODING_PCM_16BIT  AudioFormat.ENCODING_PCM_8BIT  采样精度
        //AudioTrack.MODE_STREAM 通过write()方法将数据一次次写入到AudioTrack中进行播放    AudioTrack.MODE_STATIC 把音频放入一个固定的buffer 一次性传给AudioTrack
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT,bufSize,AudioTrack.MODE_STREAM);
    }

    /**
     * 初始化MediaDecode
     */
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
            int bitRate = mMediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);//比特数
            Log.i(TAG,"比特数: "+bitRate);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始解码
     * @param outPath
     */
    public void decodeAudio(String outPath) {
        try {
            mCodec = MediaCodec.createDecoderByType(mMediaFormat.getString(MediaFormat.KEY_MIME)); //根据type创建MediaCodec
            mCodec.configure(mMediaFormat, null, null, 0); //配置 MediaCodec
            mCodec.start(); //开始解码

            mAudioTrack.play();

            FileOutputStream fosStream = new FileOutputStream(outPath);

            ByteBuffer[] inputBuffers = mCodec.getInputBuffers(); //获取输入的缓存区
            ByteBuffer[] outputBuffers = mCodec.getOutputBuffers(); //获取输出的缓冲区

            //用于描述解码得到的byte信息
            mDecodeBufferInfo = new MediaCodec.BufferInfo();

            boolean bIsEos = false;
            final long kTimeOutUs = 5000;

            while (!Thread.interrupted()) {
                if (!bIsEos) {
                    int inputBufIndex = mCodec.dequeueInputBuffer(kTimeOutUs); //获取可用的inputBuffer -1代表一直等待 0代表不等待 建议-1 避免丢帧，这里
                    if (inputBufIndex >= 0) {

                        ByteBuffer buffer = inputBuffers[inputBufIndex]; //获取inputBuffer
                        int sampleSize = mMediaExtractor.readSampleData(buffer, 0); //MediaExtractor读取数据到inputBuffer中
                        if (sampleSize < 0) { //小于0 代表所有数据已经读取完毕
                            mCodec.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            bIsEos = true;
                        } else {
                            //通知MediaDecode解码刚刚传入的数据
                            mCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, mMediaExtractor.getSampleTime(), 0);
                            mMediaExtractor.advance(); //继续下一取样
                        }
                    }
                }

                //获取解码得到的byte[]数据  timeoutUs为等待时间 -1代表一直等待 0代表不等待 此处单位为微秒  此处建议不要填-1 有些时候并没有数据输出  那么他就会一直卡这  等待
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
                        ByteBuffer outputBuffer = outputBuffers[outIndex]; //拿到用于存放PCM数据的Buffer


                        byte[] data = new byte[mDecodeBufferInfo.size]; // BufferInfo内定义了此数据块的大小
                        outputBuffer.get(data); //将Buffer内的数据取出到字节数组中
                        outputBuffer.clear(); //取出数据后 清空这些buffer

                        //像文件中写入数据
                        fosStream.write(data);
                        //使用AudioTrack播放音频
                        mAudioTrack.write(data,0,data.length);

                        Log.d(TAG, "write data :" + data.length);
                        mCodec.releaseOutputBuffer(outIndex, false); //此操作一定要做 不然MediaCodec用完所有的Buffer后  将不能向外输出数据
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
           if(mAudioTrack != null){
               mAudioTrack.stop();
               mAudioTrack.release();
           }
        }
    }
}
