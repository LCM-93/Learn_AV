package com.lcm.ffmpeg;

import android.content.Intent;
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

    public void test(View view) {
        FFmpegPlayer fFmpegPlayer = new FFmpegPlayer();
        fFmpegPlayer.testMyMedia(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3");
    }


    public void audio(View view) {
        Intent intent = new Intent(this, AudioActivity.class);
        startActivity(intent);
    }
}
