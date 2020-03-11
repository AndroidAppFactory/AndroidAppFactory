package com.bihe0832.android.lib.utils.apk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.bihe0832.android.lib.ui.toast.ToastUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;

/**
 * Created by hardyshi on 2017/9/15.
 */

public class APKUtils {

    /**
     * 获取APP版本号
     */
    public static int getAppVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi == null ? 0 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getAppVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi == null ? "" : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAppName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi == null ? "" : pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<PackageInfo> getInstalledPackageList(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        return pm.getInstalledPackages(0);
    }

    public static PackageInfo getInstalledPackage(Context ctx, String pkgName) {
        PackageManager pm = ctx.getPackageManager();
        try {
            return pm.getPackageInfo(pkgName.trim(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final String APK_PACKAGE_NAME_WECHAT = "com.tencent.mm";
    public static final String APK_LAUNCHER_CLASS_WECHAT = "com.tencent.mm.ui.LauncherUI";

    public static final String APK_PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public static final String APK_LAUNCHER_CLASS_QQ = "com.tencent.mobileqq.activity.HomeActivity";

    public static boolean startApp(Context ctx, String appName, String pkgName, String launcerClass) {
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName(pkgName, launcerClass);
        intent.setComponent(cmp);
        PackageManager pm = ctx.getPackageManager();
        intent = pm.getLaunchIntentForPackage(pkgName);
        return startApp(ctx, appName, pkgName,intent);
    }

    public static boolean startApp(Context ctx, String appName, String pkgName) {
        PackageManager pm = ctx.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        return startApp(ctx, appName, pkgName,intent);
    }

    private static boolean startApp(Context ctx, String appName, String pkgName, Intent intent) {
        if (getInstalledPackage(ctx, pkgName) == null) {
            ToastUtil.showShort(ctx, appName + "未安装，请安装后重试");
            return false;
        }
        try {
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showShort(ctx, "拉起" + appName + "失败，请手动尝试");
            return false;
        }
    }

    private static String getUid(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            Log.d("APKUtils","getUid context or packageName is null");
            return "";
        }
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return String.valueOf(ai.uid);
        } catch (Exception e) {
            Log.d("APKUtils","getUid Exception:" + e.getStackTrace());
        }

        return "";
    }


    public static boolean isRunningTask(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            Log.d("APKUtils","getTcpCountOfRunningTask context or packageName is null");
            return false;
        } else {
            BufferedReader bufferReader = (BufferedReader)null;
            try {
                bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/net/tcp"), "UTF-8"));
                String line = null;
                String uid = getUid(context, packageName);
                HashSet ports = new HashSet<String>();
                for(line = bufferReader.readLine(); line != null; line = bufferReader.readLine()) {
                    line = line.trim();
                    String[] targets = line.split("\\s+");
                    if (targets.length > 7) {
                        String cur = targets[7];
                        if (!TextUtils.isEmpty(cur) && Character.isDigit(cur.charAt(0))) {
                            ports.add(cur);
                            if (cur == uid) {
                                // 找到就立即退出
                                return true;
                            }
                        }
                    }
                }
                Log.d("APKUtils","getTcpCountOfRunningTask app: ${packageName}, uid:${uid}, result:${ports.toTypedArray().contentToString()}");
                return ports.contains(uid);
            } catch (Exception e) {
                Log.d("APKUtils","getTcpCountOfRunningTask, execNetStat IP failed." + e.getStackTrace());
            } finally {
                if (bufferReader != null) {
                    try {
                        bufferReader.close();
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
            return false;
        }
    }


}
