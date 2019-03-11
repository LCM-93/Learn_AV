package com.lcm.ffmpeg;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AudioActivity extends AppCompatActivity {
    private FFmpegPlayer fFmpegPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        fFmpegPlayer = new FFmpegPlayer();
    }


    public void decodeAudio(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();

                fFmpegPlayer.decodeToPCM(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3",Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm");
                long end = System.currentTimeMillis();
                Log.e("MainActivity", "用时：" + (end - start) + "毫秒");
            }
        }).start();
    }

    public void PCMToAAC(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                fFmpegPlayer.encodePCMToAAC(Environment.getExternalStorageDirectory() + "/recorders/ff_test.pcm", Environment.getExternalStorageDirectory() + "/recorders/ff_test.aac");
                long end = System.currentTimeMillis();
                Log.e("MainActivity", "用时：" + (end - start) + "毫秒");
            }
        }).start();
    }
}
