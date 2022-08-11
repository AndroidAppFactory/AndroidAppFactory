package com.bihe0832.android.lib.permission;

import android.Manifest;
import android.content.Context;
import android.support.v4.content.PermissionChecker;

import com.bihe0832.android.lib.permission.wrapper.FloatPermissionWrapperKt;

import java.util.List;

public class PermissionsChecker {

    private final Context mContext;

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    // 判断权限集合
    public boolean lacksPermissions(List<String> permissions) {
        if (permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    public boolean lacksPermission(String permission) {
        if (permission.equalsIgnoreCase(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            return !FloatPermissionWrapperKt.checkFloatPermission(mContext);
        } else {
            return PermissionChecker.checkSelfPermission(mContext, permission) != PermissionChecker.PERMISSION_GRANTED;
        }
    }
}
