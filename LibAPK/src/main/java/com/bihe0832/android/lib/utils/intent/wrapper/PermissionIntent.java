package com.bihe0832.android.lib.utils.intent.wrapper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class PermissionIntent {

    /**
     * 跳转到miui的权限管理页面
     */
    public static boolean gotoMiuiPermission(Context context,String packageName) {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", packageName);
            context.startActivity(localIntent);
            return true;
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", packageName);
                context.startActivity(localIntent);
                return true;
            } catch (Exception e1) { // 否则跳转到应用详情
                e1.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    public static boolean gotoMeizuPermission(Context context,String packageName) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName",packageName);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 华为的权限管理页面
     */
    public static boolean gotoHuaweiPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
