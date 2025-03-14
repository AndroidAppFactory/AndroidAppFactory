package com.bihe0832.android.lib.utils.intent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.intent.wrapper.PermissionIntent;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.ManufacturerUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;


public class IntentUtils {

    private static final String TAG = "IntentUtils";

    public static boolean jumpToOtherApp(Context context, String url) {
        if (context == null) {
            return false;
        }
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            intent.setSelector(null);
            ZLog.d(TAG, "jumpToOtherApp url:" + url + ",intent:" + intent.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            ZLog.e(TAG, "jumpToOtherApp failed:" + e.getMessage());
            return false;
        }
    }

    public static boolean openWebPage(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            ZLog.d(TAG, "openWebPage url:" + url + ",intent:" + intent.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            ZLog.e(TAG, "openWebPage failed:" + e.getMessage());
            return false;
        }
    }

    public static boolean goHomePage(Context context) {
        if (context == null) {
            return false;
        }
        try {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            context.startActivity(home);
            return true;
        } catch (Exception e) {
            ZLog.e(TAG, "goHomePage failed:" + e.getMessage());
            return false;
        }
    }


    public static boolean startIntent(Context ctx, Intent intent) {
        if (null == intent) {
            ZLog.d("startIntent intent == null");
            return false;
        }

        if (null == ctx) {
            ZLog.d("startIntent ctx == null");
            return false;
        }
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 启动应用的设置
    public static boolean startAppDetailSettings(Context ctx) {
        return startAppDetailSettings(ctx, ctx.getPackageName());
    }

    public static boolean startAppDetailSettings(Context ctx, String packageName) {
        boolean result = false;
        if (BuildUtils.INSTANCE.getSDK_INT() <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            result = startAppSettings(ctx, packageName, Settings.ACTION_SETTINGS, false);
        } else {
            result = startAppSettings(ctx, packageName, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, false);
        }

        if (!result) {
            if (ManufacturerUtil.INSTANCE.isXiaomi()) {
                result = PermissionIntent.gotoMiuiPermission(ctx, packageName);
            } else if (ManufacturerUtil.INSTANCE.isMeizu()) {
                result = PermissionIntent.gotoMeizuPermission(ctx, packageName);
            } else if (ManufacturerUtil.INSTANCE.isHuawei()) {
                result = PermissionIntent.gotoHuaweiPermission(ctx);
            }
        }
        return result;
    }

    // 启动应用的设置
    public static boolean startAppSettings(Context ctx, String data) {
        return startAppSettings(ctx, data, true);
    }

    public static boolean startAppSettings(Context ctx, String data, boolean showDetail) {
        return startAppSettings(ctx, ctx.getPackageName(), data, showDetail);
    }

    // 启动应用的设置
    public static boolean startAppSettings(Context ctx, String packageName, String data, boolean showDetail) {
        if (null == ctx) {
            ZLog.d("startAppSettings ctx == null");
            return false;
        }

        if (TextUtils.isEmpty(data)) {
            ZLog.d("startAppSettings data == null");
            if (showDetail) {
                return startAppDetailSettings(ctx, packageName);
            } else {
                return false;
            }
        }

        Intent intent = new Intent(data);
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0以上
            intent.putExtra("app_package", packageName);
            try {
                ApplicationInfo applicationInfo = ctx.getPackageManager()
                        .getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if (null != applicationInfo) {
                    intent.putExtra("app_uid", applicationInfo.uid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O) {
            // 8.0 以上
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
        }

        Intent intent2 = (Intent) intent.clone();
        intent2.setData(Uri.fromParts("package", packageName, null));
        boolean result;
        if (intent2.resolveActivity(ctx.getPackageManager()) != null) {
            result = startIntent(ctx, intent2);
        } else {
            result = startIntent(ctx, intent);
        }
        if (!result) {
            if (showDetail) {
                return startAppDetailSettings(ctx, packageName);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    // 启动手机的设置
    public static boolean startSettings(Context ctx, String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        Intent intent = new Intent();
        intent.setAction(data);
        return startIntent(ctx, intent);
    }

    public static void sendTextInfo(final Context context, final String title, final String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        sendIntent.setType("text/plain");
        try {
            context.startActivity(Intent.createChooser(sendIntent, title));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                context.startActivity(sendIntent);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    public static boolean sendMail(final Context context, final String mail, final String title, final String content) {
        Uri uri = Uri.parse("mailto:" + mail);
        List<ResolveInfo> packageInfos = context.getPackageManager()
                .queryIntentActivities(new Intent(Intent.ACTION_SENDTO, uri), 0);
        List<String> tempPkgNameList = new ArrayList<>();
        List<Intent> emailIntents = new ArrayList<>();
        for (ResolveInfo info : packageInfos) {
            String pkgName = info.activityInfo.packageName;
            if (!tempPkgNameList.contains(pkgName)) {
                tempPkgNameList.add(pkgName);
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
                emailIntents.add(intent);
            }
        }
        if (!emailIntents.isEmpty()) {
            String[] email = {mail};
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra(Intent.EXTRA_CC, email);
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, content);

            try {
                context.startActivity(Intent.createChooser(intent, title));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    context.startActivity(intent);
                    return true;
                } catch (Exception ee) {
                    ee.printStackTrace();
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    // 启动手机的设置
    public static String convertIntentToJson(Intent intent) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (null != intent && !intent.getExtras().isEmpty()) {
                Bundle b = intent.getExtras();
                Set<String> keys = b.keySet();
                for (String key : keys) {
                    try {
                        jsonObject.put(key, b.get(key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    public static final void restartAPP(final Context context) {
        Intent intent = new Intent(context, getLaunchActivityName(context, context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int mPendingIntentId = (int) (System.currentTimeMillis() / 1000);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static Class getLaunchActivityName(Context context, String packageName) {
        PackageManager localPackageManager = context.getPackageManager();
        Intent localIntent = new Intent(Intent.ACTION_MAIN);
        localIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo localResolveInfo : localPackageManager.queryIntentActivities(localIntent, 0)) {
            if (!localResolveInfo.activityInfo.applicationInfo.packageName.equalsIgnoreCase(packageName)) {
                continue;
            }
            return localResolveInfo.activityInfo.getClass();
        }
        return null;
    }
}
