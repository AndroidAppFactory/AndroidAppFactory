package com.bihe0832.android.lib.device;

import android.os.Build;
import android.os.SystemProperties;

import com.bihe0832.android.lib.text.TextFactoryUtils;


/**
 * Created by hardyshi on 2017/10/31.
 */

public class ManufacturerUtil {
    public static boolean isHuawei() {
        String manufacturer = SystemProperties.get("ro.product.manufacturer", null);
        if (TextFactoryUtils.trimSpace(manufacturer.toLowerCase()).contains("huawei")) {
            return true;
        }

        String fingerprint = SystemProperties.get("ro.build.fingerprint", null);
        if (TextFactoryUtils.trimSpace(fingerprint.toLowerCase()).contains("huawei")) {
            return true;
        }
        return false;
    }

    public static boolean isXiaomi() {
        String manufacturerModel = Build.MANUFACTURER + "-" + Build.MODEL;
        return TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("xiaomi")
                || TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("redmi");
    }

    public static boolean isOppo() {
        try {
            String manufacturer = Build.MANUFACTURER;
            if (!android.text.TextUtils.isEmpty(manufacturer) && manufacturer.toLowerCase().contains("oppo")) {
                return true;
            }

            String fingerprint = Build.FINGERPRINT;
            if (!android.text.TextUtils.isEmpty(fingerprint) && fingerprint.toLowerCase().contains("oppo")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isVivo() {
        String manufacturerModel = Build.MANUFACTURER + "-" + Build.MODEL;
        return TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("vivo");
    }
}

