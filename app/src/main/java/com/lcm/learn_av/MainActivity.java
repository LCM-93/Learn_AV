package com.lcm.learn_av;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.lcm.learn_av.audio.MediaManage;
import com.lcm.learn_av.queue.RecordQueueManager;
import com.lcm.learn_av.record.AudioDecode;
import com.lcm.learn_av.record.AudioEncode;
import com.lcm.learn_av.record.RecordCaptor;

public class MainActivity extends AppCompatActivity {

    private SeekBar mSeekbar;

    private RecordCaptor mRecordCaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSeekbar = findViewById(R.id.seekBar);

        mRecordCaptor = new RecordCaptor();

        mRecordCaptor.setRecordListener(new RecordCaptor.RecordListener() {
            @Override
            public void flushData(byte[] data) {
                RecordQueueManager.getInstance().put(data, 0, data.length);
            }
        });
    }


    public void init(View view) {
        mRecordCaptor.init();
        AudioDecode.getInstance().init(Environment.getExternalStorageDirectory() + "/recorders/bj.mp3");
        AudioEncode.getInstance().init(Environment.getExternalStorageDirectory() + "/recorders/out.aac");

    }


    public void start(View view) {
        mRecordCaptor.startRecord();
        AudioDecode.getInstance().startDecode();
        AudioEncode.getInstance().start();
    }


    public void stop(View view) {
        mRecordCaptor.stopRecord();
        AudioDecode.getInstance().stopDecode();
        AudioEncode.getInstance().stop();
    }


    public void audition(View view){
        MediaManage.playSound(Environment.getExternalStorageDirectory() + "/recorders/out.aac", new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MediaManage.stop();
            }
        });
    }

}
