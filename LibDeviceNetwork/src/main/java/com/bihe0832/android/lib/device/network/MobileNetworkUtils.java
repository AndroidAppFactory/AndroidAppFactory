package com.bihe0832.android.lib.device.network;

import android.content.Context;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;


/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-12.
 * Description: Description
 */
public class MobileNetworkUtils {
    public static int getMobileNetworkClass(Context context, NetworkInfo info) {
        int netTypeFormInfo = NetworkUtil.NETWORK_CLASS_NONET;
        if (info != null) {
            netTypeFormInfo = getMobileNetworkClass(info.getSubtype());
            if (netTypeFormInfo != NetworkUtil.NETWORK_CLASS_4_G) {
                return netTypeFormInfo;
            }
            // NetworkInfo的subtype有可能未更新，进一步获取
        }
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int netTypeFromManager = getMobileNetworkClass(telephonyManager.getNetworkType());
            if (netTypeFromManager != NetworkUtil.NETWORK_CLASS_NONET) {
                return netTypeFromManager;
            }
        }
        return netTypeFormInfo;
    }

    private static int getMobileNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: // 0
                return NetworkUtil.NETWORK_CLASS_NONET;
            case TelephonyManager.NETWORK_TYPE_GPRS: // 1
            case TelephonyManager.NETWORK_TYPE_EDGE: // 2
            case TelephonyManager.NETWORK_TYPE_CDMA: // 4
            case TelephonyManager.NETWORK_TYPE_1xRTT: // 7
            case TelephonyManager.NETWORK_TYPE_IDEN: // 11
            case 16: // TelephonyManager.NETWORK_TYPE_GSM [api25]  16
                return NetworkUtil.NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS: // 3
            case TelephonyManager.NETWORK_TYPE_EVDO_0: // 5
            case TelephonyManager.NETWORK_TYPE_EVDO_A:// 6
            case TelephonyManager.NETWORK_TYPE_HSDPA: // 8
            case TelephonyManager.NETWORK_TYPE_HSUPA: // 9
            case TelephonyManager.NETWORK_TYPE_HSPA: // 10
            case TelephonyManager.NETWORK_TYPE_EVDO_B: // [api9] 12
            case TelephonyManager.NETWORK_TYPE_EHRPD: // [api11]  14
            case TelephonyManager.NETWORK_TYPE_HSPAP: // [api13]  15
            case 17:  // TelephonyManager.NETWORK_TYPE_TD_SCDMA [api25]  17
                return NetworkUtil.NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE: // api11  13
            case 18: // TelephonyManager.NETWORK_TYPE_IWLAN [api25]  18
            case 19: // TelephonyManager.NETWORK_TYPE_LTE_CA [hide]  19
                return NetworkUtil.NETWORK_CLASS_4_G;
            case 20:  // TelephonyManager.NETWORK_TYPE_NR [hide]  20
                return NetworkUtil.NETWORK_CLASS_5_G;
            default:
                return NetworkUtil.NETWORK_CLASS_4_G;
        }
    }
}
