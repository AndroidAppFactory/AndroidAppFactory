# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.14)
project(aafcrop)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -frtti -std=c++11 -fexceptions -fvisibility=hidden -Wall")

# 16KB 页面对齐支持（Google Play 要求）
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

include(FetchContent)

# ==================== libjpeg-turbo (2.1.5.1 — 最后一个支持 add_subdirectory 的版本) ====================
FetchContent_Declare(
        libjpeg-turbo
        GIT_REPOSITORY https://github.com/libjpeg-turbo/libjpeg-turbo.git
        GIT_TAG 2.1.5.1
        GIT_SHALLOW TRUE
)
set(ENABLE_SHARED OFF CACHE BOOL "" FORCE)
set(ENABLE_STATIC ON CACHE BOOL "" FORCE)
set(WITH_TURBOJPEG OFF CACHE BOOL "" FORCE)
set(WITH_JAVA OFF CACHE BOOL "" FORCE)
set(REQUIRE_SIMD OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(libjpeg-turbo)

# ==================== zlib (Android NDK 自带) ====================
find_library(z-lib z)

# ==================== libpng ====================
FetchContent_Declare(
        libpng
        GIT_REPOSITORY https://github.com/glennrp/libpng.git
        GIT_TAG v1.6.44
        GIT_SHALLOW TRUE
)
set(PNG_SHARED OFF CACHE BOOL "" FORCE)
set(PNG_STATIC ON CACHE BOOL "" FORCE)
set(PNG_TESTS OFF CACHE BOOL "" FORCE)
set(PNG_TOOLS OFF CACHE BOOL "" FORCE)
set(SKIP_INSTALL_ALL ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(libpng)

# ==================== aafcrop 主库 ====================

add_library(aafcrop SHARED
        uCrop.cpp)

find_library(log-lib log)
find_library(android-lib android)

target_include_directories(aafcrop PRIVATE
        ${libjpeg-turbo_SOURCE_DIR}
        ${libjpeg-turbo_BINARY_DIR}
        ${libpng_SOURCE_DIR}
        ${libpng_BINARY_DIR}
)

target_link_libraries(aafcrop
        ${log-lib}
        ${android-lib}
        ${z-lib}
        png_static
        jpeg-static
)
