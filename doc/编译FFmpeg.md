

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
>
> [将FFmpeg编译成一个libffmpeg.so](https://cloud.tencent.com/developer/article/1334943)

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
```

#### 开始编译

```
sudo chmod +x build_android.sh

./build_android.sh
```



####  编译成一个libffmpeg.so 包

先生成所有的 xxx.a 静态包，再讲所有的静态包打包成一个 .so包

打静态包 需要把

```
	--disable-static \
	--enable-shared \
```

替换成

```
	--enable-static \
	--disable-shared \
```

[`build_android_one.sh`](https://github.com/lichenming0516/Learn_AV/blob/master/doc/build_android_one.sh)

```
#!/bin/sh
make clean
API=19
CPU=armeabi
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

# 生成 xxx.a 静态包
build_libs()
{
	./configure \
	--prefix=$PREFIX \
	--target-os=linux \
	--arch=$CPU \
	--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	--disable-asm \
	--enable-cross-compile \
	--enable-static \
	--disable-shared \
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

# 打包所有xxx.a静态包  生成一个libffmpeg.so包
packet_one()
{
	$TOOLCHAIN/bin/$PLATFORM-ld \
	-rpath-link=$SYSROOT/usr/lib \
	-L$SYSROOT/usr/lib \
	-L$PEFIX/lib \
	-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
	$PREFIX/libffmpeg.so \
	libavcodec/libavcodec.a \
	libavfilter/libavfilter.a \
	libavformat/libavformat.a \
	libavutil/libavutil.a \
	libswresample/libswresample.a \
	libswscale/libswscale.a \
	-lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
	$TOOLCHAIN/lib/gcc/$PLATFORM/4.9.x/libgcc.a

	$TOOLCHAIN/bin/$PLATFORM-strip $PREFIX/libffmpeg.so
}

echo "开始打包"

packet_one

echo "打包结束"
```



## 引入FFmpeg

[Android Studio通过cmake创建FFmpeg项目](https://blog.csdn.net/eydwyz/article/details/78757262)

