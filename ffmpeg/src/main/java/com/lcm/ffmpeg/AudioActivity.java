package com.lcm.ffmpeg;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lcm.ffmpeg.R;
import com.lcm.ffmpeg.audio.AudioBuffer;
import com.lcm.ffmpeg.audio.EncodeAndDecode;
import com.lcm.ffmpeg.audio.RecordCaptor;
import com.lcm.ffmpeg.audio.RecordEncode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioActivity extends AppCompatActivity {
    private static final String TAG = "AudioActivity";
    private EncodeAndDecode encodeAndDecode;
    private AudioBuffer audioBuffer;
    private RecordCaptor recordCaptor;
    private Thread encodeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        encodeAndDecode = new EncodeAndDecode();
    }


    public void decodeAudio(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();

                encodeAndDecode.decodeToPCM(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3", Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm");
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
                encodeAndDecode.encodePCMToAAC(Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm", Environment.getExternalStorageDirectory() + "/recorders/ff_test.aac");
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

                int readSize = RecordEncode.getInstance().initAudio(Environment.getExternalStorageDirectory() + "/recorders/ff_test_record.aac");
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
                        RecordEncode.getInstance().encodeAudio(buff);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    RecordEncode.getInstance().close();
                }


            }
        }).start();
    }


    public void initAudio(View view) {
        int readSize = RecordEncode.getInstance().initAudio(Environment.getExternalStorageDirectory() + "/recorders/ff_test_record.aac");
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
                    RecordEncode.getInstance().encodeAudio(frameBuf);
                }
            }
            RecordEncode.getInstance().close();
        }
    }
}
