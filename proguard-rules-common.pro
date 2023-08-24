#通用代码混淆文件
-optimizationpasses 7                   # 指定代码的压缩级别
-dontusemixedcaseclassnames             # 混合时不使用大小写混合，混合后的类名为小写
-dontskipnonpubliclibraryclasses        # 是否混淆第三方jar
-dontskipnonpubliclibraryclassmembers   # 指定不去忽略非公共库的类成员
-dontpreverify                          # 混淆时是否做预校验
-dontoptimize
-dontshrink
-verbose                                # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    # 混淆时所采用的算法
-keepattributes SourceFile,LineNumberTable # 抛出异常时保留代码行数


-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn android.support.**
-dontwarn android.telephony.**
-dontwarn android.graphics.**
-dontwarn android.view.**
-dontwarn **ConcurrentHashMap**
-dontwarn **KeySetView

-repackageclasses

-keepattributes Exceptions
-keepattributes Signature                   # 避免混淆泛型
-keepattributes InnerClasses
-keepattributes *Annotation*                # 保留Annotation不混淆
-keep @**annotation** class * {*;}

-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.view.View

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
   public <init>(android.content.Context);
   public <init>(android.content.Context, android.util.AttributeSet);
   public <init>(android.content.Context, android.util.AttributeSet, int);
}

# support包
-keep class android.support.**{ *; }
-keep class android.arch.** { *; }
-keep class android.os.** { *; }

#androidx
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**


-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class * implements java.io.Serializable {
}

-keepclasseswithmembers class * {
    native <methods>;
}

-keep class * extends android.widget.BaseAdapter

-keep,allowobfuscation @interface android.support.annotation.Keep

-keep @android.support.annotation.Keep class *
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}

# 灯塔
-keep class com.tencent.beacon.** {*;}

##Glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.**{*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl


#路由
-keep class com.bihe0832.android.lib.router.stub.* {*;}

# 下拉刷新组件
-keep class com.handmark.pulltorefresh.library.** { *; }

# 微信sdk jar
-keep class com.tencent.mm.**{*;}
-dontwarn com.tencent.mm.**
-keep class com.tencent.wxop.**{*;}
-dontwarn com.tencent.wxop.**
-keep class * extends com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

# opensdk jar
-keep class com.tencent.connect.**{*;}
-keep class com.tencent.map.**{*;}
-keep class com.tencent.open.**{*;}
-keep class com.tencent.qqconnect.**{*;}
-keep class com.tencent.connect.**{*;}
-keep class com.tencent.tauth.**{*;}

#X5内核
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
-keep class com.tencent.smtt.**{*;}
-keep class com.tencent.mtt.**{*;}
-keep class com.tencent.tbs.**{*;}

#retrofit
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class com.squareup.**{*;}
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#gson

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
-dontwarn com.google.gson.**

-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


#webview Jsbridge相关
-keep class com.bihe0832.android.lib.webview.jsbridge.BaseJsBridgeProxy
-keepclassmembernames class com.bihe0832.android.lib.webview.jsbridge.BaseJsBridgeProxy {
    public *;
}

-keepclassmembers class * extends com.bihe0832.android.lib.adapter.CardBaseHolder {
       public <init>(android.content.Context,android.view.View);
}
