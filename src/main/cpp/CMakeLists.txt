cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")
add_library(
        gpush
        SHARED
        gpush.cpp
        GQueue.cpp
        RtmpPush.cpp
        librtmp/amf.c
        librtmp/hashswf.c
        librtmp/log.c
        librtmp/parseurl.c
        librtmp/rtmp.c
        GCallJava.cpp
        native-lib.cpp
        RecordBuffer.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        gpush
        OpenSLES
        ${log-lib}
)