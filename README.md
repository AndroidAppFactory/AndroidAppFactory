## 关于 AndroidAppFactory

<img src="https://blog.bihe0832.com/public/img/androidappfactory.png" height="200px" />

开始做酱油的时候发现做开发那么久竟然没有一整套顺手的开发方案，要从头开始一个一个搭建（相当于之前做的就没什么沉淀），因此开始逐渐整理一整套相对全面并且顺手的开发方案。随着这几年逐渐完善，现在基本上具备雏形了，因此逐渐总结一下。

### 相关链接

- 主页：[https://android.bihe0832.com](https://android.bihe0832.com)

- 源码：[https://github.com/bihe0832/AndroidAppFactory](https://github.com/bihe0832/AndroidAppFactory)
	
	目前包含了绝大部分公共的基础组件的源码，上层组件及持续集成等正在逐渐完善中。
	
- 方案介绍：[https://blog.bihe0832.com/android-dev-summary.html](https://blog.bihe0832.com/android-dev-summary.html)

	完整的介绍了整个基于组件化的多APP开发方案。
	
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
		    api 'com.bihe0832.android:lib-wrapper:1.0.3'
		}

关于组件使用更详细的内容点击链接 [https://android.bihe0832.com/#!start.md](https://android.bihe0832.com/#!start.md) 了解


