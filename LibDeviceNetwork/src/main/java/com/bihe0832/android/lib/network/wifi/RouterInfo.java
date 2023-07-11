package com.bihe0832.android.lib.network.wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.ARPUtils;
import com.bihe0832.android.lib.network.IpUtils;
import com.bihe0832.android.lib.network.MacUtils;
import com.bihe0832.android.lib.network.NetworkUtil;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Description
 */
public class RouterInfo {

    private static Set<String> ipList = new HashSet<String>();


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
        Map<String, String> ipmacMap = MacUtils.getHardwareAddress(ipList);
        int total = ipmacMap != null ? ipmacMap.size() : 0;
        ZLog.d("neighborPhones wifis:" + (endIp - startIp) + ",valid host:" + total + ",iplists:" + ipList.size() + ",count:" + count);
        return total;
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
                    ARPUtils.sendUdpMessage(curIpStr, ARPUtils.UDP_DETECT_PORT, ARPUtils.UDP_DETECT_MSG);
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
        Map<String, String> ipMacMap = MacUtils.getLanMacAddr(ipList);
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

    private static synchronized void addIpList(String IP) {
        ipList.add(IP);
    }


}