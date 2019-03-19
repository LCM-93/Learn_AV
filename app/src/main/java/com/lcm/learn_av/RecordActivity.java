package com.lcm.learn_av;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lcm.learn_av.audio.MediaManage;
import com.lcm.learn_av.manager.RecordManager;

public class RecordActivity extends AppCompatActivity {
    private RecordManager mRecordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
    }

    public void init(View view) {
        mRecordManager = new RecordManager();
        mRecordManager.init(this, true, Environment.getExternalStorageDirectory() + "/recorders/bj.mp3", Environment.getExternalStorageDirectory() + "/recorders/record_out.aac");
    }

    public void start(View view) {
        mRecordManager.startRecord();
    }

    public void stop(View view) {
        mRecordManager.stopRecord();
    }


    public void audition(View view) {
        MediaManage.playSound(Environment.getExternalStorageDirectory() + "/recorders/record_out.aac", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MediaManage.stop();
            }
        });
    }

    public void audition1(View view) {
        MediaManage.playSound(Environment.getExternalStorageDirectory() + "/recorders/record_out_1.aac", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MediaManage.stop();
            }
        });
    }
}
