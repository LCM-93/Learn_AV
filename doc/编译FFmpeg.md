

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
>
> [编译Android平台使用的FFmpeg(armeabi,armeabi-v7a,arm64-v8a,x86,x86_64)](https://blog.csdn.net/yuxiatongzhi/article/details/80096802)

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

ANDROID_ARMV5_CFLAGS='-march=armv5te -marm'
ANDROID_ARMV7_CFLAGS='-march=armv7-a -mfloat-abi=softfp -mfpu=neon -marm'
ANDROID_ARMV8_CFLAGS='-march=armv8-a'
ANDROID_X86_CFLAGS='-march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32'
ANDROID_X86_64_CFLAGS='-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel'

CFLAGS_ARRAY[0]=${ANDROID_ARMV5_CFLAGS}
CFLAGS_ARRAY[1]=${ANDROID_ARMV7_CFLAGS} 
CFLAGS_ARRAY[2]=${ANDROID_ARMV8_CFLAGS} 
CFLAGS_ARRAY[3]=${ANDROID_X86_CFLAGS}
CFLAGS_ARRAY[4]=${ANDROID_X86_64_CFLAGS}

ARCH_ARRAY=(arm arm arm64 x86 x86_64)
ANDROID_ARCH_ABI_ARRAY=(armeabi armeabi-v7a arm64-v8a x86 x86_64)
PLATFORM_ARRAY=(arm-linux-androideabi arm-linux-androideabi aarch64-linux-android x86 x86_64)
PLATFORM_COROSS_ARRAY=(arm-linux-androideabi arm-linux-androideabi aarch64-linux-android i686-linux-android x86_64-linux-android)

#配置INDEX 切换打包版本
INDEX=1

API=19
TOOLCHAIN_VERSION=4.9
BUILD_PLATFORM=darwin-x86_64
ARCH=${ARCH_ARRAY[$INDEX]}
CFLAGS=${CFLAGS_ARRAY[$INDEX]}
ANDROID_ARCH_ABI=${ANDROID_ARCH_ABI_ARRAY[$INDEX]}
PLATFORM=${PLATFORM_ARRAY[$INDEX]}
PLATFORM_COROSS=${PLATFORM_COROSS_ARRAY[$INDEX]}

# 缓存文件目录
export TMPDIR="/Users/chenming/AndroidStudioProjects/ffmpeg-4.1/tmpdir"

# NDK环境
NDK=/Library/Android_SDK/ndk-bundle-r16
TOOLCHAIN=$NDK/toolchains/$PLATFORM-$TOOLCHAIN_VERSION/prebuilt/${BUILD_PLATFORM}
SYSROOT=$NDK/platforms/android-$API/arch-$ARCH

# NDK16 后头文件目录改动了
ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/$PLATFORM_COROSS

# 要保存的动态库目录 
PREFIX=$(pwd)/android/$ANDROID_ARCH_ABI
COROSS_PREFIX=${TOOLCHAIN}/bin/$PLATFORM_COROSS-

echo "-------------------------------------------------------------------------------------"
echo "| ANDROID_ARCH_ABI : ${ANDROID_ARCH_ABI}"
echo "-------------------------------------------------------------------------------------"
echo "| ARCH : ${ARCH} (CPU)"
echo "-------------------------------------------------------------------------------------"
echo "| API : ${API}"
echo "-------------------------------------------------------------------------------------"
echo "| TOOLCHAIN : ${TOOLCHAIN}"
echo "-------------------------------------------------------------------------------------"
echo "| SYSROOT : ${SYSROOT} (android 库文件目录)"
echo "-------------------------------------------------------------------------------------"
echo "| SYSROOT_INCLUDE : ${ASM} (Android 库头文件目录)"
echo "-------------------------------------------------------------------------------------"


# 生成 xxx.so 动态包
build_libs()
{
	./configure \
	--prefix=$PREFIX \
	--target-os=android \
	--arch=$ARCH \
	--cross-prefix=$COROSS_PREFIX \
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
	--extra-cflags="-I$ASM -isysroot $ISYSROOT -D__ANDROID_API__=$API -U_FILE_OFFSET_BITS $CFLAGS -Os -fPIC -DANDROID -Wfatal-errors -Wno-deprecated" \
	--extra-cxxflags="-D__thumb__ -fexceptions -frtti" \
	--extra-ldflags="-L$SYSROOT/usr/lib" \
	$ADDITIONAL_CONFIGURE_FLAG
}
echo "-------------------------------------------------------------------------------------"
echo "| 开始编译"
echo "-------------------------------------------------------------------------------------"
build_libs
make clean
make -j4
make install
echo "-------------------------------------------------------------------------------------"
echo "| 编译${ANDROID_ARCH_ABI}结束"
echo "-------------------------------------------------------------------------------------"
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

ANDROID_ARMV5_CFLAGS='-march=armv5te -marm'
ANDROID_ARMV7_CFLAGS='-march=armv7-a -mfloat-abi=softfp -mfpu=neon -marm'
ANDROID_ARMV8_CFLAGS='-march=armv8-a'
ANDROID_X86_CFLAGS='-march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32'
ANDROID_X86_64_CFLAGS='-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel'

CFLAGS_ARRAY[0]=${ANDROID_ARMV5_CFLAGS}
CFLAGS_ARRAY[1]=${ANDROID_ARMV7_CFLAGS} 
CFLAGS_ARRAY[2]=${ANDROID_ARMV8_CFLAGS} 
CFLAGS_ARRAY[3]=${ANDROID_X86_CFLAGS}
CFLAGS_ARRAY[4]=${ANDROID_X86_64_CFLAGS}

ARCH_ARRAY=(arm arm arm64 x86 x86_64)
ANDROID_ARCH_ABI_ARRAY=(armeabi armeabi-v7a arm64-v8a x86 x86_64)
PLATFORM_ARRAY=(arm-linux-androideabi arm-linux-androideabi aarch64-linux-android x86 x86_64)
PLATFORM_COROSS_ARRAY=(arm-linux-androideabi arm-linux-androideabi aarch64-linux-android i686-linux-android x86_64-linux-android)

#配置INDEX 切换打包版本
INDEX=3

API=19
TOOLCHAIN_VERSION=4.9
BUILD_PLATFORM=darwin-x86_64
ARCH=${ARCH_ARRAY[$INDEX]}
CFLAGS=${CFLAGS_ARRAY[$INDEX]}
ANDROID_ARCH_ABI=${ANDROID_ARCH_ABI_ARRAY[$INDEX]}
PLATFORM=${PLATFORM_ARRAY[$INDEX]}
PLATFORM_COROSS=${PLATFORM_COROSS_ARRAY[$INDEX]}

# 缓存文件目录
export TMPDIR="/Users/chenming/AndroidStudioProjects/ffmpeg-4.1/tmpdir"

# NDK环境
NDK=/Library/Android_SDK/ndk-bundle-r16
TOOLCHAIN=$NDK/toolchains/$PLATFORM-$TOOLCHAIN_VERSION/prebuilt/${BUILD_PLATFORM}
SYSROOT=$NDK/platforms/android-$API/arch-$ARCH

# NDK16 后头文件目录改动了
ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/$PLATFORM_COROSS

# 要保存的动态库目录 
PREFIX=$(pwd)/android/$ANDROID_ARCH_ABI
COROSS_PREFIX=${TOOLCHAIN}/bin/$PLATFORM_COROSS-

echo "-------------------------------------------------------------------------------------"
echo "| ANDROID_ARCH_ABI : ${ANDROID_ARCH_ABI}"
echo "-------------------------------------------------------------------------------------"
echo "| ARCH : ${ARCH} (CPU)"
echo "-------------------------------------------------------------------------------------"
echo "| API : ${API}"
echo "-------------------------------------------------------------------------------------"
echo "| TOOLCHAIN : ${TOOLCHAIN}"
echo "-------------------------------------------------------------------------------------"
echo "| SYSROOT : ${SYSROOT} (android 库文件目录)"
echo "-------------------------------------------------------------------------------------"
echo "| SYSROOT_INCLUDE : ${ASM} (Android 库头文件目录)"
echo "-------------------------------------------------------------------------------------"


# 生成 xxx.so 动态包
build_libs()
{
	./configure \
	--prefix=$PREFIX \
	--target-os=android \
	--arch=$ARCH \
	--cross-prefix=$COROSS_PREFIX \
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
	--extra-cflags="-I$ASM -isysroot $ISYSROOT -D__ANDROID_API__=$API -U_FILE_OFFSET_BITS $CFLAGS -Os -fPIC -DANDROID -Wfatal-errors -Wno-deprecated" \
	--extra-cxxflags="-D__thumb__ -fexceptions -frtti" \
	--extra-ldflags="-L$SYSROOT/usr/lib" \
	$ADDITIONAL_CONFIGURE_FLAG
}
echo "-------------------------------------------------------------------------------------"
echo "| 开始编译静态包"
echo "-------------------------------------------------------------------------------------"
build_libs
make clean
make -j4
make install
echo "-------------------------------------------------------------------------------------"
echo "| 静态包编辑完成"
echo "-------------------------------------------------------------------------------------"

# 打包所有xxx.a静态包  生成一个libffmpeg.so包
packet_one()
{
	$TOOLCHAIN/bin/$PLATFORM_COROSS-ld \
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
	$TOOLCHAIN/lib/gcc/$PLATFORM_COROSS/4.9.x/libgcc.a

	$TOOLCHAIN/bin/$PLATFORM_COROSS-strip $PREFIX/libffmpeg.so
}

echo "-------------------------------------------------------------------------------------"
echo "| 开始打包"
echo "-------------------------------------------------------------------------------------"

packet_one

echo "-------------------------------------------------------------------------------------"
echo "| 打包${ANDROID_ARCH_ABI}结束"
echo "-------------------------------------------------------------------------------------"

```



## 引入FFmpeg

[Android Studio通过cmake创建FFmpeg项目](https://blog.csdn.net/eydwyz/article/details/78757262)

