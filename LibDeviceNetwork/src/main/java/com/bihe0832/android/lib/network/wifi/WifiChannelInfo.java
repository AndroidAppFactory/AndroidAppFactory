package com.bihe0832.android.lib.network.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.bihe0832.android.lib.log.ZLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Wi-Fi 信道信息
 */
public class WifiChannelInfo {

    public final String bssid;
    public final String ssid;
    public final int signalLevelDbm;
    public final int frequency;
    public final int channel;

    public WifiChannelInfo(String bssid, String ssid, int signalLevelDbm, int frequency) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.signalLevelDbm = signalLevelDbm;
        this.frequency = frequency;
        this.channel = getWifiChannelByFrequency(frequency);
    }

    @Override
    public String toString() {

        return "WifiChannelInfo{" +
                "bssid='" + bssid + '\'' +
                ", bssid='" + ssid + '\'' +
                ", signalLevelDbm=" + signalLevelDbm +
                ", frequency=" + frequency +
                ", channel=" + channel +
                '}';


    }

    public static List<WifiChannelInfo> getWifiChannelInfos(Context context) {
        List<WifiChannelInfo> wifiChannelInfos = Collections.emptyList();
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                List<ScanResult> scanResults = wm.getScanResults();
                wifiChannelInfos = new ArrayList<>(scanResults.size());
                for (ScanResult scanResult : scanResults) {
                    wifiChannelInfos.add(new WifiChannelInfo(scanResult.BSSID, scanResult.SSID, scanResult.level,
                            scanResult.frequency));
                }
            }
        } catch (Exception e) {
            ZLog.d("getWifiChannelInfos failed, for " + e.toString());
        }
        return wifiChannelInfos;
    }

    public static int getWiFiChannel(List<ScanResult> canScanWifi, String bssid) {
        int curChannel = -1;
        if (canScanWifi == null || canScanWifi.isEmpty()) {
            ZLog.d("channel scanResult is 0, for location switch or permission denied");
            return curChannel;
        }

        for (ScanResult it : canScanWifi) {
            int channel = getWifiChannelByFrequency(it.frequency);
            if (it.BSSID.equalsIgnoreCase(bssid)) {
                curChannel = channel;
            }
        }
        return curChannel;
    }

    public static int getWifiChannelByFrequency(int frequency) {
        int channel = -1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5180:
                channel = 36;
                break;
            case 5190:
                channel = 38;
                break;
            case 5200:
                channel = 40;
                break;
            case 5210:
                channel = 42;
                break;
            case 5220:
                channel = 44;
                break;
            case 5230:
                channel = 46;
                break;
            case 5240:
                channel = 48;
                break;
            case 5260:
                channel = 52;
                break;
            case 5280:
                channel = 56;
                break;
            case 5300:
                channel = 60;
                break;
            case 5320:
                channel = 64;
                break;
            case 5500:
                channel = 100;
                break;
            case 5520:
                channel = 104;
                break;
            case 5745:
                channel = 149;
                break;
            case 5765:
                channel = 153;
                break;
            case 5785:
                channel = 157;
                break;
            case 5805:
                channel = 161;
                break;
            case 5825:
                channel = 165;
                break;
            default:
                break;
        }
        return channel;
    }
}