

## 编译FFmpeg

> 参考文章
>
> 《Android音视频开发》--何俊林
>
> [编译针对于Android平台的ffmpeg 4.1版本(最新版)](https://blog.csdn.net/qq_34902522/article/details/87879145)
>
> [FFmpeg编译报错[libavfilter/aeval.o] Error 1](https://www.jianshu.com/p/e8c6c634bcf5)
>
> [ffmpeg configure配置选项](https://blog.csdn.net/momo0853/article/details/78043903)

### 环境

* macOS 10.14.3
* ndk-bundle-r16
* ffmpeg-4.1

### 过程

#### 配置NDK环境

修改 `.bash_profile`

```
export ANDROID_HOME=/Library/Android_SDK
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/platform-tools

export NDK_ROOT=$PATH:$ANDROID_HOME/ndk-bundle-r16
export PATH=$PATH:$NDK_ROOT
```

生效环境

```
$ source .bash_profile 
```

#### 修改configure文件

打开ffmpeg源码目录下`configure`文件

```
SLIBNAME_WITH_MAJOR='$(SLIBNAME).$(LIBMAJOR)'
LIB_INSTALL_EXTRA_CMD='$$(RANLIB) "$(LIBDIR)/$(LIBNAME)"'
SLIB_INSTALL_NAME='$(SLIBNAME_WITH_VERSION)'
SLIB_INSTALL_LINKS='$(SLIBNAME_WITH_MAJOR) $(SLIBNAME)'
```

修改为

```
SLIBNAME_WITH_MAJOR='$(SLIBPREF)$(FULLNAME)-$(LIBMAJOR)$(SLIBSUF)'
LIB_INSTALL_EXTRA_CMD='$$(RANLIB) "$(LIBDIR)/$(LIBNAME)"'
SLIB_INSTALL_NAME='$(SLIBNAME_WITH_MAJOR)'
SLIB_INSTALL_LINKS='$(SLIBNAME)'
```

#### 创建编译脚本

[`build_android.sh`](https://github.com/lichenming0516/Learn_AV/blob/master/doc/build_android.sh)

```
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
```

#### 开始编译

```
sudo chmod +x build_android.sh

./build_android.sh
```

