#!/bin/sh
make clean
API=19
CPU=armeabi-v7a
PLATFORM=arm-linux-androideabi

# 缓存文件目录
export TMPDIR="/Users/chenming/AndroidStudioProjects/ffmpeg-4.1/tmpdir"

# NDK环境
NDK=/Library/Android_SDK/ndk-bundle-r16
TOOLCHAIN=$NDK/toolchains/$PLATFORM-4.9/prebuilt/darwin-x86_64
SYSROOT=$NDK/platforms/android-$API/arch-arm/

# NDK16 后头文件目录改动了
ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/$PLATFORM

# 要保存的动态库目录 
PREFIX=$(pwd)/android/$CPU

# 生成 xxx.so 动态包
build_libs()
{
	./configure \
	--prefix=$PREFIX \
	--target-os=linux \
	--arch=$CPU \
	--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	--disable-asm \
	--enable-cross-compile \
	--disable-static \
	--enable-shared \
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

echo "开始编译"

build_libs
make clean
make -j4
make install

echo "编译结束"