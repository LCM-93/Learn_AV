## CMake编译Speex

> 参考文章
>
> [speex 在android上降噪与回音消除使用](https://blog.csdn.net/qq_27688259/article/details/80525215)
>
> [audio_speex](https://github.com/CL-window/audio_speex)
>
> [speex算法在android上的移植](https://blog.csdn.net/zkw12358/article/details/25339003)

### 获取Speex相关源码

[speex官网](https://www.speex.org/downloads/)中最新版的源码中已经将编解码部分与降噪回音处理部分分离，这里我需要的是音频降噪的功能，所以下载[Speex DSP Source Code](https://ftp.osuosl.org/pub/xiph/releases/speex/speexdsp-1.2rc3.tar.gz)。

新建一个Android项目，在main目录下新建cpp目录，将源码中`include`与`libspeexdsp`目录全部拷贝到cpp目录下，将`include/speex`目录下的`speexdsp_config_types.h.in`文件复制一份，并且重命名为`speexdsp_config_types.h`，修改其中的内容如下

```c
#ifndef __SPEEX_TYPES_H__
#define __SPEEX_TYPES_H__

#if defined HAVE_STDINT_H
#  include <stdint.h>
#elif defined HAVE_INTTYPES_H
#  include <inttypes.h>
#elif defined HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

typedef short spx_int16_t;
typedef unsigned short spx_uint16_t;
typedef int spx_int32_t;
typedef unsigned int spx_uint32_t;

#endif
```



### 编写CMake配置文件

在cpp目录下新建`CMakeLists.txt`，如果你是在创建项目的时候选择了支持C++，可能`CMakeLists.txt`文件是在项目目录下，没有关系，只是在配置的时候需要注意一下源码的相关路径不要写错。

```c
# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.
cmake_minimum_required(VERSION 3.4.1)

# 添加宏定义
add_definitions(-D FIXED_POINT)
add_definitions(-D USE_KISS_FFT)
add_definitions(-U HAVE_CONFIG_H)

# 在执行CMAKE时打印消息 注意大小写
# message([SEND_ERROR | STATUS | FATAL_ERROR] "message to display" ...)
message(STATUS "Source Directory: ${CMAKE_CURRENT_SOURCE_DIR}")

message(STATUS "Android ABI: ${ANDROID_ABI}")
message(STATUS "Android API LEVEL: ${ANDROID_NATIVE_API_LEVEL}")

# 添加头文件目录,指定头文件与库文件路径 include_directories与link_directories 可以多次调用以设置多个路径
include_directories("include")

#源码所在位置
file(GLOB_RECURSE ALL_PROJ_SRC "libspeexdsp/*.c")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.
add_library( # Sets the name of the library.
        audio_speex
        SHARED
        speex_jni.cpp
        ${ALL_PROJ_SRC})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build. 查找库所在目录
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        audio_speex

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
```

这里需要注意的是在文件中配置的一些宏定义，因为源码中会根据不同的定义走不同的逻辑。(ps：我不会告诉你因为这几个宏定义浪费了我多少时间😂)

```
# 添加宏定义
add_definitions(-D FIXED_POINT)
add_definitions(-D USE_KISS_FFT)
add_definitions(-U HAVE_CONFIG_H)
```

### 配置build.gradle

在项目的build.gradle中配置`CMakeLists.txt`文件

```c
android {
    compileSdkVersion 28
    
    defaultConfig {
        applicationId "com.lcm.speex"
        minSdkVersion 19
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags ""
                abiFilters 'x86', 'armeabi-v7a'
            }
        }
    }

    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

```

点击 `rebuild project`

### Error

1、源码中几个test开头的文件可以全部注释掉，都是一些测试代码 `testdenoise.c`、`testecho.c`、`testjitter.c`、`testresample.c`

2、

```c
 libspeexdsp/buffer.c
 Error:(49, 1) error: unknown type name 'EXPORT'
```

查看参考文档有两种解决方案

第一种就是将所有 `EXPORT`替换为`JNIEXPORT` ，同时需要在文件头部添加 `#include <jni.h>`，注意需要替换的文件可能比较多。

第二种是说的是 `solution2: use ndk,ndk need change include head , result is ok`，应该是修改NDK的相关配置，我暂时没有搞懂😂



### 降噪处理代码



