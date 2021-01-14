<img src="https://blog.bihe0832.com/public/img/androidappfactory.png" height="100px" />

## 关于 AndroidAppFactory

做酱油的时候发现做开发那么久竟然没有一整套顺手的开发方案，要从头开始一个一个搭建（相当于之前做的就没什么沉淀），因此开始逐渐整理一整套相对全面并且顺手的开发方案。随着这几年逐渐完善，现在基本上具备雏形了，因此逐渐总结一下。

AAF 是基于组件化方案，逐渐积累的一套 Android 终端开发方案。方案力求做到**技术方案可以轻松的从一个项目复用到另一个项目，或者可以快速在框架基础上开发出一个独立全新的应用**，开发过程中基本聚焦在新业务的逻辑，而不是基础功能。

## 相关链接

### AAF框架相关

- 方案介绍：[https://blog.bihe0832.com/android-dev-summary.html](https://blog.bihe0832.com/android-dev-summary.html)

	完整AAF的整个技术方案，以及其中一些核心的技术点
	
- **框架主页：[https://android.bihe0832.com](https://android.bihe0832.com)**

    主要**介绍框架的使用以及怎么新增，升级组件**

- 框架源码：[https://github.com/bihe0832/AndroidAppFactory](https://github.com/bihe0832/AndroidAppFactory)
	
    基础组件和公共组件等可直接复用组件的源码。
	
- 框架代码统计：[https://android.bihe0832.com/source/lib/index.html](https://android.bihe0832.com/source/lib/index.html)

### AAF框架具体实践

- **AAF Sample：[https://android.bihe0832.com/samples/](https://android.bihe0832.com/samples/)**

    **所有基于AAF开发的应用的集中展示。**

- AAF Sample 源码：[https://github.com/bihe0832/AndroidAppFactory-Sample](https://github.com/bihe0832/AndroidAppFactory-Sample)

	具体实践以开源源码

- AAF Sample 代码统计：[https://android.bihe0832.com/source/sample/index.html](https://android.bihe0832.com/source/sample/index.html)


## AAF 使用

-  添加依赖
	
	在根目录添加发布插件的相关依赖
	
	    buildscript {  
	        repositories {  
	            jcenter()  
	        }  
	    }   
	
	    allprojects {  
	        repositories {  
	            jcenter()  
	        }  
	    }
    
-  import

	直接在项目依赖中添加对应库的依赖：
	
		dependencies {
		    api 'com.bihe0832.android:lib-wrapper:+'
		}

关于组件使用更详细的内容点击链接 [https://android.bihe0832.com/#!start.md](https://android.bihe0832.com/#!start.md) 了解


