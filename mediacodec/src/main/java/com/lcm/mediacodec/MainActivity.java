package com.lcm.mediacodec;

import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lcm.mediacodec.audio.AudioDecode;
import com.lcm.mediacodec.audio.WavEncode;

public class MainActivity extends AppCompatActivity {
    private AudioDecode audioDecode;

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
                wavEncode = new WavEncode();
                wavEncode.pcmToWav(Environment.getExternalStorageDirectory() + "/recorders/test.pcm",Environment.getExternalStorageDirectory() + "/recorders/test.wav");
                long end = SystemClock.currentThreadTimeMillis();
                Log.e("MainActivity","用时："+(end-start));
            }
        }).start();
    }
}
