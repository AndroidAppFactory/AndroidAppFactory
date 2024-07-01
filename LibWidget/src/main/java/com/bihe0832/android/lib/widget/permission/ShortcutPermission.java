package com.bihe0832.android.lib.widget.permission;

import static com.bihe0832.android.lib.widget.permission.ShortcutPermissionChecker.checkOnEMUI;
import static com.bihe0832.android.lib.widget.permission.ShortcutPermissionChecker.checkOnMIUI;
import static com.bihe0832.android.lib.widget.permission.ShortcutPermissionChecker.checkOnOPPO;
import static com.bihe0832.android.lib.widget.permission.ShortcutPermissionChecker.checkOnVIVO;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import androidx.annotation.IntDef;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.utils.os.ManufacturerUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;


public class ShortcutPermission {

    public static final int PERMISSION_GRANTED = 0;

    public static final int PERMISSION_DENIED = -1;
    public static final int PERMISSION_ASK = 1;
    public static final int PERMISSION_UNKNOWN = 2;
    private static final String TAG = "ShortcutPermission";

    public static boolean hasPermission(Context context) {
        return checkPermissionResult(context) == PERMISSION_GRANTED;
    }

    @PermissionResult
    public static int checkPermissionResult(Context context) {
        ZLog.d(TAG, "manufacturer = " + ManufacturerUtil.INSTANCE.getMANUFACTURER() + ", api level= "
                + Build.VERSION.SDK_INT);
        int result;
        if (ManufacturerUtil.INSTANCE.isHuawei()) {
            result = checkOnEMUI(context);
        } else if (ManufacturerUtil.INSTANCE.isMiRom()) {
            result = checkOnMIUI(context);
        } else if (ManufacturerUtil.INSTANCE.isOppo()) {
            result = checkOnOPPO(context);
        } else if (ManufacturerUtil.INSTANCE.isVivo()) {
            result = checkOnVIVO(context);
        } else {
            if (PermissionManager.INSTANCE.isAllPermissionOK(context,
                    Arrays.asList(Manifest.permission.INSTALL_SHORTCUT))) {
                return PERMISSION_GRANTED;
            } else {
                return PERMISSION_UNKNOWN;
            }
        }
        return result;
    }

    @IntDef(value = {PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_ASK, PERMISSION_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionResult {

    }

}
