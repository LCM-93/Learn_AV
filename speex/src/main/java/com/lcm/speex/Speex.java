package com.lcm.speex;


/**
 * ****************************************************************
 * Author: LiChenMing.Chaman
 * Date: 2019/3/19 2:09 PM
 * Desc:
 * *****************************************************************
 */
public class Speex {
    private final static String TAG = "Speex";
    /* quality
     * 1 : 4kbps (very noticeable artifacts, usually intelligible)
     * 2 : 6kbps (very noticeable artifacts, good intelligibility)
     * 4 : 8kbps (noticeable artifacts sometimes)
     * 6 : 11kpbs (artifacts usually only noticeable with headphones)
     * 8 : 15kbps (artifacts not usually noticeable)
     */
    private static final int DEFAULT_COMPRESSION = 4;
    //private Logger log = LoggerFactory.getLogger(Speex.class);

    static {
        System.loadLibrary("audio_speex");
    }

    public native int open(int compression);
    public native int getFrameSize();
    public native int decode(byte encoded[], short lin[], int size);
    public native int encode(short lin[], int offset, byte encoded[], int size);
    public native void close();
}