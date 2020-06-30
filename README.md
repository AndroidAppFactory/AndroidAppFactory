![AndroidAppFactory](https://blog.bihe0832.com/public/img/androidappfactory.png )

## 相关链接

- 主页：[https://android.bihe0832.com](https://android.bihe0832.com)

- 源码：[https://github.com/bihe0832/AndroidAppFactory](https://github.com/bihe0832/AndroidAppFactory)

## 背景

在任何终端项目的开发中，我们都绕不开几个问题：

- **应用内的业务关系非常复杂**。如果前期没有做好解耦，当随着业务增加，项目成员越来越多的时候，项目开发维护的成本会非常高。这样复杂的关系下无论是新功能开发还是老版本调整都会非常痛苦

- **当项目发展到一定阶段，逐渐庞大以后，开发过程中编译、构建等都会耗费大量的开发时间**。即使可以使用instant run等工具优化编译的时间，但都是治标不治本，开发的效率依旧很低，最好的方式还是尽可能精简代码

- 虽然不同项目间可以复用的代码很多，但是目前**代码复用的方式还停留在代码文件或者代码片段的复用**，每次都是搜索复制粘贴，效率很低，而且随着时间推移，同一文件会存在多个版本，同一个bug可能也会每个地方遇到一次。


在终端开发的框架演进的过程中，也一直通过各种方法来优化解决上面的问题。

- 通过模块化来降低业务代码和基础框架、降低业务逻辑之间的代码耦合

- 通过组件化将公共逻辑，基础功能抽离为组件，组件和业务分别独立维护，减小业务规模。

为了方便自己的开发，也为了技术积累和沉淀，开始逐渐结合以前的项目经历整理并搭建一套基础的开发库。


## 设计原则

AndroidAppFactory 主要是对终端开发中不断积累的一些基础的工具或者开发库的整理，通过在这样的方式，一方面提高代码质量，另一方面提高开发效率。整理的最终目的是：

- 相同的坑不重复遇到

- 相同功能的多个方案不重复研究

- 相同功能的代码不存在两份，保证一致

- 降低后续新项目的开发成本，做到尽可能高的代码复用

为了后续的规划，目前对于功能模块拆分基本上按照下面的几个原则

- 模块拆分尽可能原子化，一个模块聚焦一个功能

- 同一功能对外接口暴露尽可能少

- 同一功能覆盖尽可能多的场景，且兼容性尽可能好

## 使用方式

**详细的使用方式可以前往主页查看，目前正在陆续整理扩充。这里仅提供一个引用方式。**

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

