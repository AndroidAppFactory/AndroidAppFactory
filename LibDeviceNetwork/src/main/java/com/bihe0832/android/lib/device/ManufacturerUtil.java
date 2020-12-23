package com.bihe0832.android.lib.device;

import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.utils.ConvertUtils;
import java.util.Locale;


/**
 * Created by hardyshi on 2017/10/31.
 */

public class ManufacturerUtil {

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机型号
     *
     * @return 型号
     */
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }

    public static String getFingerPrint() {
        return android.os.Build.FINGERPRINT;
    }

    public static String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    public static String getCommonRomVersion() {
        return SystemProperties.get("ro.build.display.id", "");
    }

    public static boolean isCurrentLanguageSimpleChinese() {
        String mCurrentLanguage = Locale.getDefault().getLanguage();
        return mCurrentLanguage.trim().toLowerCase().equals("zh");
    }

    public static boolean isHuawei() {
        if (TextFactoryUtils.trimSpace(getManufacturer().toLowerCase()).contains("huawei")) {
            return true;
        }

        String fingerprint = getFingerPrint();
        if (TextFactoryUtils.trimSpace(fingerprint.toLowerCase()).contains("huawei")) {
            return true;
        }
        return false;
    }

    public static String getEmuiVersion() {
        try {
            String emuiVersion = SystemProperties.get("ro.build.version.emui", (String) null);
            if (emuiVersion != null) {
                return emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }

        return VERSION.SDK_INT >= 24 ? "5.0" : "4.0";
    }

    public static boolean isXiaomi() {
        String manufacturerModel = getManufacturer() + "-" + getDeviceModel();
        return TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("xiaomi")
                || TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("redmi");
    }

    public static boolean isMiRom() {
        String miUiVersionName = SystemProperties.get("ro.miui.ui.version.name", (String) null);
        if (!TextUtils.isEmpty(miUiVersionName)) {
            return true;
        } else {
            return TextFactoryUtils.trimSpace(getManufacturer()).toLowerCase().contains("xiaomi");
        }
    }

    public static String getMiuiVersion() {
        String version = SystemProperties.get("ro.miui.ui.version.name", (String) null);
        if (version != null) {
            try {
                return version.substring(1);
            } catch (Exception var2) {
                ZLog.e("get miui version code error, version : " + version);
            }
        }

        return version;
    }

    public static long getMiuiVersionCode() {
        String miuiVersionCodeStr = SystemProperties.get("ro.miui.ui.version.code", (String) null);
        return ConvertUtils.parseLong(miuiVersionCodeStr, -1);

    }

    public static boolean isOppo() {
        try {
            String manufacturer = getManufacturer();
            if (!android.text.TextUtils.isEmpty(manufacturer) && manufacturer.toLowerCase().contains("oppo")) {
                return true;
            }

            String fingerprint = getFingerPrint();
            if (!android.text.TextUtils.isEmpty(fingerprint) && fingerprint.toLowerCase().contains("oppo")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getOppoRomVersion() {
        String romVersion = SystemProperties.get("ro.build.version.opporom", "");

        try {
            return romVersion.substring(1);
        } catch (Exception var2) {
            var2.printStackTrace();
            ZLog.e("getOppoRomVersion version code error, version : " + romVersion);
            return romVersion;
        }
    }

    public static boolean isVivo() {
        String manufacturerModel = getManufacturer() + "-" + getDeviceModel();
        return TextFactoryUtils.trimSpace(manufacturerModel.toLowerCase()).contains("vivo");
    }

    public static boolean isSmartisan() {
        return TextFactoryUtils.trimSpace(getDeviceBrand().toLowerCase()).contains("smartisan");
    }

    public static boolean isMeizu() {
        return TextFactoryUtils.trimSpace(getDeviceBrand().toLowerCase()).contains("meizu");
    }

    public static String getVivoRomVersion() {
        return SystemProperties.get("ro.vivo.android.os.version", "");
    }

    public static boolean isSumsung() {
        return TextFactoryUtils.trimSpace(getManufacturer().toLowerCase()).contains("samsung");
    }

    public static String getSumsungRomVersion() {
        String version = SystemProperties.get("ro.build.display.id");

        try {
            if (version != null) {
                return version.substring(0, version.indexOf("."));
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        return version;
    }
}

