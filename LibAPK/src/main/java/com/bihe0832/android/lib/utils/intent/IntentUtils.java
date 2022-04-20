package com.bihe0832.android.lib.utils.intent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.intent.wrapper.PermissionIntent;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.ManufacturerUtil;

import org.json.JSONObject;

import java.util.Set;


public class IntentUtils {

    private static final String TAG = "IntentUtils";

    public static boolean jumpToOtherApp(String url, Context context) {
        if (context == null) {
            return false;
        }
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
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

    public static boolean openWebPage(String url, Context context) {
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
        boolean result = false;
        if (ManufacturerUtil.INSTANCE.isXiaomi()) {
            result = PermissionIntent.gotoMiuiPermission(ctx);
        } else if (ManufacturerUtil.INSTANCE.isMeizu()) {
            result = PermissionIntent.gotoMeizuPermission(ctx);
        } else if (ManufacturerUtil.INSTANCE.isHuawei()) {
            result = PermissionIntent.gotoHuaweiPermission(ctx);
        }
        if (result) {
            return true;
        } else {
            if (BuildUtils.INSTANCE.getSDK_INT() <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return startAppSettings(ctx, Settings.ACTION_SETTINGS, false);
            } else {
                return startAppSettings(ctx, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, false);
            }
        }
    }

    // 启动应用的设置
    public static boolean startAppSettings(Context ctx, String data) {
        return startAppSettings(ctx, data, true);
    }

    // 启动应用的设置
    public static boolean startAppSettings(Context ctx, String data, boolean showDetail) {
        if (null == ctx) {
            ZLog.d("startAppSettings ctx == null");
            return false;
        }

        if (TextUtils.isEmpty(data)) {
            ZLog.d("startAppSettings data == null");
            if (showDetail) {
                return startAppDetailSettings(ctx);
            } else {
                return false;
            }
        }

        Intent intent = new Intent(data);
        if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.O) {
            intent.putExtra("android.provider.extra.APP_PACKAGE", ctx.getPackageName());
        } else if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0以上到8.0以下
            intent.putExtra("app_package", ctx.getPackageName());
            if (null != ctx.getApplicationInfo()) {
                intent.putExtra("app_uid", ctx.getApplicationInfo().uid);
            }
        }
        intent.setData(Uri.fromParts("package", ctx.getPackageName(), null));
        if (!startIntent(ctx, intent)) {
            if (showDetail) {
                return startAppDetailSettings(ctx);
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


    // 启动手机的设置
    public static String convertIntentToJson(Intent intent) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (null != intent && !intent.getExtras().isEmpty()){
                Bundle b = intent.getExtras();
                Set<String> keys = b.keySet();
                for (String key : keys) {
                    try {
                        jsonObject.put(key, b.get(key));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


}
