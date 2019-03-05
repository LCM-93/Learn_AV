package com.lcm.mediacodec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lcm.mediacodec.audio.AudioDecode;
import com.lcm.mediacodec.audio.AudioEncode;
import com.lcm.mediacodec.audio.WavEncode;

public class MainActivity extends AppCompatActivity {
    private AudioDecode audioDecode;
    private AudioEncode audioEncode;
    private WavEncode wavEncode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void decode(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = SystemClock.currentThreadTimeMillis();
                audioDecode = new AudioDecode(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3");
                audioDecode.decodeAudio(Environment.getExternalStorageDirectory() + "/recorders/test.pcm");
                long end = SystemClock.currentThreadTimeMillis();
                Log.e("MainActivity","用时："+(end-start));
            }
        }).start();
    }


    public void encode(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = SystemClock.currentThreadTimeMillis();
                audioEncode = new AudioEncode();
                audioEncode.encode(Environment.getExternalStorageDirectory() + "/recorders/test.pcm",Environment.getExternalStorageDirectory() + "/recorders/test.aac", MediaFormat.MIMETYPE_AUDIO_AAC);
                long end = SystemClock.currentThreadTimeMillis();
                Log.e("MainActivity","用时："+(end-start));
            }
        }).start();
    }
}
