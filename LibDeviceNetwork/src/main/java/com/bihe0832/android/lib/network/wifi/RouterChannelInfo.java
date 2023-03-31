package com.bihe0832.android.lib.network.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.bihe0832.android.lib.log.ZLog;

import java.util.HashMap;
import java.util.List;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Description
 */
public class RouterChannelInfo {

    public int curChannel = -1;
    public int curChannelApCount = -1;
    public int neigbourChannelApCount = -1;
    public int totalApCount = -1;

    public static RouterChannelInfo getRouterChannelInfo(WifiManager wifiManager) {
        RouterChannelInfo ret = new RouterChannelInfo();
        try {
            WifiInfo info = wifiManager.getConnectionInfo();
            List<ScanResult> scanResults = wifiManager.getScanResults();
            HashMap<Integer, Integer> channelCount = new HashMap<Integer, Integer>();
            int curChannel = 0;
            for (ScanResult result : scanResults) {
                int channel = WifiChannelInfo.getWifiChannelByFrequency(result.frequency);
                if (result.BSSID.equalsIgnoreCase(info.getBSSID())) {
                    curChannel = channel;
                }
                if (channelCount.containsKey(channel)) {
                    channelCount.put(channel, channelCount.get(channel) + 1);
                } else {
                    channelCount.put(channel, 1);
                }
            }
            int curChannelApCount = 0;
            int neigbourChannelApCount = 0;
            int totalApCount = 0;
            for (HashMap.Entry<Integer, Integer> entry : channelCount.entrySet()) {
                int chn = entry.getKey();
                int num = entry.getValue();
                totalApCount += num;
                if (curChannel == chn) {
                    curChannelApCount = num;
                } else if (chn >= curChannel - 2 && chn <= curChannel + 2) {
                    neigbourChannelApCount += num;
                }
            }
            ret.curChannel = curChannel;
            ret.curChannelApCount = curChannelApCount;
            ret.neigbourChannelApCount = neigbourChannelApCount;
            ret.totalApCount = totalApCount;
        } catch (Exception e) {
            ZLog.e(e.toString());
        }
        return ret;
    }

}