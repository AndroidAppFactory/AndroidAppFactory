package com.bihe0832.android.lib.superapp;

import android.content.Context;

import com.bihe0832.android.lib.utils.apk.APKUtils;

import java.util.ArrayList;

/**
 * @author zixie code@bihe0832.com
 * Created on 2019-12-16.
 * Description: Description
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

}
