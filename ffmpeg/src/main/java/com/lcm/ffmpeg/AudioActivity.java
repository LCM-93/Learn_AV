package com.lcm.ffmpeg;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lcm.ffmpeg.audio.AudioBuffer;
import com.lcm.ffmpeg.audio.CmdHandle;
import com.lcm.ffmpeg.audio.EncodeAndDecode;
import com.lcm.ffmpeg.audio.RecordCaptor;
import com.lcm.ffmpeg.audio.RecordEncode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioActivity extends AppCompatActivity {
    private static final String TAG = "AudioActivity";
    private AudioBuffer audioBuffer;
    private RecordCaptor recordCaptor;
    private Thread encodeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
    }


    public void decodeAudio(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();

                EncodeAndDecode.instance.decodeToPCM(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3", Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm");
                long end = System.currentTimeMillis();
                Log.e(TAG, "用时：" + (end - start) + "毫秒");
            }
        }).start();
    }

    public void PCMToAAC(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                EncodeAndDecode.instance.encodePCMToAAC(Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm", Environment.getExternalStorageDirectory() + "/recorders/ff_test.aac");
                long end = System.currentTimeMillis();
                Log.e(TAG, "用时：" + (end - start) + "毫秒");
            }
        }).start();
    }


    public void testRecordEncode(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;

                int readSize = RecordEncode.instance.initAudio(Environment.getExternalStorageDirectory() + "/recorders/ff_test_record.aac");
                if (readSize <= 0) {
                    Log.e(TAG, "initAudio error");
                    return;
                }
                Log.i(TAG, "readSize:" + readSize);
                audioBuffer = new AudioBuffer(readSize);

                try {
                    byte[] buff = new byte[readSize];
                    in = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm"));
                    while (in.read(buff) >= readSize) {
                        Log.i(TAG, "buff.length:" + buff.length);
                        RecordEncode.instance.encodeAudio(buff);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    RecordEncode.instance.close();
                }


            }
        }).start();
    }


    public void initAudio(View view) {
        int readSize = RecordEncode.instance.initAudio(Environment.getExternalStorageDirectory() + "/recorders/ff_test_record.aac");
        if (readSize <= 0) {
            Log.e(TAG, "initAudio error");
            return;
        }
        Log.i(TAG, "readSize:" + readSize);
        audioBuffer = new AudioBuffer(readSize);

        encodeThread = new Thread(new EncodeRunnable());

        recordCaptor = new RecordCaptor();
        recordCaptor.setRecordListener(new RecordCaptor.RecordListener() {
            @Override
            public void flushData(byte[] data) {
                audioBuffer.put(data, 0, data.length);
            }
        });
    }


    public void startRecord(View view) {
        recordCaptor.startRecord();
        encodeThread.start();
    }


    public void stopRecord(View view) {
        recordCaptor.stopRecord();
    }

    //编码线程
    private class EncodeRunnable implements Runnable {

        @Override
        public void run() {
            while (recordCaptor.isRecording() || !audioBuffer.isEmpty()) {
                byte[] frameBuf = audioBuffer.getFrameBuf();
                if (frameBuf != null) {
                    RecordEncode.instance.encodeAudio(frameBuf);
                }
            }
            RecordEncode.instance.close();
        }
    }


    /**
     * 测试调用cmd
     */
    public void testCmd(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                String[] strings = cutAudio(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3", 60, 60, Environment.getExternalStorageDirectory() + "/recorders/cut_bj.mp3");
                CmdHandle.instance.executeCmd(strings);
                long end = System.currentTimeMillis();
                Log.e(TAG, "剪切用时：" + (end - start) + "毫秒");

            }
        }).start();
    }


    public  String[] cutAudio(String srcFile, int startTime, int duration, String targetFile) {
        String cutAudioCmd = "ffmpeg -y -ss %d -t %d -i %s -acodec copy %s";
        cutAudioCmd = String.format(cutAudioCmd, startTime, duration, srcFile, targetFile);
        Log.i(TAG, "commands:------->" + cutAudioCmd);
        return cutAudioCmd.split(" ");//以空格分割为字符串数组
    }
}
