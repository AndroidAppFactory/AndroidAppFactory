package com.bihe0832.android.lib.ffmpeg;

/**
 * 总结怎么通过 [mobile-ffmpeg](https://github.com/tanersener/mobile-ffmpeg) 构建自定义扩展的 ffmpeg
 *
 * 官方构建脚本：https://github.com/tanersener/mobile-ffmpeg/wiki/android.sh
 *
 * @author code@bihe0832.com
 *         Created on 2024/1/2.
 *         Description: 当前扩展为 ffmpeg 核心加上了 libvidstab 和 xvidcore，视频编码使用 openh264
 * @Document 自定义构建流程总结
 *
 *         ！！！--------  构建流程 --------！！！
 *
 *         1.【获取源码】 从 git clone 工程源码
 *         2.【解决编译报错】将 mobile-ffmpeg 根目录的 android 工程导入 Android Studio，修改 AGP等配置，是项目编译不报错
 *         3. 从 app 的 build.gradle 查看对应的 NDK 及 cmake 版本，然后利用 Android Studio 的SDK工具下载对应的版本
 *         4. 将 NDK 设置环境变量 ANDROID_NDK_ROOT，并将 $ANDROID_NDK_ROOT/prebuilt/darwin-x86_64/bin 添加到系统环境变量
 *         5. 复制 $ANDROID_NDK_ROOT/prebuilt/darwin-x86_64/bin 的 yasm 并命名为 nasm，放在同文件夹
 *         6. 根据官方指引，安装基础的依赖库，具体查看：https://github.com/tanersener/mobile-ffmpeg/wiki/Android-Prerequisites
 *         7. 在根目录根据自己的需求，运行命令构建ffmpeg，例如:
 *
 *         ./android.sh --disable-arm-v7a-neon --enable-libpng --enable-openh264 --enable-libvidstab --enable-xvidcore --enable-gpl
 *
 *         8. 构建失败时，可以在根目录的 build.log 中查看具体的错误原因，修复后继续构建
 *
 *
 *         ！！！--------  常见问题 --------！！！
 *
 *         1. Can't exec "aclocal": No such file or directory at
 *         /opt/homebrew/Cellar/autoconf/2.71/share/autoconf/Autom4te/FileUtils.pm line 274.
 *
 *         autoreconf: error: aclocal failed with exit status: 2
 *
 *         【解决方法】：https://stackoverflow.com/questions/76852766/error-cant-exec-aclocal-with-homebrew-installed-autoreconf-on-mac
 *         【具体命令】：brew install automake
 *
 *         2. make报错：'aclocal-1.15'
 *
 *         【解决方法】：https://blog.csdn.net/Max_Shui/article/details/106327029
 *         【具体命令】：手动安装 1.15.1
 *         sudo tar xzf automake-1.15.1.tar.gz
 *         cd automake-1.16.1
 *         sudo ./configure --prefix=/usr/local/Cellar/automake/1.16.1
 *         sudo make
 *         sudo make install
 *         brew link automake
 *         automake --version
 *
 *         3. 运行时报错
 */