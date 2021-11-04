package com.bihe0832.android.lib.network;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实现WIFI相关的网络信息获取方法
 *
 * 尽量使用 {@link WifiManagerWrapper}
 */
@Deprecated
public class WifiUtil {

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    // 信号强度相关默认为void的取值情况
    public static final String VOID_SIGNAL_INFO = "-1_-1_-1_-1";

    private static final int SOCKET_TIMEOUT = 5; // second
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public static final String INVALID_BSSID = "02:00:00:00:00:00";
    private static final String INVALID_MAC = "00:00:00:00:00:00";
    private static final String MAC_RE = "^%s\\s+0x1\\s+0x[2|6]\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;

    private static int sLastTerminalCount = -1;

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement
                .get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    /*基于NetID获取WiFi-SSID,无需依赖定位权限*/
    public static String getWifiSSIDBasedNetworkId(Context context) {
        if (context == null) {
            return "";
        }
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    public static int getWifiSignalLevel(Context context) {
        int strength = -1;
        if (context == null) {
            return strength;
        }
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info.getBSSID() != null) {
                    // 链接信号强度
                    strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return strength;
    }

    // 返回值不为空
    public static RouterInfo getRouterInfo(Context context) {
        if (context == null) {
            ZLog.d("getRouterInfo, context is null");
            return new RouterInfo(-2);
        }
        // 如果是非wifi，则不执行
        if (NetworkUtil.getNetworkState(context) != NetworkUtil.NETWORK_CLASS_WIFI) {
            ZLog.d("getRouterInfo, current net is non-wifi");
            return new RouterInfo(-3);
        }

        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm == null) {
            ZLog.d("getRouterInfo, wifiManager is null");
            return new RouterInfo(-4);
        }

        DhcpInfo dhcpInfo = wm.getDhcpInfo();

        int ipNetSeq = dhcpInfo.ipAddress;
        int netmaskNetSeq = dhcpInfo.netmask;       // 此处的netmask有bug，永远是0，Google拒绝修复
        try {
            InetAddress inetAddress = InetAddress.getByName(IpUtils.ipn2s(dhcpInfo.ipAddress));
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            if (networkInterface == null) {
                return new RouterInfo(-6);
            }
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                short netPrefix = address.getNetworkPrefixLength();
                ZLog.d("netPrefix: " + netPrefix);
                if (netPrefix < 32) {
                    netmaskNetSeq = (1 << (netPrefix)) - 1;
                }
            }
        } catch (IOException e) {

        }
        Map<String, String> beaconData = new HashMap<>();
        beaconData.put("current IP", IpUtils.ipn2s(ipNetSeq));
        beaconData.put("Netmask", IpUtils.ipn2s(netmaskNetSeq));
        beaconData.put("Gateway", IpUtils.ipn2s(dhcpInfo.gateway));

        if (netmaskNetSeq == 0) {
            netmaskNetSeq = 16777215; // 默认子网掩码, 即255.255.255.0
            ZLog.d("getRouterInfo, netmaskNetSeq is null");
        }
        String ipStr = IpUtils.ipn2s(ipNetSeq);
        try {
            // 判断是否是IPv4地址
            if (!(IpUtils.isIpv4Address(ipStr))) {
                ZLog.d("getRouterInfo, current host ip is not valid ipv4 address");
                return new RouterInfo(-5);
            }
        } catch (Exception e) {
            return new RouterInfo(-5);
        }

        int ipHostSeq = IpUtils.ips2h(ipStr); // ip网络序->主机序
        int netmaskHostSeq = IpUtils.ipn2h(netmaskNetSeq); // 子网掩码网络序->主机序
        int startIp = (ipHostSeq & netmaskHostSeq) + 1; // 起点地址
        int endIp = (ipHostSeq & netmaskHostSeq) | (~netmaskHostSeq); // 终点地址，一般为广播地址
        ZLog.d("getRouterInfo, ip:" + ipStr + ", netmask:" + IpUtils.ipn2s(netmaskNetSeq));
        ZLog.d("getRouterInfo, startIp:" + IpUtils.iph2s(startIp) + ", endIp:" + IpUtils.iph2s(endIp));
        int availableIps = 0; // 最多不能超过254个
        ipList = new HashSet<>();
        ZLog.d("Begin sending arp req");
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int ip = startIp; ip < endIp; ip++) {
            availableIps++;
            if (availableIps >= 255) {
                break;
            }
            final String curIpStr = IpUtils.iph2s(ip);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    addIpList(curIpStr);
                    ZLog.d("getRouterInfo1: " + curIpStr);
                    sendArpReqPacket(curIpStr, 1);
                }
            });

        }
        executor.shutdown();
        try {
            executor.awaitTermination(1L, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ZLog.d("End Sending arp req");
        // 很多情况下2S后才会有返回，因此固定在这里sleep2秒后执行
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
        ZLog.d("getLanMacAddr begin");
        Map<String, String> ipMacMap = getLanMacAddr(ipList);
        ZLog.d("getLanMacAddr end");
        int terminals = ipMacMap.size();
        // 必然有自己为终端
        if (terminals == 0) {
            terminals = 1;
        }
        ZLog.d("getRouterInfo, availableIps:" + availableIps + ", terminals:" + terminals);
        //sLastTerminalCount = terminals; // 缓存terminals
        return new RouterInfo(terminals, availableIps, ipMacMap);
    }

    private static Set<String> ipList = new HashSet<String>();

    private static synchronized void addIpList(String IP) {
        ipList.add(IP);
    }

    public static int getCachedTerminalCount() {
        return sLastTerminalCount;
    }

    private static void sendArpReqPacket(String ipStr) {
        try {
            InetAddress inetAddress = IpUtils.getDomainFirstAddr(ipStr);
            if (inetAddress != null) {
                inetAddress.isReachable(SOCKET_TIMEOUT);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void sendArpReqPacket(String ipStr, int count) {
        try {
            InetAddress inetAddress = IpUtils.getDomainFirstAddr(ipStr);
            if (inetAddress != null) {
                for (int i = 0; i < count; i++) {
                    inetAddress.isReachable(SOCKET_TIMEOUT);
                }
            }
        } catch (Exception e) {
            ZLog.d("sendArpReqPacketerr" + e.toString());
            // ignore
        }
    }

    public static Map<String, String> getLanMacAddr(Set<String> ipList) {
        Map<String, String> ipMacMap = new HashMap<>();
        BufferedReader bufferedReader = null;
        String line;
        try {
            if (null != ipList && ipList.size() != 0) {
                if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.P) {
                    Process proc = Runtime.getRuntime().exec("ip neigh show");
                    proc.waitFor();
                    bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(new FileInputStream("/proc/net/arp"), Charset.forName("UTF-8")),
                            WifiUtil.IO_BUFFER_SIZE);
                }

                while ((line = bufferedReader.readLine()) != null) {
                    ZLog.d("getLanMacAddr1 lien:" + line);

                    String[] lineSegments = line.split(" +");
                    if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.P) {
                        if (lineSegments.length > 4) {
                            String ip = lineSegments[0];
                            String macAddr = lineSegments[4];
                            /*
                             * 有些时候同一网段下多个ip可能拥有相同的mac地址，
                             * 因此我们反向维护这个映射，去除mac相同的ip，同时
                             * 我们在ip最后加上" 重复次数"
                             */
                            if (!macAddr.equals(INVALID_MAC)) {
                                ZLog.d("putLanMacAddr:" + ip + " " + macAddr);
                                String macIP = ipMacMap.get(macAddr);
                                if (macIP == null) {
                                    ipMacMap.put(macAddr, ip + " 1");
                                } else {
                                    int dup = Integer.parseInt(macIP.split(" ")[1]);
                                    ipMacMap.put(macAddr, macIP.split(" ")[0] + " " + String.valueOf(dup + 1));
                                }
                                //ipMacMap.put(ip, macAddr);
                            }

                        }
                    } else {
                        if (lineSegments.length > 4) {
                            String ip = lineSegments[0];
                            String macAddr = lineSegments[3];
                            /*
                             * 有些时候同一网段下多个ip可能拥有相同的mac地址，
                             * 因此我们反向维护这个映射，去除mac相同的ip，同时
                             * 我们在ip最后加上" 重复次数"
                             */
                            if (!macAddr.equals(INVALID_MAC)) {
                                ZLog.d("putLanMacAddr:" + ip + " " + macAddr);
                                String macIP = ipMacMap.get(macAddr);
                                if (macIP == null) {
                                    ipMacMap.put(macAddr, ip + " 1");
                                } else {
                                    int dup = Integer.parseInt(macIP.split(" ")[1]);
                                    ipMacMap.put(macAddr, macIP.split(" ")[0] + " " + String.valueOf(dup + 1));
                                }
                                //ipMacMap.put(ip, macAddr);
                            }

                        }
                    }


                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return ipMacMap;
    }

    public static String getWifiMacAddr(WifiManager wm) {
        if (null == wm) {
            return "";
        }
        DhcpInfo dhcpInfo = wm.getDhcpInfo();
        if (null == dhcpInfo) {
            return "";
        }
        String gateWayIp = IpUtils.ipn2s(dhcpInfo.gateway);
        ZLog.d("getWifiMacAddr gateWayIp:" + gateWayIp);
        return WifiUtil.getLanMacAddr(gateWayIp);
    }

    public static String getLanMacAddr(String wifiGateIp) {
        String ipMac = "";
        BufferedReader bufferedReader = null;
        try {
            if (wifiGateIp != null && wifiGateIp.length() != 0) {
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), IO_BUFFER_SIZE);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(INVALID_MAC)) {
                        continue;
                    }
                    if (!line.contains(wifiGateIp)) {
                        continue;
                    }
                    String linePattern = String.format(MAC_RE, wifiGateIp.replace(".", "\\."));
                    Pattern pattern = Pattern.compile(linePattern);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String macAddr = matcher.group(1);
                        if (!macAddr.equals(INVALID_MAC)) {
                            ipMac = macAddr;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return ipMac;
    }

    public static class RouterInfo {

        // terminals > 0: 终端数
        // terminals = -1: 未完成终端数计算
        // terminals = -2: context为空
        // terminals = -3: 当前不是wifi网络
        // terminals = -5: 当前ip不是ipv4地址
        public final int terminals;
        public final int availableIps;
        public final Map<String, String> ipMacMap;

        public RouterInfo(int terminals, int availableIps, Map<String, String> ipMacMap) {
            this.terminals = terminals;
            this.availableIps = availableIps;
            this.ipMacMap = ipMacMap;
        }

        public RouterInfo(int terminals) {
            this(terminals, 0, null);
        }
    }

    public static String getGatewayIp(Context context) {
        String ret = IpUtils.INVALID_IP;
        if (context == null) {
            return ret;
        }
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                DhcpInfo dhcpInfo = wm.getDhcpInfo();
                ret = IpUtils.ipn2s(dhcpInfo.gateway);
            }
        } catch (Exception e) {
            // ignore
        }
        return ret;
    }

    public static String getRouterMac(Context ctx) {
        String ret = "0";
        try {
            int netType = NetworkUtil.getNetworkState(ctx);
            if (netType == NetworkUtil.NETWORK_CLASS_WIFI) {
                WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    WifiInfo info = wm.getConnectionInfo();
                    if (info != null) {
                        ret = info.getBSSID();
                    }
                }
            }
        } catch (Exception e) {
            ZLog.d("getRouterMac, exception:" + e.getMessage());
        }
        return ret;
    }

    public static int getWifiLinkSpeed(Context ctx) {
        int linkSpeed = -1;
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    public static int getWifiSignalValue(Context ctx) {
        int signalValue = 1;
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
     *         -1   当前非WiFi或未知信道
     *         0    无权限
     */
    public static int getWifiChannel(Context ctx) {
        if (NetworkUtil.getNetworkState(ctx) != NetworkUtil.NETWORK_CLASS_WIFI) {
            return -1;
        }
        int curChannel = 0;
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                List<ScanResult> scanResults = wm.getScanResults();
                if (scanResults == null || scanResults.size() <= 0) {
                    ZLog.d("channel scanResult is 0, for location switch or permission denied");
                    return curChannel;
                }
                for (ScanResult result : scanResults) {
                    int channel = getWifiChannelByFrequency(result.frequency);
                    if (result.BSSID.equalsIgnoreCase(info.getBSSID())) {
                        curChannel = channel;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return curChannel;
    }

    public static RouterChannelInfo getRouterChannelInfo(WifiManager wifiManager) {
        RouterChannelInfo ret = new RouterChannelInfo();
        try {
            WifiInfo info = wifiManager.getConnectionInfo();
            List<ScanResult> scanResults = wifiManager.getScanResults();
            HashMap<Integer, Integer> channelCount = new HashMap<Integer, Integer>();
            int curChannel = 0;
            for (ScanResult result : scanResults) {
                int channel = getWifiChannelByFrequency(result.frequency);
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

                int channel = getWifiChannelByFrequency(result.frequency);
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

    public static String getWifiSSID(Context context) {
        String ret = "";
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    /*去除字符串中的双引号*/
    public static String removeQuotes(String originStr) {
        return originStr.replace("\"", "");
    }

    public static String getWifiSSIDWithoutQuotes(Context context) {
        return removeQuotes(getWifiSSID(context));
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

    public static String getBssid(@NonNull Context context) {
        String bssid = "";
        try {
            WifiManager wifiManager =
                    (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            bssid = wifiInfo.getBSSID();
        } catch (Exception e) {
            ZLog.d("getBssid failed, for " + e.toString());
        }
        return bssid;
    }

    public static List<WifiChannelInfo> getWifiChannelInfos(@NonNull Context context) {
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
                    boolean is24G = is24GHz(result.frequency);
                    boolean is5G = is5GHz(result.frequency);
                    return new FrequencyInfo(is24G, is5G);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return FrequencyInfo.EMPTY;
    }

    public static boolean is24GHz(int freq) {
        return freq > 2400 && freq < 2500;
    }


    public static boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public static class FrequencyInfo {

        public static final FrequencyInfo EMPTY = new FrequencyInfo(false, false);

        public final boolean is24G;
        public final boolean is5G;

        public FrequencyInfo(boolean is24G, boolean is5G) {
            this.is24G = is24G;
            this.is5G = is5G;
        }
    }

    public static class RouterChannelInfo {

        public int curChannel = -1;
        public int curChannelApCount = -1;
        public int neigbourChannelApCount = -1;
        public int totalApCount = -1;
    }

    public static class WifiChannelInfo {

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
    }

    /**
     * 获取当前路由器上连接的设备数
     * ###耗时操作，慎用###
     */
    public static int getCurrentRouterDevices(Context mContext) {
        if (mContext == null) {
            return -1;
        }
        WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wm.getDhcpInfo();
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
                return -1;
            }
        } catch (UnknownHostException e) {
            ZLog.w("reverseIpMultiThread UnknownHostException error:" + e.getMessage());
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
        }

        // 很多情况下2S后才会有返回，因此不如固定在这里sleep2秒后执行
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        HashMap<String, String> ipmacMap = getHardwareAddress(ipList);
        int total = ipmacMap != null ? ipmacMap.size() : 0;
        ZLog.d("neighborPhones wifis:" + (endIp - startIp) + ",valid host:" + total + ",iplists:" + ipList.size()
                + ",count:" + count);
        return total;
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
                getLanMacAddr(tmpIp);
            }

            // 很多情况下2S后才会有返回，因此不如固定在这里sleep2秒后执行
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            HashMap<String, String> ipmacMap = getHardwareAddress(ipList);
            total = ipmacMap != null ? ipmacMap.size() : 0;
            // 因为必然有自己为终端
            if (total == 0) {
                total = 1;
            }
            ZLog.d("neighborPhones wifis:" + (endIp - startIp) + ",valid host:" + total
                    + ",iplists:" + ipList.size() + ",count:" + count);
        } catch (Exception e) {
            ZLog.e(e.getMessage());
        }
        return total;
    }

    public static HashMap<String, String> getHardwareAddress(Vector<String> ipList) {
        String hw = INVALID_MAC;
        HashMap<String, String> mapList = new HashMap<String, String>();
        BufferedReader bufferedReader = null;
        try {
            if (ipList != null && ipList.size() != 0) {
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(INVALID_MAC)) {
                        continue;
                    }
                    boolean flag = false;
                    for (String ip : ipList) {
                        if (!line.contains(ip)) {
                            continue;
                        }
                        String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                        Pattern pattern = Pattern.compile(ptrn);
                        matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            hw = matcher.group(1);
                            if (!hw.equals(INVALID_MAC)) {
                                // ZLog.debug("neighborPhones ip:" + ip + ",mac:"
                                // + hw);
                                mapList.put(ip, hw);
                                flag = true;
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return mapList;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    ZLog.e("getHardwareAddress, error:" + e.getMessage());
                }
            }
        }
        return mapList;
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
