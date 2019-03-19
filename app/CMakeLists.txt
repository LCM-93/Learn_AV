
cmake_minimum_required(VERSION 3.4.1)

# 添加宏定义
add_definitions(-D FIXED_POINT)
add_definitions(-D USE_KISS_FFT)
add_definitions(-U HAVE_CONFIG_H)

file(GLOB_RECURSE FFMPEG_CMD_SRC "src/main/jni/ffmpeg/include/*.c")
#file(GLOB_RECURSE ALL_SPEEX_SRC "src/main/jni/speex/libspeex/*.c")
file(GLOB_RECURSE ALL_SPEEXDSP_SRC "src/main/jni/speex/libspeexdsp/*.c")

add_library(
        cmffmpeg
        SHARED
        src/main/jni/encodeAndDecode.c
        src/main/jni/recordEncode.c
        src/main/jni/recordDecode.c
        src/main/jni/cmdHandle.c
        src/main/jni/speex_jni.cpp
        src/main/jni/speex_dsp_jni.c
        ${FFMPEG_CMD_SRC}
        ${ALL_SPEEXDSP_SRC})


# 设置ffmpeg库所在目录
set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/${ANDROID_ABI})

add_library(avcodec
        SHARED
        IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libavcodec.so)


#添加libavdevice-57.so
add_library(avdevice
        SHARED
        IMPORTED)
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libavdevice.so)

add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libavfilter.so)

add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libavformat.so)

add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libavutil.so)

add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libswresample.so)

add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libswscale.so)


#add_library(ffmpeg
#        SHARED
#        IMPORTED)
#set_target_properties(ffmpeg
#        PROPERTIES IMPORTED_LOCATION
#        ${distribution_DIR}/libffmpeg.so)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# 添加ffmpeg头文件路径
include_directories(src/main/jni/ffmpeg/include
                src/main/jni/speex/include )

target_link_libraries( # Specifies the target library.

        cmffmpeg

        avcodec
        avdevice
        avfilter
        avformat
        avutil
        swresample
        swscale

        #        ffmpeg

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})