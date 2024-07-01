package com.bihe0832.android.lib.widget.permission;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.apk.APKUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ShortcutPermissionChecker {

    private static final String TAG = "ShortcutPermissionCheck";

    @ShortcutPermission.PermissionResult
    public static int checkOnEMUI(@NonNull Context context) {
        ZLog.d(TAG, "checkOnEMUI");
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        try {
            Class<?> PermissionManager = Class.forName("com.huawei.hsm.permission.PermissionManager");
            Method canSendBroadcast = PermissionManager.getDeclaredMethod("canSendBroadcast", Context.class,
                    Intent.class);
            Object invoke = canSendBroadcast.invoke(PermissionManager, context, intent);
            if (invoke != null) {
                boolean invokeResult = (boolean) invoke;
                ZLog.d(TAG, "EMUI check permission canSendBroadcast invoke result = " + invokeResult);
                if (invokeResult) {
                    return ShortcutPermission.PERMISSION_GRANTED;
                } else {
                    return ShortcutPermission.PERMISSION_DENIED;
                }
            } else {
                return ShortcutPermission.PERMISSION_UNKNOWN;
            }
        } catch (ClassNotFoundException e) {//Multiple-catch require API level 19
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        } catch (NoSuchMethodException e) {
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        } catch (IllegalAccessException e) {
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        } catch (InvocationTargetException e) {
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        } catch (Exception e) {
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        }
    }

    @ShortcutPermission.PermissionResult
    public static int checkOnVIVO(@NonNull Context context) {
        ZLog.d(TAG, "checkOnVIVO");
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            ZLog.d(TAG, "contentResolver is null");
            return ShortcutPermission.PERMISSION_UNKNOWN;
        }
        Cursor query = null;
        try {
            Uri parse = Uri.parse("content://com.bbk.launcher2.settings/favorites");
            query = contentResolver.query(parse, null, null, null, null);
            if (query == null) {
                ZLog.d(TAG, "cursor is null (Uri : content://com.bbk.launcher2.settings/favorites)");
                return ShortcutPermission.PERMISSION_UNKNOWN;
            }

            while (query.moveToNext()) {
                String titleByQueryLauncher = query.getString(query.getColumnIndexOrThrow("title"));
                ZLog.d(TAG, "title by query is " + titleByQueryLauncher);
                if (!TextUtils.isEmpty(titleByQueryLauncher) && titleByQueryLauncher.equals(APKUtils.getAppName(context))) {
                    int value = query.getInt(query.getColumnIndexOrThrow("shortcutPermission"));
                    ZLog.d(TAG, "permission value is " + value);
                    if (value == 1 || value == 17) {
                        return ShortcutPermission.PERMISSION_DENIED;
                    } else if (value == 16) {
                        return ShortcutPermission.PERMISSION_GRANTED;
                    } else if (value == 18) {
                        return ShortcutPermission.PERMISSION_ASK;
                    }
                }
            }
        } catch (Exception e) {
            ZLog.d(TAG, e.getMessage());
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return ShortcutPermission.PERMISSION_UNKNOWN;
    }

    @ShortcutPermission.PermissionResult
    public static int checkOnMIUI(@NonNull Context context) {
        ZLog.d(TAG, "checkOnMIUI");
        if (BuildUtils.INSTANCE.getSDK_INT() < Build.VERSION_CODES.KITKAT) {
            return ShortcutPermission.PERMISSION_UNKNOWN;
        }
        try {
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            String pkg = context.getApplicationContext().getPackageName();
            int uid = context.getApplicationInfo().uid;
            Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getDeclaredMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE,
                    String.class);
            Object invoke = checkOpNoThrowMethod.invoke(mAppOps, 10017, uid, pkg);//the ops of INSTALL_SHORTCUT is 10017
            if (invoke == null) {
                ZLog.d(TAG, "MIUI check permission checkOpNoThrowMethod(AppOpsManager) invoke result is null");
                return ShortcutPermission.PERMISSION_UNKNOWN;
            }
            String result = invoke.toString();
            ZLog.d(TAG, "MIUI check permission checkOpNoThrowMethod(AppOpsManager) invoke result = " + result);
            switch (result) {
                case "0":
                    return ShortcutPermission.PERMISSION_GRANTED;
                case "1":
                    return ShortcutPermission.PERMISSION_DENIED;
                case "5":
                    return ShortcutPermission.PERMISSION_ASK;
            }
        } catch (Exception e) {
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        }
        return ShortcutPermission.PERMISSION_UNKNOWN;
    }

    @ShortcutPermission.PermissionResult
    public static int checkOnOPPO(@NonNull Context context) {
        ZLog.d(TAG, "checkOnOPPO");
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            ZLog.d(TAG, "contentResolver is null");
            return ShortcutPermission.PERMISSION_UNKNOWN;
        }
        Uri parse = Uri.parse("content://settings/secure/launcher_shortcut_permission_settings");
        Cursor query = contentResolver.query(parse, null, null, null, null);
        if (query == null) {
            ZLog.d(TAG, "cursor is null (Uri : content://settings/secure/launcher_shortcut_permission_settings)");
            return ShortcutPermission.PERMISSION_UNKNOWN;
        }
        try {
            String pkg = context.getApplicationContext().getPackageName();
            while (query.moveToNext()) {
                String value = query.getString(query.getColumnIndex("value"));
                ZLog.d(TAG, "permission value is " + value);
                if (!TextUtils.isEmpty(value)) {
                    if (value.contains(pkg + ", 1")) {
                        return ShortcutPermission.PERMISSION_GRANTED;
                    }
                    if (value.contains(pkg + ", 0")) {
                        return ShortcutPermission.PERMISSION_DENIED;
                    }
                }
            }
            return ShortcutPermission.PERMISSION_UNKNOWN;
        } catch (Exception e) {
            ZLog.d(TAG, e.getMessage());
            return ShortcutPermission.PERMISSION_UNKNOWN;
        } finally {
            query.close();
        }
    }
}
