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
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.bihe0832.android.lib.utils.encrypt.HexUtils;
import com.bihe0832.android.lib.utils.encrypt.MD5;
import com.bihe0832.android.lib.utils.encrypt.MessageDigestUtils;
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

    /**
     * 获取APP版本号
     */
    public static long getAppVersionCode(Context context) {
        return getAppVersionCode(context, context.getPackageName());
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
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getAppVersionName(Context context) {
        return getAppVersionName(context, context.getPackageName());
    }

    public static String getAppVersionName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? "" : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int compareVersion(String oldVersion, String newVersion) throws AAFException {
        ZLog.d("testVerion", "oldVersion:" + oldVersion + "； oldVersion:" + newVersion);
        if (TextUtils.isEmpty(oldVersion) || TextUtils.isEmpty(newVersion)) {
            return -1;
        }
        if (oldVersion.equals(newVersion)) {
            return 0; //版本相同
        }
        String[] v1Array = oldVersion.split("\\.");
        String[] v2Array = newVersion.split("\\.");
        int v1Len = v1Array.length;
        int v2Len = v2Array.length;
        int baseLen = 0;
        if (v1Len > v2Len) {
            baseLen = v2Len;
        } else {
            baseLen = v1Len;
        }
        //基础版本号比较
        for (int i = 0; i < baseLen; i++) {
            //同位版本号相同
            if (v1Array[i].equals(v2Array[i])) {
                //比较下一位
                continue;
            } else {
                return ConvertUtils.parseInt(v1Array[i]) > ConvertUtils.parseInt(v2Array[i]) ? 1 : -1;
            }
        }
        //基础版本相同，再比较子版本号
        if (v1Len != v2Len) {
            return v1Len > v2Len ? 1 : 2;
        } else {
            //基础版本相同，无子版本号
            return 0;
        }
    }


    public static String getAppName(Context context) {
        return getAppName(context, context.getPackageName());
    }

    public static String getAppName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? "" : pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<PackageInfo> getInstalledPackageList(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            return pm.getInstalledPackages(0);
        } catch (Exception e) {
            return null;
        }
    }

    public static PackageInfo getInstalledPackage(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            return pm.getPackageInfo(pkgName.trim(), 0);
        } catch (PackageManager.NameNotFoundException e) {
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

    public static boolean startApp(Context ctx, String appName, String pkgName, String launcerClass,
                                   boolean showTips) {
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
                    ToastUtil.showShort(ctx, "应用未安装，请安装后重试");
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
                ToastUtil.showShort(ctx, "拉起" + appName + "失败，请手动尝试");
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

    public static String getSigMessageDigestByPkgName(Context context, String messageDigestType, String pkgName, boolean showTips) {
        if (null != pkgName && pkgName.length() > 0) {
            try {
                Signature sig = context.getPackageManager()
                        .getPackageInfo(pkgName, PackageManager.GET_SIGNATURES).signatures[0];
                String result = MessageDigestUtils.getDigestData(sig.toByteArray(), messageDigestType);
                if (null != result && result.length() > 0) {
                    return result;
                } else {
                    if (showTips) {
                        ToastUtil.showShort(context, "读取失败，请重试或者检查应用是否有签名！");
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                if (showTips) {
                    ToastUtil.showShort(context, "应用未安装，请检查输入的包名是否正确！");
                }
            }
        } else {
            if (showTips) {
                ToastUtil.showShort(context, "请先输入需要查询签名应用的包名！");
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
