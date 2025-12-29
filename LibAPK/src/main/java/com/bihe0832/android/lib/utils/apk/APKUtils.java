package com.bihe0832.android.lib.utils.apk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;

import com.bihe0832.android.lib.aaf.tools.AAFException;
import com.bihe0832.android.lib.aaf.res.R;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.encrypt.HexUtils;
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MD5;
import com.bihe0832.android.lib.utils.encrypt.messagedigest.MessageDigestUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zixie on 2017/9/15.
 */

public class APKUtils {

    private static final String TAG = "APKUtils";

    /**
     * 检查并记录包名查询失败的原因
     * Android 11+ 需要 QUERY_ALL_PACKAGES 权限或 queries 声明
     */
    private static void logPackageQueryError(Context context, String packageName) {
        if (context == null) return;
        
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.R) {
            ZLog.d(TAG, "Cannot query package [" + packageName + "], may need QUERY_ALL_PACKAGES permission or <queries> declaration on Android 11+");
        }
    }

    /**
     * 获取APP版本号
     */
    public static long getAppVersionCode(Context context) {
        try {
            return getAppVersionCode(context, context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取APP版本号
     */
    public static long getAppVersionCode(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.O_MR1) {
                return pi == null ? 0 : pi.getLongVersionCode();
            } else {
                return pi == null ? 0 : pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            logPackageQueryError(context, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getAppVersionName(Context context) {
        try {
            return getAppVersionName(context, context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAppVersionName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? "" : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            logPackageQueryError(context, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param oldVersion
     * @param newVersion
     * @return 版本一致 0， oldVersion 更高 1， newVersion 更高 2，无法比较 -1
     * @throws AAFException
     */
    public static int compareVersion(String oldVersion, String newVersion) throws AAFException {
        ZLog.d("testVerion", "oldVersion:" + oldVersion + "； newVersion:" + newVersion);
        if (TextUtils.isEmpty(oldVersion) || TextUtils.isEmpty(newVersion)) {
            return -1;
        }
        if (oldVersion.equals(newVersion)) {
            return 0; //版本相同
        }
        String[] oldVersionArray = oldVersion.split("\\.");
        String[] newVersionArray = newVersion.split("\\.");
        int oldVersionLen = oldVersionArray.length;
        int newVersionLen = newVersionArray.length;
        int baseLen = 0;
        if (oldVersionLen > newVersionLen) {
            baseLen = newVersionLen;
        } else {
            baseLen = oldVersionLen;
        }
        //基础版本号比较
        for (int i = 0; i < baseLen; i++) {
            //同位版本号比较
            if (!oldVersionArray[i].equals(newVersionArray[i])) {
                return ConvertUtils.parseInt(oldVersionArray[i]) > ConvertUtils.parseInt(newVersionArray[i]) ? 1 : 2;
            }
        }
        //基础版本相同，再比较子版本号
        if (oldVersionLen != newVersionLen) {
            return oldVersionLen > newVersionLen ? 1 : 2;
        } else {
            //基础版本相同，无子版本号
            return 0;
        }
    }


    public static String getAppName(Context context) {
        try {
            return getAppName(context, context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAppName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? "" : pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            logPackageQueryError(context, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取已安装应用列表
     * 注意：Android 11+ 需要 QUERY_ALL_PACKAGES 权限才能获取完整列表，否则只返回部分可见应用
     */
    public static List<PackageInfo> getInstalledPackageList(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            List<PackageInfo> list = pm.getInstalledPackages(0);
            if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.R && list != null && list.size() < 10) {
                ZLog.w("APKUtils", "getInstalledPackageList: only got " + list.size() + " apps, may need QUERY_ALL_PACKAGES permission on Android 11+");
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否能查询指定包名的应用信息
     * 兼容 QUERY_ALL_PACKAGES 权限和 queries 声明两种方式
     * Android 11+ 需要声明权限或 queries 才能查询其他应用
     *
     * @param context     上下文
     * @param packageName 要查询的包名
     * @return 是否能查询该包名
     */
    public static boolean canQueryPackage(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }

        // Android 11 以下不需要特殊权限
        if (BuildUtils.INSTANCE.getSDK_INT() < Build.VERSION_CODES.R) {
            return true;
        }

        // Android 11+：尝试查询目标包名
        // 如果有 QUERY_ALL_PACKAGES 权限或在 <queries> 中声明了该包名，查询会成功
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            // 能查到说明有权限（已安装的情况）
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // 查不到可能是：1. 没权限 2. 确实没安装
            // 再尝试查询自己的包名，如果自己都查不到说明 PM 有问题
            try {
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                // 能查到自己，说明 PM 正常，目标包可能是没安装或没权限
                // 这种情况下返回 true，让调用方继续轮询等待安装
                return true;
            } catch (Exception ex) {
                // 连自己都查不到，说明有严重问题
                ZLog.e("APKUtils", "canQueryPackage: cannot query self package, PM may have issues");
                return false;
            }
        } catch (Exception e) {
            ZLog.w("APKUtils", "canQueryPackage: query failed for " + packageName + ", error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取 APK 文件的包名
     *
     * @param context  上下文
     * @param filePath APK 文件路径
     * @return 包名，解析失败返回 null
     */
    public static String getApkPackageName(Context context, String filePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, 0);
            if (packageInfo != null) {
                return packageInfo.packageName;
            }
        } catch (Exception e) {
            ZLog.e("Failed to get APK package name: " + e.getMessage());
        }
        return "";
    }

    public static PackageInfo getInstalledPackage(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            return pm.getPackageInfo(pkgName.trim(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            logPackageQueryError(ctx, pkgName);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean startApp(Context ctx, String pkgName) {
        return startApp(ctx, pkgName, true);
    }

    public static boolean startApp(Context ctx, String appName, String pkgName, String launcerClass) {
        return startApp(ctx, appName, pkgName, launcerClass, true);
    }

    public static boolean startApp(Context ctx, String pkgName, boolean showTips) {
        try {
            PackageManager pm = ctx.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(pkgName);
            return startApp(ctx, getAppName(ctx, pkgName), pkgName, intent, showTips);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startApp(Context ctx, String appName, String pkgName, String launcerClass, boolean showTips) {
        try {
            Intent intent = new Intent();
            ComponentName cmp = new ComponentName(pkgName, launcerClass);
            intent.setComponent(cmp);
            return startApp(ctx, appName, pkgName, intent, showTips);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean startApp(Context ctx, String appName, String pkgName) {
        return startApp(ctx, appName, pkgName, true);
    }

    public static boolean startApp(Context ctx, String appName, String pkgName, boolean showTips) {
        try {
            PackageManager pm = ctx.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(pkgName);
            return startApp(ctx, appName, pkgName, intent, showTips);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean startApp(Context ctx, String appName, String pkgName, Intent intent, boolean showTips) {
        try {
            if (getInstalledPackage(ctx, pkgName) == null) {
                if (showTips) {
                    ToastUtil.showShort(ctx, ctx.getString(R.string.apk_not_install));
                }
                return false;
            }
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (showTips) {
                ToastUtil.showShort(ctx, String.format(ctx.getString(R.string.apk_launcher_failed), appName));
            }
            return false;
        }
    }

    private static String getUid(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            ZLog.d("APKUtils", "getUid context or packageName is null");
            return "";
        }
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return String.valueOf(ai.uid);
        } catch (Exception e) {
            ZLog.d("APKUtils", "getUid Exception:" + e.getStackTrace());
        }

        return "";
    }

    public static boolean isRunningTask(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            ZLog.d("APKUtils", "getTcpCountOfRunningTask context or packageName is null");
            return false;
        } else {
            BufferedReader bufferReader = (BufferedReader) null;
            try {
                bufferReader = new BufferedReader(
                        new InputStreamReader(new FileInputStream("/proc/net/tcp"), "UTF-8"));
                String line = null;
                String uid = getUid(context, packageName);
                HashSet ports = new HashSet<String>();
                for (line = bufferReader.readLine(); line != null; line = bufferReader.readLine()) {
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
                ZLog.d("APKUtils",
                        "getTcpCountOfRunningTask app: ${packageName}, uid:${uid}, result:${ports.toTypedArray().contentToString()}");
                return ports.contains(uid);
            } catch (Exception e) {
                ZLog.d("APKUtils", "getTcpCountOfRunningTask, execNetStat IP failed." + e.getStackTrace());
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

    public static String getAPKPath(Context context, String pkgName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkgName, PackageManager.GET_SIGNATURES);
            return info.sourceDir;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSigMd5ByPkgName(Context context, String pkgName) {
        return getSigMd5ByPkgName(context, pkgName, false);
    }

    public static String getSigMd5ByPkgName(Context context, String pkgName, boolean showTips) {
        return getSigMessageDigestByPkgName(context, MD5.MESSAGE_DIGEST_TYPE_MD5, pkgName, showTips);
    }

    public static String getSigMessageDigestByPkgName(Context context, String messageDigestType, String pkgName,
                                                      boolean showTips) {
        if (null != pkgName && pkgName.length() > 0) {
            try {
                Signature sig = context.getPackageManager()
                        .getPackageInfo(pkgName, PackageManager.GET_SIGNATURES).signatures[0];
                String result = MessageDigestUtils.getDigestData(sig.toByteArray(), messageDigestType);
                if (null != result && result.length() > 0) {
                    return result;
                } else {
                    if (showTips) {
                        ToastUtil.showShort(context, context.getString(R.string.apk_sig_not_found));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                logPackageQueryError(context, pkgName);
                if (showTips) {
                    ToastUtil.showShort(context, context.getString(R.string.apk_sig_bad_package));
                }
            }
        } else {
            if (showTips) {
                ToastUtil.showShort(context, context.getString(R.string.apk_sig_package_empty));
            }
        }
        return "";
    }

    public static String getSigFingerprint(Context context, String packageName) {
        String hexString = "";
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signatures = packageInfo.signatures;
            byte[] cert = signatures[0].toByteArray();
            InputStream input = new ByteArrayInputStream(cert);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate c = (X509Certificate) cf.generateCertificate(input);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            hexString = HexUtils.bytes2HexStr(publicKey);
        } catch (Exception e) {
            // ignore
        }
        return hexString;
    }


    public static RSAPublicKey getSigPublicKey(Context context, String pkgName) {
        if (pkgName != null && pkgName.length() > 0) {
            try {
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
                Signature[] signatures = packageInfo.signatures;
                for (Signature signature : signatures) {
                    try {
                        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                        ByteArrayInputStream certInputStream = new ByteArrayInputStream(signature.toByteArray());
                        Certificate x509Certificate = certFactory.generateCertificate(certInputStream);
                        RSAPublicKey publicKey = (RSAPublicKey) x509Certificate.getPublicKey();
                        try {
                            certInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (null != publicKey && null != publicKey.getEncoded() && publicKey.getEncoded().length > 0) {
                            return publicKey;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
