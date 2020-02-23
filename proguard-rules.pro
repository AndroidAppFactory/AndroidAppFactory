-optimizationpasses 5                   # 指定代码的压缩级别
-dontusemixedcaseclassnames             # 指定代码的压缩级别
-dontskipnonpubliclibraryclasses        # 是否混淆第三方jar
-dontpreverify                          # 混淆时是否做预校验
-dontoptimize
-ignorewarning                          # 忽略警告，避免打包时某些警告出现
-verbose                                # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    # 混淆时所采用的算法
#-applymapping mapping.txt

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

