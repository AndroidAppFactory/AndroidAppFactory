package com.bihe0832.android.lib.superapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.bihe0832.android.lib.utils.apk.APKUtils;
import com.bihe0832.android.lib.utils.intent.IntentUtils;
import com.bihe0832.android.lib.utils.intent.wrapper.PermissionIntent;
import com.bihe0832.android.lib.utils.os.ManufacturerUtil;
import java.util.ArrayList;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-12-16.
 *         Description: Description
 */
public class APPMarketHelper {

    public static String getFirstMarket(Context context) {
        try {
            if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_GOOGLE_PLAY)) {
                return SuperAPPContants.APK_PACKAGE_NAME_GOOGLE_PLAY;
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_TENCENT_MARKET)) {
                return SuperAPPContants.APK_PACKAGE_NAME_TENCENT_MARKET;
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_XIAOMI_MARKET)) {
                return SuperAPPContants.APK_PACKAGE_NAME_XIAOMI_MARKET;
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_HUAWEI_MARKET)) {
                return SuperAPPContants.APK_PACKAGE_NAME_HUAWEI_MARKET;
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_OPPO_MARKET)) {
                return SuperAPPContants.APK_PACKAGE_NAME_OPPO_MARKET;
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_VIVO_MARKET)) {
                return SuperAPPContants.APK_PACKAGE_NAME_VIVO_MARKET;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private static boolean isMarketInstall(Context context, String marketPackage) {
        return APKUtils.getInstalledPackage(context.getApplicationContext(), marketPackage) != null;
    }

    public static ArrayList<String> getMarkerList(Context context) {
        ArrayList<String> marketList = new ArrayList<>();
        try {
            if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_GOOGLE_PLAY)) {
                marketList.add(SuperAPPContants.APK_PACKAGE_NAME_GOOGLE_PLAY);
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_TENCENT_MARKET)) {
                marketList.add(SuperAPPContants.APK_PACKAGE_NAME_TENCENT_MARKET);
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_XIAOMI_MARKET)) {
                marketList.add(SuperAPPContants.APK_PACKAGE_NAME_XIAOMI_MARKET);
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_HUAWEI_MARKET)) {
                marketList.add(SuperAPPContants.APK_PACKAGE_NAME_HUAWEI_MARKET);
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_OPPO_MARKET)) {
                marketList.add(SuperAPPContants.APK_PACKAGE_NAME_OPPO_MARKET);
            } else if (isMarketInstall(context, SuperAPPContants.APK_PACKAGE_NAME_VIVO_MARKET)) {
                marketList.add(SuperAPPContants.APK_PACKAGE_NAME_VIVO_MARKET);
            }
        } catch (Exception e) {
            return marketList;
        }
        return marketList;
    }

    public static String getManufacturerMarket(Context context) {
        try {
            if (ManufacturerUtil.INSTANCE.isXiaomi()) {
                return SuperAPPContants.APK_PACKAGE_NAME_XIAOMI_MARKET;
            } else if (ManufacturerUtil.INSTANCE.isOppo()) {
                return SuperAPPContants.APK_PACKAGE_NAME_OPPO_MARKET;
            }else if (ManufacturerUtil.INSTANCE.isVivo()) {
                return SuperAPPContants.APK_PACKAGE_NAME_VIVO_MARKET;
            }else if (ManufacturerUtil.INSTANCE.isHuawei()) {
                return SuperAPPContants.APK_PACKAGE_NAME_HUAWEI_MARKET;
            }else {
                return getFirstMarket(context);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean launchMarket(Context context, String marketPackage, String packageName) {
        try {
            Intent intent = Intent.parseUri(String.format("market://details?id=%s", packageName), Intent.URI_INTENT_SCHEME);
            intent.setPackage(marketPackage);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            intent.setSelector(null);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            IntentUtils.startIntent(context, intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
