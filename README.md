# AndroidAppFactory


## 背景

沉淀、提升效率

## 概述

待补充

### 使用方式

所有的组件都可以单独使用，同时为了方便开发，直接添加了下面三个组件，方便快速使用

![LibWrapper](https://img.shields.io/badge/AndroidAppFactory-LibWrapper-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-wrapper/images/download.svg) ](https://bintray.com/bihe0832/android/lib-wrapper/_latestVersion)

- 简介：

    所有基础库的合集，包含了权限声明等，建议平时开发直接使用LibWrapper的最新版
    
- 使用：

		implementation 'com.bihe0832.android:lib-wrapper:1.0.3'
    
![LibUIUtils](https://img.shields.io/badge/AndroidAppFactory-LibUIUtils-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-uiutils/images/download.svg) ](https://bintray.com/bihe0832/android/lib-uiutils/_latestVersion)

- 简介：

    所有UI相关的基础库的合集
    
- 使用：

        implementation 'com.bihe0832.android:lib-uiutils:1.3.10'

![LibUtils](https://img.shields.io/badge/AndroidAppFactory-LibUtils-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-utils/images/download.svg) ](https://bintray.com/bihe0832/android/lib-utils/_latestVersion)

- 简介：

    所有非UI相关的基础库的合集，使用时如果用到特殊库，需要添加对应的权限声明
    
- 使用：

    		implementation 'com.bihe0832.android:lib-utils:1.6.0'

## 组件列表

![RouterAnnotation](https://img.shields.io/badge/AndroidAppFactory-RouterAnnotation-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-router-annotation/images/download.svg) ](https://bintray.com/bihe0832/android/lib-router-annotation/_latestVersion)

- 简介：

	通用路由的annotation工具组件
	
- 使用：

		implementation 'com.bihe0832.android:lib-router-annotation:1.2.1'
	
![Router](https://img.shields.io/badge/AndroidAppFactory-Router-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-router/images/download.svg) ](https://bintray.com/bihe0832/android/lib-router/_latestVersion)

- 简介：

	通用路由的核心库
	
- 使用：

		implementation 'com.bihe0832.android:lib-router:1.2.4'


![RouterCompiler](https://img.shields.io/badge/AndroidAppFactory-RouterCompiler-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-router-compiler/images/download.svg) ](https://bintray.com/bihe0832/android/lib-router-compiler/_latestVersion)

- 简介：

	编译时生成路由表的注解处理器
	
- 使用：

    	kapt "com.bihe0832.android:lib-router-compiler:1.2.1"

![LibAndroid](https://img.shields.io/badge/AndroidAppFactory-LibAndroid-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-android/images/download.svg) ](https://bintray.com/bihe0832/android/lib-android/_latestVersion)

- 简介：

	一些系统接口，由于版本的原因没法使用，直接引入相关源码

- 使用：

		implementation 'com.bihe0832.android:lib-android:1.0.0'


![LibCommonUtils](https://img.shields.io/badge/AndroidAppFactory-LibCommonUtils-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-utils-common/images/download.svg) ](https://bintray.com/bihe0832/android/lib-utils-common/_latestVersion)

- 简介：

    安全的类型转换、时间格式化、自增ID、随机数等

- 使用：

        implementation 'com.bihe0832.android:lib-utils-common:1.2.1'
		
![LibFragmentation_core](https://img.shields.io/badge/AndroidAppFactory-LibFragmentation_core-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-fragmentation-core/images/download.svg) ](https://bintray.com/bihe0832/android/lib-fragmentation-core/_latestVersion)

- 简介：

    单Activity ＋ 多Fragment 模式的Fragment管理框架。对应GitHub：https://github.com/YoKeyword/Fragmentation 目前已经做了二次封装
	
    
- 使用：

        implementation 'com.bihe0832.android:lib-fragmentation-core:1.0.0'
		
![LibFragmentation](https://img.shields.io/badge/AndroidAppFactory-LibFragmentation-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-fragmentation/images/download.svg) ](https://bintray.com/bihe0832/android/lib-fragmentation/_latestVersion)

- 简介：

    单Activity ＋ 多Fragment 模式的Fragment管理框架。对应GitHub：https://github.com/YoKeyword/Fragmentation 目前已经做了二次封装

- 使用：
		
        implementation 'com.bihe0832.android:lib-fragmentation:1.0.1'
        
![LibFragmentation_swipeback](https://img.shields.io/badge/AndroidAppFactory-LibFragmentation_swipeback-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-fragmentation-swipeback/images/download.svg) ](https://bintray.com/bihe0832/android/lib-fragmentation-swipeback/_latestVersion)

- 简介：

    单Activity ＋ 多Fragment 模式的Fragment管理框架。对应GitHub：https://github.com/YoKeyword/Fragmentation 目前已经做了二次封装
    
- 使用：

        implementation 'com.bihe0832.android:lib-fragmentation-swipeback:1.0.1'

![LibFlycoTabLayout](https://img.shields.io/badge/AndroidAppFactory-LibFlycoTabLayout-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-flycotablayout/images/download.svg) ](https://bintray.com/bihe0832/android/lib-flycotablayout/_latestVersion)

- 简介：

    FlycoTabLayout 是一个 Android TabLayout 库，目前有两个 TabLayout。对应github：[https://github.com/H07000223/FlycoTabLayout/blob/master/README_CN.md](https://github.com/H07000223/FlycoTabLayout/blob/master/README_CN.md)
	
- 使用：

        implementation 'com.bihe0832.android:lib-flycotablayout:1.0.0'

![LibQRCode](https://img.shields.io/badge/AndroidAppFactory-LibQRCode-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-qrcode/images/download.svg) ](https://bintray.com/bihe0832/android/lib-qrcode/_latestVersion)

- 简介：

    一款基于zxing的二维码识别类库，目前已经被二次封装

- 使用：

        implementation 'com.bihe0832.android:lib-qrcode:1.0.4'


![LibAdapter](https://img.shields.io/badge/AndroidAppFactory-LibAdapter-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-adapter/images/download.svg?version=1.0.0) ](https://bintray.com/bihe0832/android/lib-adapter/1.0.0/link)

- 简介：
	
	一个更丰富和强大的RecyclerAdapter框架，可以支持：
	
	- 支持一个RecycleView支持多种Item样式
	
	- 添加头部、尾部、空页面
	
	- 自动加载更多等
	
	对应GitHub为：[https://github.com/CymChad/BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)，目前已经在`BaseCard`中进行了二次封装，使用方法可以参考`TestCardActivity`。**建议后续RecycleView都使用该Adapter**。

- 使用：

		implementation 'com.bihe0832.android:lib-adapter:1.0.0'


![LibRefresh](https://img.shields.io/badge/AndroidAppFactory-LibRefresh-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-refresh/images/download.svg) ](https://bintray.com/bihe0832/android/lib-refresh/_latestVersion)

- 简介：

    配合LibAdapter 一起使用的下拉刷新，上滑加载更多的组件，对应Github为：[https://github.com/anzaizai/EasyRefreshLayout](https://github.com/anzaizai/EasyRefreshLayout)

- 使用：

        implementation 'com.bihe0832.android:lib-refresh:1.0.0'
        
![LibThread](https://img.shields.io/badge/AndroidAppFactory-LibThread-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-thread/images/download.svg) ](https://bintray.com/bihe0832/android/lib-thread/_latestVersion)

- 简介：

    线程管理模块，提供了不同优先级（系统级别的优先级）的线程（HandlerThread）及一个有五个线程的线程池
    
- 使用：

        implementation 'com.bihe0832.android:lib-thread:1.2.0'

![LibEncrypt](https://img.shields.io/badge/AndroidAppFactory-LibEncrypt-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-encrypt/images/download.svg) ](https://bintray.com/bihe0832/android/lib-encrypt/_latestVersion)

- 简介：
    
    AES加密，MD5计算，进制转化

- 使用：

        implementation 'com.bihe0832.android:lib-encrypt:1.2.0'

![LibChannel](https://img.shields.io/badge/AndroidAppFactory-LibChannel-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-channel/images/download.svg) ](https://bintray.com/bihe0832/android/lib-channel/_latestVersion)

- 简介：

	渠道号相关，主要是读取渠道号。目前渠道号写在assets下的channel.ini文件
	
- 使用：

		implementation 'com.bihe0832.android:lib-channel:1.2.1'
		
![LibRequest](https://img.shields.io/badge/AndroidAppFactory-LibRequest-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-request/images/download.svg) ](https://bintray.com/bihe0832/android/lib-request/_latestVersion)

- 简介：
    
    请求参数处理，URL校验、合并等
    
- 使用：

        implementation 'com.bihe0832.android:lib-request:1.0.2'

![LibHttpCommon](https://img.shields.io/badge/AndroidAppFactory-LibHttpCommon-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-http-common/images/download.svg) ](https://bintray.com/bihe0832/android/lib-http-common/_latestVersion)

- 简介：

    一款封装HttpURLConnection实现的简单的网络请求的事例，没有做任何处理，将网络请求的内容以String返回

- 使用：

        implementation 'com.bihe0832.android:lib-http-common:1.3.0'

![LibHttpAdvanced](https://img.shields.io/badge/AndroidAppFactory-LibHttpAdvanced-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-http-advanced/images/download.svg) ](https://bintray.com/bihe0832/android/lib-http-advanced/_latestVersion)

- 简介：

    一款封装HttpURLConnection实现的简单的网络请求的事例，会完成网络请求结果的解析，最终网络请求结果（Json）会被处理为对应数据类型

- 使用：

        implementation 'com.bihe0832.android:lib-http-advanced:1.3.0'
    
    
![LibSqlite](https://img.shields.io/badge/AndroidAppFactory-LibSqlite-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-sqlite/images/download.svg) ](https://bintray.com/bihe0832/android/lib-sqlite/_latestVersion)

- 简介：

    Sqlite封装，同时提供了一个key-value的基本数据库

- 使用：

			implementation 'com.bihe0832.android:lib-sqlite:1.0.0'

![LibGson](https://img.shields.io/badge/AndroidAppFactory-LibGson-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-gson/images/download.svg) ](https://bintray.com/bihe0832/android/lib-gson/_latestVersion)

- 简介：

    基于Gson封装的类型安全的转换方法

- 使用：

        implementation 'com.bihe0832.android:lib-gson:1.2.2'

![LibTimer](https://img.shields.io/badge/AndroidAppFactory-LibTimer-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-timer/images/download.svg) ](https://bintray.com/bihe0832/android/lib-timer/_latestVersion)

- 简介：

- 使用：


![Download](https://img.shields.io/badge/AndroidAppFactory-LibConfig-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-config/images/download.svg) ](https://bintray.com/bihe0832/android/lib-config/_latestVersion)

- 简介：

	配置管理相关，支持读取本地配置文件、支持配置保存本地
	
- 使用：

		implementation 'com.bihe0832.android:lib-config:1.2.1'
		
![Download](https://img.shields.io/badge/AndroidAppFactory-LibToast-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-channel/images/download.svg) ](https://bintray.com/bihe0832/android/lib-channel/_latestVersion)

- 简介：

    基于Timer封装的定时器
    
- 使用：

        implementation 'com.bihe0832.android:lib-timer:1.2.5'

![LibAPK](https://img.shields.io/badge/AndroidAppFactory-LibAPK-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-utils-apk/images/download.svg) ](https://bintray.com/bihe0832/android/lib-utils-apk/_latestVersion)

- 简介：

    检查应用安装，获取应用数据，打开APP等发送各类Intent
    
- 使用：

        implementation 'com.bihe0832.android:lib-utils-apk:1.2.3'

![LibTTS](https://img.shields.io/badge/AndroidAppFactory-LibTTS-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-tts/images/download.svg) ](https://bintray.com/bihe0832/android/lib-tts/_latestVersion)

- 简介：

    文字转语音组件的封装

- 使用：

        implementation 'com.bihe0832.android:lib-tts:1.3.4'


![LibFile](https://img.shields.io/badge/AndroidAppFactory-LibFile-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-file/images/download.svg) ](https://bintray.com/bihe0832/android/lib-file/_latestVersion)

- 简介：

    提供Provider的方式访问文件，以及压缩和解压缩

- 使用：

        implementation 'com.bihe0832.android:lib-file:1.2.8'

![LibDownloadAndInstall](https://img.shields.io/badge/AndroidAppFactory-LibDownloadAndInstall-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-download-install/images/download.svg) ](https://bintray.com/bihe0832/android/lib-download-install/_latestVersion)

- 简介：

    简单的下载和唤起应用安装

- 使用：

        implementation 'com.bihe0832.android:lib-download-install:1.2.8'


![LibCommonUIUtils](https://img.shields.io/badge/AndroidAppFactory-LibCommonUIUtils-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-ui-common/images/download.svg) ](https://bintray.com/bihe0832/android/lib-ui-common/_latestVersion)

- 简介：

    资源反射、各种格式转换、获取View的信息、屏幕长宽等信息

- 使用：

        implementation 'com.bihe0832.android:lib-ui-common:1.0.2'
        
    
![LibImage](https://img.shields.io/badge/AndroidAppFactory-LibImage-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-image/images/download.svg) ](https://bintray.com/bihe0832/android/lib-image/_latestVersion)

- 简介：

    对ImageView加载图片的的扩展，支持各种类型的图片加载方式

- 使用：

        implementation 'com.bihe0832.android:lib-image:1.0.0'

![LibDialog](https://img.shields.io/badge/AndroidAppFactory-LibDialog-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-dialog/images/download.svg) ](https://bintray.com/bihe0832/android/lib-dialog/_latestVersion)

- 简介：

    自定义样式的对话框，包括通用的、带进度的、以及全屏非全屏的loading
    
- 使用：

        implementation 'com.bihe0832.android:lib-dialog:1.0.4'


![LibNotification](https://img.shields.io/badge/AndroidAppFactory-LibNotification-brightgreen)[ ![Download](https://api.bintray.com/packages/bihe0832/android/lib-notification/images/download.svg) ](https://bintray.com/bihe0832/android/lib-notification/_latestVersion)

- 简介：

    创建通知栏通知，支持下载带进度的，以及通用的

- 使用：
        
        implementation 'com.bihe0832.android:lib-notification:1.0.9'

