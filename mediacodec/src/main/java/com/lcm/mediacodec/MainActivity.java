package com.lcm.mediacodec;

import android.media.MediaFormat;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lcm.mediacodec.audio.AudioDecode;
import com.lcm.mediacodec.audio.AudioEncode;
import com.lcm.mediacodec.audio.RecordCaptor;


public class MainActivity extends AppCompatActivity {
    private AudioDecode audioDecode;
    private AudioEncode audioEncode;

    private RecordCaptor recordCaptor;
    private AudioEncode recordEncode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void decode(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                audioDecode = new AudioDecode(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3");
                audioDecode.decodeAudio(Environment.getExternalStorageDirectory() + "/recorders/test.pcm");
                long end = System.currentTimeMillis();
                Log.e("MainActivity", "用时：" + (end - start) + "毫秒");
            }
        }).start();
    }


    public void encode(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                audioEncode = new AudioEncode();
                audioEncode.encodeFile(Environment.getExternalStorageDirectory() + "/recorders/test.pcm", Environment.getExternalStorageDirectory() + "/recorders/test.aac", MediaFormat.MIMETYPE_AUDIO_AAC);
                long end = System.currentTimeMillis();
                Log.e("MainActivity", "用时：" + (end - start) + "毫秒");
            }
        }).start();
    }


    public void startRecord(View view) {
        if (recordCaptor == null) {
            recordCaptor = new RecordCaptor();
        }
        if (recordEncode == null) {
            recordEncode = new AudioEncode();
            recordEncode.initEncodeData(Environment.getExternalStorageDirectory() + "/recorders/record.aac");
        }
        recordCaptor.setRecordListener(new RecordCaptor.RecordListener() {
            @Override
            public void flushData(byte[] data) {
                //将录音数据编码保存到文件中
                if (recordEncode != null) recordEncode.encodeData(data);
            }
        });
        recordCaptor.startRecord();
    }


    public void stopRecord(View view) {
        if (recordCaptor != null) recordCaptor.stopRecord();
        if(recordEncode != null) recordEncode.release();
    }


}
