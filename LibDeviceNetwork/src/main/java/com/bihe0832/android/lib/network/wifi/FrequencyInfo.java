package com.bihe0832.android.lib.network.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.NetworkUtil;

import java.util.List;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Wi-Fi 频率信息
 */
public class FrequencyInfo {

    public static final FrequencyInfo EMPTY = new FrequencyInfo(false, false);

    public final boolean is24G;
    public final boolean is5G;

    public FrequencyInfo(boolean is24G, boolean is5G) {
        this.is24G = is24G;
        this.is5G = is5G;
    }

    public static FrequencyInfo getWifiFrequencyInfo(Context ctx) {
        if (NetworkUtil.getNetworkState(ctx) != NetworkUtil.NETWORK_CLASS_WIFI) {
            return FrequencyInfo.EMPTY;
        }
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (null == wm) {
                return FrequencyInfo.EMPTY;
            }
            WifiInfo info = wm.getConnectionInfo();
            List<ScanResult> scanResults = wm.getScanResults();
            if (scanResults == null || scanResults.size() <= 0) {
                ZLog.d("channel scanResult is 0, for location switch or permission denied");
                return FrequencyInfo.EMPTY;
            }
            for (ScanResult result : scanResults) {
                if (result.BSSID.equalsIgnoreCase(info.getBSSID())) {
                    boolean is24G = is24GHzWiFi(result.frequency);
                    boolean is5G = is5GHzWiFi(result.frequency);
                    return new FrequencyInfo(is24G, is5G);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return FrequencyInfo.EMPTY;
    }

    public static boolean is24GHzWiFi(int freq) {
        return freq > 2400 && freq < 2500;
    }


    public static boolean is5GHzWiFi(int freq) {
        return freq > 4900 && freq < 5900;
    }
}
