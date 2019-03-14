package com.lcm.learn_av.audio;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by huanglongfei on 2017/8/8.
 */

public class MediaManage {

    public static MediaPlayer mMediaPlayer;

    private static AudioManager mAudioManager;

    private static boolean isPause;

    public static void playSound(AssetFileDescriptor fileDescriptor, MediaPlayer.OnCompletionListener
            onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }

    public static void playSound(Context context, AssetFileDescriptor fileDescriptor, MediaPlayer.OnCompletionListener
            onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }
            mAudioManager.setSpeakerphoneOn(true);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }

    public static void playSound(AssetFileDescriptor fileDescriptor, MediaPlayer.OnPreparedListener onPreparedListener,
                                 MediaPlayer.OnCompletionListener onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }


    public static void playSound(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }

    public static void playSound(AssetFileDescriptor fileDescriptor, MediaPlayer.OnCompletionListener
            onCompletionListener, boolean loop) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }


    public static void playSound(String filePath, MediaPlayer.OnPreparedListener onPreparedListener, MediaPlayer
            .OnCompletionListener onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }

    public static void playSoundAsync(Context context, String filePath, MediaPlayer.OnPreparedListener
            onPreparedListener, MediaPlayer.OnCompletionListener onCompletionListener) {

        if (filePath == null) {
            return;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            setAudioOutputSource(context);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }

    public static void setAudioOutputSource(Context context) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        if (mAudioManager.isBluetoothA2dpOn()) {
            changeToHeadset(context);
        } else {
            changeToSpeaker(context);
        }
    }

    public static void playSoundSideCache(Context context, String proxyUrl, MediaPlayer.OnPreparedListener
            onPreparedListener, MediaPlayer.OnCompletionListener onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }

        try {
            setAudioOutputSource(context);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setDataSource(context, Uri.parse(proxyUrl));
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }

    }

    public static void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

            mMediaPlayer.stop();
        }
    }

    public static void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

            mMediaPlayer.pause();
            isPause = true;
        }
    }

    public static void release() {
        if (mMediaPlayer != null) {

            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public static void resume() {
        if (mMediaPlayer != null && isPause) {

            mMediaPlayer.start();
            isPause = false;
        }
    }

    public static void mute() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(0.0F, 0.0F);
        }
    }

    public static int getCurrentPosition() {
        if (mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getCurrentPosition();
    }

    public static int getDuration() {
        if (mMediaPlayer == null) {
            return 1;
        }
        return mMediaPlayer.getDuration();
    }

    public static boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * 切换到外放
     */
    public static void changeToSpeaker(Context context) {
        //注意此处，蓝牙未断开时使用MODE_IN_COMMUNICATION而不是MODE_NORMAL
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        mAudioManager.setMode(mAudioManager.isBluetoothA2dpOn() ? AudioManager.MODE_IN_COMMUNICATION :
                AudioManager.MODE_NORMAL);
        mAudioManager.stopBluetoothSco();
        mAudioManager.setBluetoothScoOn(false);
        mAudioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到蓝牙音箱
     */
    public static void changeToHeadset(Context context) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.startBluetoothSco();
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.setSpeakerphoneOn(false);
    }

}