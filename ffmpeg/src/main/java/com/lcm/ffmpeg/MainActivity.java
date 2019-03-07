package com.lcm.ffmpeg;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void test(View view){
        FFmpegPlayer fFmpegPlayer = new FFmpegPlayer();
        fFmpegPlayer.playMyMedia(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3");
    }
}
