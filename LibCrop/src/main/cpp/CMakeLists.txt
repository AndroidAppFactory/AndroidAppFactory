# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -frtti -std=c++11 -fexceptions -fvisibility=hidden -Wall")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 从系统环境变量中获取路径
set(NDK_BUILD_PATH $ENV{NDK_BUILD_PATH})

add_library(aafcrop SHARED
        uCrop.cpp)

find_library(log-lib log)
find_library(android-lib android)
find_library(z-lib z)


add_library(libpng STATIC IMPORTED)
set_target_properties(libpng PROPERTIES IMPORTED_LOCATION ${NDK_BUILD_PATH}/libpng/obj/local/${ANDROID_ABI}/libpng.a)

add_library(libjpeg STATIC IMPORTED)
set_target_properties(libjpeg PROPERTIES IMPORTED_LOCATION ${NDK_BUILD_PATH}/libjpeg/obj/local/${ANDROID_ABI}/libjpeg9.a)


target_include_directories(aafcrop PRIVATE ${NDK_BUILD_PATH}/libpng/jni/)
target_include_directories(aafcrop PRIVATE ${NDK_BUILD_PATH}/libjpeg/jni/)


target_link_libraries(aafcrop
        ${log-lib}
        ${android-lib}
        ${z-lib}
        libpng
        libjpeg)
