package com.bihe0832.android.lib.network.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Build;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.IpUtils;
import com.bihe0832.android.lib.network.MacUtils;
import com.bihe0832.android.lib.network.NetworkUtil;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 实现WIFI相关的网络信息获取方法
 * <p>
 * 尽量使用 {@link WifiManagerWrapper}
 */

public class WifiUtil {

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    // 信号强度相关默认为void的取值情况
    public static final String VOID_SIGNAL_INFO = "-1_-1_-1_-1";

    public static final String INVALID_BSSID = "02:00:00:00:00:00";

    public static final String DEFAULT_SSID = "<unknown ssid>";

    private static int sLastTerminalCount = -1;

    public static int getSecurity(WifiConfiguration config) {
        if (config == null) {
            return SECURITY_NONE;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static String getWifiSSIDBasedNetworkId(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiSSIDBasedNetworkId(wm);
    }

    /*基于NetID获取WiFi-SSID,无需依赖定位权限*/
    public static String getWifiSSIDBasedNetworkId(WifiManager wm) {
        try {
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (null == info) {
                    return "";
                }
                ZLog.d("getWifiSSIDBasedNetworkId WifiInfo:" + info.toString());

                int netWorkId = info.getNetworkId();
                if (-1 == netWorkId) {
                    return "";
                }

                List<WifiConfiguration> wifiConfigurationList = wm.getConfiguredNetworks();
                if (null == wifiConfigurationList || wifiConfigurationList.size() <= 0) {
                    return "";
                }
                for (WifiConfiguration wifiConfiguration : wifiConfigurationList) {
                    if (wifiConfiguration.networkId == netWorkId) {
                        ZLog.d("getWifiSSIDBasedNetworkId wifiConfiguration:" + wifiConfiguration.toString());
                        return wifiConfiguration.SSID;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }


    //周边Wi-Fi、信道等信息
    public static String getWifiSignal(Context ctx) {
        if (NetworkUtil.getNetworkState(ctx) != NetworkUtil.NETWORK_CLASS_WIFI) {
            return VOID_SIGNAL_INFO;
        }
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm == null) {
                return VOID_SIGNAL_INFO;
            }
            WifiInfo info = wm.getConnectionInfo();
            int curChannel = 0;
            HashMap<Integer, Integer> channelCount = new HashMap<Integer, Integer>();
            List<ScanResult> scanResults = wm.getScanResults();
            if (scanResults.size() <= 0) {
                ZLog.d("scanResult is 0");
            }
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
            int curCount = 0;
            int neibCount = 0;
            int sum = 0;
            for (HashMap.Entry<Integer, Integer> entry : channelCount.entrySet()) {
                int chn = entry.getKey();
                int num = entry.getValue();
                sum += num;
                if (curChannel == chn) {
                    curCount = num;
                } else if (chn >= curChannel - 2 && chn <= curChannel + 2) {
                    neibCount += num;
                }
            }

            return "" + curChannel + "_" + curCount + "_" + neibCount + "_" + sum;
        } catch (Exception e) {
            return VOID_SIGNAL_INFO;
        }
    }


    public static int getWifiSignalLevel(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiSignalLevel(wm);
    }

    public static int getWifiSignalLevel(WifiManager wm) {
        int strength = -1;
        try {
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                strength = getWifiSignalLevel(wm, info.getRssi(), 5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strength;
    }


    public static int getWifiSignalLevel(WifiManager wm, int rssi, int numLevels) {
        int signalLevel = -1;
        try {
            if (BuildUtils.INSTANCE.getSDK_INT() >= 30 && wm != null) {
                signalLevel = wm.calculateSignalLevel(rssi);
            } else {
                signalLevel = WifiManager.calculateSignalLevel(rssi, numLevels);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            signalLevel = -1;
        }

        return signalLevel;
    }

    public static int getCachedTerminalCount() {
        return sLastTerminalCount;
    }

    public static String getGatewayMac(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getGatewayIp(wm);
    }

    public static String getGatewayMac(WifiManager wm) {
        String gateWayIp = getGatewayIp(wm);
        ZLog.d("getWifiMacAddr gateWayIp:" + gateWayIp);
        return MacUtils.getLanMacAddr(gateWayIp);
    }

    public static String getGatewayIp(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getGatewayIp(wm);
    }

    public static String getGatewayIp(WifiManager wm) {
        String ret = IpUtils.INVALID_IP;
        try {
            if (wm != null) {
                DhcpInfo dhcpInfo = wm.getDhcpInfo();
                ret = IpUtils.ipn2s(dhcpInfo.gateway);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static int getWifiLinkSpeed(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiLinkSpeed(wm);
    }

    public static int getWifiLinkSpeed(WifiManager wm) {
        int linkSpeed = -1;
        try {
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info.getBSSID() != null) {
                    // 链接信号强度
                    linkSpeed = info.getLinkSpeed();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return linkSpeed;
    }

    public static int getWifiRssi(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiRssi(wm);
    }

    public static int getWifiRssi(WifiManager wm) {
        int signalValue = 1;
        try {
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info.getBSSID() != null) {
                    // 链接信号强度
                    signalValue = info.getRssi();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return signalValue;
    }

    /**
     * @return int
     * -1   当前非WiFi或未知信道
     * 0    无权限
     */
    public static int getWifiChannel(Context ctx) {
        if (NetworkUtil.getNetworkState(ctx) != NetworkUtil.NETWORK_CLASS_WIFI) {
            return -1;
        }

        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                List<ScanResult> scanResults = wm.getScanResults();
                return WifiChannelInfo.getWiFiChannel(scanResults, info.getBSSID());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getWifiSSID(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiSSID(wm);
    }

    public static String getWifiSSID(WifiManager wm) {
        String ret = "";
        try {
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null) {
                    ret = info.getSSID();
                }
            }
        } catch (Exception e) {
            ZLog.d("getRouterSSID, exception:" + e.getMessage());
        }
        return ret;
    }

    public static String getWifiSSIDWithoutQuotes(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiSSIDWithoutQuotes(wm);
    }

    public static String getWifiSSIDWithoutQuotes(WifiManager wm) {
        return TextFactoryUtils.trimMarks(getWifiSSID(wm));
    }

    public static int getWifiCode(Context context) {
        try {
            Context applicationContext = context.getApplicationContext();
            if (NetworkUtil.getNetworkState(context) == NetworkUtil.NETWORK_CLASS_WIFI) {
                WifiManager wm = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    // 得到配置好的网络连接
                    List<WifiConfiguration> wifiConfigList = wm.getConfiguredNetworks();
                    for (WifiConfiguration wifiConfiguration : wifiConfigList) {
                        //比较networkId，防止配置网络保存相同的SSID
                        if (wifiConfiguration.status == WifiConfiguration.Status.CURRENT) {
                            return getSecurity(wifiConfiguration);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ZLog.d("getWifiCode exception:" + e.getMessage());
        }
        return -1;
    }

    public static String getBssid(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getBssid(wm);
    }

    public static String getBssid(WifiManager wm) {
        String bssid = "";
        try {
            WifiInfo wifiInfo = wm.getConnectionInfo();
            bssid = wifiInfo.getBSSID();
        } catch (Exception e) {
            ZLog.d("getBssid failed, for " + e.toString());
        }
        return bssid;
    }

    public static int getWifiSharedTerminalNum(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiSharedTerminalNum(wm);
    }

    // 如果没有其他终端将返回1，即仅有自己
    public static int getWifiSharedTerminalNum(WifiManager wifiManager) {
        int total = -1;
        try {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            int netipaddr = dhcpInfo.ipAddress;
            int netmask = dhcpInfo.netmask;
            // 如果子网掩码为0直接返回
            if (netmask == 0) {
                netmask = 16777215;
                ZLog.e("netmask is null");
            }
            // ip地址网络序->主机序
            String str_ipaddr = IpUtils.ipn2s(netipaddr);
            try {
                // 判断是否是IPv4地址
                if (!(InetAddress.getByName(str_ipaddr) instanceof Inet4Address)) {
                    ZLog.e("current host ip is not valid ipv4 address");
                    return -5;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return -5;
            }

            int hostIpAddr = IpUtils.ips2h(str_ipaddr);

            // 子网掩码网络序->主机序
            String str_netmask = IpUtils.ipn2s(netmask);
            int hostmask = IpUtils.ips2h(str_netmask);

            // 起点地址
            int startIp = (hostIpAddr & hostmask) + 1;
            // 终点地址，一般为广播地址
            int endIp = (hostIpAddr & hostmask) | (~hostmask);

            ZLog.d("neighborPhones ipaddr:" + IpUtils.iph2s(hostIpAddr) + ",mask:" + IpUtils.iph2s(hostmask));
            ZLog.d("neighborPhones startIp:" + IpUtils.iph2s(startIp) + ",endIp:" + IpUtils.iph2s(endIp));

            // 最多不能超过254个
            int count = 0;
            Vector<String> ipList = new Vector<String>();
            for (int i = startIp; i < endIp; i++) {
                count++;
                if (count > 255) {
                    break;
                }
                String tmpIp = IpUtils.iph2s(i);
                ipList.add(tmpIp);
                MacUtils.getLanMacAddr(tmpIp);
            }

            // 很多情况下2S后才会有返回，因此不如固定在这里sleep2秒后执行
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            Map<String, String> ipmacMap = MacUtils.getHardwareAddress(ipList);
            total = ipmacMap != null ? ipmacMap.size() : 0;
            // 因为必然有自己为终端
            if (total == 0) {
                total = 1;
            }
            ZLog.d("neighborPhones wifis:" + (endIp - startIp) + ",valid host:" + total + ",iplists:" + ipList.size() + ",count:" + count);
        } catch (Exception e) {
            ZLog.e(e.getMessage());
        }
        return total;
    }

    public static String getWifiIp(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return getWifiIp(wm);
    }

    public static String getWifiIp(WifiManager wifiManager) {
        if (null == wifiManager) {
            return "";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null == wifiInfo) {
            return "";
        }
        String ipAddress = IpUtils.ipn2s(wifiInfo.getIpAddress());//得到IPV4地址
        ZLog.i("wifi_ip -> " + ipAddress);
        return ipAddress;
    }

}
