## 关于 AndroidAppFactory

<img src="https://blog.bihe0832.com/public/img/androidappfactory.png" height="200px" />

开始做酱油的时候发现做开发那么久竟然没有一整套顺手的开发方案，要从头开始一个一个搭建（相当于之前做的就没什么沉淀），因此开始逐渐整理一整套相对全面并且顺手的开发方案。随着这几年逐渐完善，现在基本上具备雏形了，因此逐渐总结一下。

AAF 是基于组件化方案，逐渐积累的一套 Android 终端开发方案。方案力求做到技术方案可以轻松的从一个项目复用到另一个项目，或者可以快速在框架基础上开发出另一个独立的应用，开发过程中基本聚焦在新业务的逻辑，而不是基础功能。

### 相关链接

- 主页：[https://android.bihe0832.com](https://android.bihe0832.com)

- 方案介绍：[https://blog.bihe0832.com/android-dev-summary.html](https://blog.bihe0832.com/android-dev-summary.html)

	完整的介绍了整个基于组件化的多APP开发方案。
	
- AAF 源码：基础组件和公共组件等可直接复用组件的源码。

	- Github：[https://github.com/bihe0832/AndroidAppFactory](https://github.com/bihe0832/AndroidAppFactory)
	
	- 代码统计：[https://android.bihe0832.com/source/lib/index.html](https://android.bihe0832.com/source/lib/index.html)
	
- AAF Sample：所以基于AAF开发的独立应用或者Sample的源码

	- Github：[https://github.com/bihe0832/AndroidAppFactory-Sample](https://github.com/bihe0832/AndroidAppFactory-Sample)

	- 代码统计：[https://android.bihe0832.com/source/sample/index.html](https://android.bihe0832.com/source/sample/index.html)

	- 项目展示：[https://android.bihe0832.com/samples/](https://android.bihe0832.com/samples/)

### 组件使用

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


