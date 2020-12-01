package com.bihe0832.android.lib.permission;

import android.content.Context;
import android.support.v4.content.PermissionChecker;

public class PermissionsChecker {

    private final Context mContext;

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    // 判断权限集合
    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    public boolean lacksPermission(String permission) {
        return PermissionChecker.checkSelfPermission(mContext, permission) != PermissionChecker.PERMISSION_GRANTED;
    }
}
