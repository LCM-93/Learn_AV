#!/bin/sh
make clean
export TMPDIR="/Users/chenming/AndroidStudioProjects/ffmpeg/tmpdir"
NDK=/Library/Android_SDK/ndk-bundle-r16
API=19
PLATFORM=arm-linux-androideabi
TOOLCHAIN=$NDK/toolchains/$PLATFORM-4.9/prebuilt/darwin-x86_64
SYSROOT=$NDK/platforms/android-$API/arch-arm/
ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/$PLATFORM
CPU=arm
PREFIX=$(pwd)/android/$CPU
build_one()
{
./configure \
--prefix=$PREFIX \
--target-os=android \
--arch=arm \
--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
--disable-asm \
--enable-cross-compile \
--enable-shared \
--disable-static \
--enable-runtime-cpudetect \
--disable-doc \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-doc \
--disable-symver \
--enable-small \
--enable-gpl --enable-nonfree --enable-version3 --disable-iconv \
--enable-jni \
--enable-mediacodec \
--disable-decoders --enable-decoder=vp9 --enable-decoder=h264 --enable-decoder=mpeg4 --enable-decoder=aac \
--disable-encoders --enable-encoder=vp9_vaapi --enable-encoder=h264_nvenc --enable-encoder=h264_v4l2m2m --enable-encoder=hevc_nvenc \
--disable-demuxers --enable-demuxer=rtsp --enable-demuxer=rtp --enable-demuxer=flv --enable-demuxer=h264 \
--disable-muxers --enable-muxer=rtsp --enable-muxer=rtp --enable-muxer=flv --enable-muxer=h264 \
--disable-parsers --enable-parser=mpeg4video --enable-parser=aac --enable-parser=h264 --enable-parser=vp9 \
--disable-protocols --enable-protocol=rtmp --enable-protocol=rtp --enable-protocol=tcp --enable-protocol=udp \
--disable-bsfs \
--disable-indevs --enable-indev=v4l2 \
--disable-outdevs \
--disable-filters \
--disable-postproc \
--sysroot=$SYSROOT \
--extra-libs=-lgcc \
--extra-cflags="-I$ASM -isysroot $ISYSROOT -D__ANDROID_API__=$API -U_FILE_OFFSET_BITS -Os -fPIC -DANDROID -D__thumb__ -mthumb -Wfatal-errors -Wno-deprecated -mfloat-abi=softfp -marm" \
--extra-ldflags="-marm"
$ADDITIONAL_CONFIGURE_FLAG
}
build_one
make clean
make -j4
make install