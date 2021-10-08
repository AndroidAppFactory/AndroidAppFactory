package com.bihe0832.android.lib.immersion;

import android.os.Build;
import android.text.TextUtils;
import com.bihe0832.android.lib.device.ManufacturerUtil;
import com.bihe0832.android.lib.utils.ConvertUtils;

class RomType {

    public static final int MIUI = 1;
    public static final int FLYME = 2;
    public static final int ANDROID_NATIVE = 3;
    public static final int NA = 4;

    private static Integer romType;

    public static int getRomType() {
        if (romType != null) {
            return romType;
        }

        if (isMIUIV6OrAbove()) {
            romType = MIUI;
            return romType;
        }

        if (isFlymeV4OrAbove()) {
            romType = FLYME;
            return romType;
        }

        if (isAndroid5OrAbove()) {
            romType = ANDROID_NATIVE;
            return romType;
        }

        romType = NA;
        return romType;
    }

    private static boolean isFlymeV4OrAbove() {
        return getFlymeVersion() >= 4;
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    protected static int getFlymeVersion() {
        String displayId = Build.DISPLAY;
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            displayId = displayId.replaceAll("Flyme", "");
            displayId = displayId.replaceAll("OS", "");
            displayId = displayId.replaceAll(" ", "");
            String version = displayId.substring(0, 1);
            if (version != null) {
                return ConvertUtils.parseInt(version);
            }
        }
        return -1;
    }

    //MIUI V6对应的versionCode是4
    //MIUI V7对应的versionCode是5
    private static boolean isMIUIV6OrAbove() {
        return ManufacturerUtil.INSTANCE.getMiuiVersionCode() >= 4;
    }

    //Android Api 23以上
    private static boolean isAndroid5OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
