package com.bihe0832.android.lib.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import com.bihe0832.android.lib.log.ZLog;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * 实现不区分4G、WIFI的网络信息获取方法
 */
public class NetworkUtil {

    public static final int DT_TYPE_BLUETOOTH_AUXILIARY = 1 << 6;
    public static final int DT_TYPE_WIFI_AUXILIARY = 1 << 5;
    public static final int DT_TYPE_BLUETOOTH = 1 << 4;
    public static final int DT_TYPE_ETH = 1 << 3;
    public static final int DT_TYPE_WIFI = 1 << 2;
    public static final int DT_TYPE_MOBILE = 1 << 1;
    public static final int DT_TYPE_VPN = 1;

    // 0: 无网络, 0:unknown, 1: 2G, 2: 3G, 3: 4G, 4: wifi
    public static final int NETWORK_CLASS_NONET = 0;
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
    public static final int NETWORK_CLASS_WIFI = 4;
    public static final int NETWORK_CLASS_WIFI_4G = 5;//用于上报
    public static final int NETWORK_CLASS_WIFI_WIFI = 6;//用于上报
    public static final int NETWORK_CLASS_5_G = 7;
    public static final int NETWORK_CLASS_BLUETOOTH = 8;
    public static final int NETWORK_CLASS_ETHERNET = 9;

    public static final int DEFAULT_SIGNAL_LEVEL = -1;
    public static final int DEFAULT_SIGNAL_VALUE = 1;

    public static String getNetworkName(Context context) {
        return getNetworkName(getNetworkState(context));
    }

    public static String getNetworkName(int netState) {
        String name = "unknown";
        switch (netState) {
            case NETWORK_CLASS_NONET:
                name = "unknown或无网络";
                break;
            case NETWORK_CLASS_2_G:
                name = "2G";
                break;
            case NETWORK_CLASS_3_G:
                name = "3G";
                break;
            case NETWORK_CLASS_4_G:
                name = "4G";
                break;
            case NETWORK_CLASS_WIFI:
                name = "wifi";
                break;
            case NETWORK_CLASS_ETHERNET:
                name = "ethernet";
                break;
            case NETWORK_CLASS_BLUETOOTH:
                name = "bluetooth";
                break;
            case NETWORK_CLASS_5_G:
                name = "5G";
                break;
            default:
                break;
        }
        return name;
    }

    public static int getNetworkState(Context context) {
        int netValue = NETWORK_CLASS_UNKNOWN;
        if (null == context) {
            return netValue;
        }
        try {
            ConnectivityManager connectMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            if (info == null) {
                netValue = NETWORK_CLASS_NONET;
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netValue = NETWORK_CLASS_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                netValue = getMobileNetworkClass(context, info);
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                netValue = NETWORK_CLASS_ETHERNET;
            } else if (info.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
                netValue = NETWORK_CLASS_BLUETOOTH;
            } else {
                netValue = NETWORK_CLASS_UNKNOWN;
            }
        } catch (Exception e) {
            ZLog.d("getNetworkState exception:" + e.getMessage());
        }
        return netValue;
    }

    public static int getMobileNetworkClass(Context context, NetworkInfo info) {
        int defaultNetTypeFormInfo = NETWORK_CLASS_4_G;
        if (info != null) {
            int netTypeFormInfo = getMobileNetworkClass(info.getSubtype());
            ZLog.i("netTypeFormInfo:" + netTypeFormInfo);
            if (netTypeFormInfo != NETWORK_CLASS_4_G && netTypeFormInfo != NETWORK_CLASS_NONET) {
                return netTypeFormInfo;
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int netTypeFromManager = getMobileNetworkClass(telephonyManager.getNetworkType());
            ZLog.i("netTypeFromManager:" + netTypeFromManager);
            if (netTypeFromManager != NETWORK_CLASS_NONET) {
                return netTypeFromManager;
            }
        }
        return defaultNetTypeFormInfo;
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

    public static boolean isMobileNet(int networkType) {
        return networkType == NetworkUtil.NETWORK_CLASS_2_G
                || networkType == NetworkUtil.NETWORK_CLASS_3_G
                || networkType == NetworkUtil.NETWORK_CLASS_4_G
                || networkType == NetworkUtil.NETWORK_CLASS_5_G;
    }

    public static boolean isMobileNet(Context context) {
        return isMobileNet(getNetworkState(context));
    }

    public static boolean isWifiNet(int networkType) {
        return networkType == NetworkUtil.NETWORK_CLASS_WIFI;
    }

    public static boolean isWifiNet(Context context) {
        return isWifiNet(getNetworkState(context));
    }

    public static boolean isEtherNet(int networkType) {
        return networkType == NetworkUtil.NETWORK_CLASS_ETHERNET;
    }

    public static boolean isEtherNet(Context context) {
        return isEtherNet(getNetworkState(context));
    }

    public static boolean isBluetoothNet(int networkType) {
        return networkType == NetworkUtil.NETWORK_CLASS_BLUETOOTH;
    }

    public static boolean isBluetoothNet(Context context) {
        return isBluetoothNet(getNetworkState(context));
    }

    public static int getSignalLevel(Context context, int networkType) {
        switch (networkType) {
            case NETWORK_CLASS_WIFI:
                return WifiUtil.getWifiSignalLevel(context);
            case NETWORK_CLASS_2_G:
            case NETWORK_CLASS_3_G:
            case NETWORK_CLASS_4_G:
            case NETWORK_CLASS_5_G:
                return MobileUtil.getSignalLevel();
        }
        return DEFAULT_SIGNAL_LEVEL; // level 0-4，默认-1
    }

    public static int getSignalValue(Context context, int networkType) {
        switch (networkType) {
            case NETWORK_CLASS_WIFI:
                return WifiUtil.getWifiSignalValue(context);
            case NETWORK_CLASS_2_G:
            case NETWORK_CLASS_3_G:
            case NETWORK_CLASS_4_G:
            case NETWORK_CLASS_5_G:
                return MobileUtil.getSignalValue();
        }
        return DEFAULT_SIGNAL_VALUE; // value， 负数，默认1
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isNetworkConnected(int netType) {
        return netType != NETWORK_CLASS_UNKNOWN;
    }

    public static boolean checkDtTypeAvailable(int dtType, int targetType) {
        return (dtType & targetType) == targetType;
    }

    public static boolean checkDtTypeWiFi4GVpn(int dtType) {
        final int targetType = DT_TYPE_WIFI | DT_TYPE_MOBILE | DT_TYPE_VPN;
        return checkDtTypeAvailable(dtType, targetType);
    }

    // 格式为name:IP
    public static String[] getAllNetInterface() {
        ArrayList<String> availableInterface = new ArrayList<>();
        String[] interfaces = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }

                    String ip = ia.getHostAddress();
                    ZLog.d("getAllNetInterface, available interface:" + ni.getName() + ", address:" + ip);
                    // 过滤掉127段的ip地址
                    if (!ni.isLoopback()) {
                        availableInterface.add(ni.getName() + ":" + ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ZLog.d("getAllNetInterface, all interface:" + availableInterface.toString());
        int size = availableInterface.size();
        if (size > 0) {
            interfaces = new String[size];
            for (int i = 0; i < size; i++) {
                interfaces[i] = availableInterface.get(i);
            }
        }
        return interfaces;
    }

    public static DtTypeInfo getDtTypeInfo(Context context) {
        DtTypeInfo dtTypeinfo = new DtTypeInfo();
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return dtTypeinfo;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] nets = cm.getAllNetworks();
                for (Network net : nets) {
                    NetworkInfo netInfo = cm.getNetworkInfo(net);
                    if (netInfo == null || !netInfo.isAvailable()) {
                        continue;
                    }
                    LinkProperties linkProperties = cm.getLinkProperties(net);
                    if (linkProperties == null) {
                        continue;
                    }
                    List<LinkAddress> las = linkProperties.getLinkAddresses();
                    if (las == null) {
                        continue;
                    }
                    String ip = "";
                    for (LinkAddress linkAddress : las) {
                        InetAddress ia = linkAddress.getAddress();
                        if (ia instanceof Inet6Address || ia.isLoopbackAddress()) {
                            continue; // skip ipv6 and lookback
                        }
                        ip = ia.getHostAddress();
                    }
                    if (ip == null || ip.length() <= 0) {
                        continue;
                    }
//                    ZLog.d("getDtTypeInfo linkProperties:" + linkProperties.toString());
//                    ZLog.d("getDtTypeInfo type:" + netInfo.getType() + ", netInfo:" + netInfo.toString());
                    switch (netInfo.getType()) {
                        case ConnectivityManager.TYPE_VPN: {
                            dtTypeinfo.dtType |= DT_TYPE_VPN;
                            dtTypeinfo.vpnIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_ETHERNET: {
                            dtTypeinfo.dtType |= DT_TYPE_ETH;
                            dtTypeinfo.ethIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_BLUETOOTH: {
                            dtTypeinfo.dtType |= DT_TYPE_BLUETOOTH;
                            dtTypeinfo.bluetoothIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_MOBILE: {
                            dtTypeinfo.dtType |= DT_TYPE_MOBILE;
                            dtTypeinfo.mobileIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_WIFI: {
                            String ifName = linkProperties.getInterfaceName();
                            if (ifName != null) {
                                if (ifName.contains("wlan0")) {
                                    dtTypeinfo.dtType |= DT_TYPE_WIFI;
                                    dtTypeinfo.wifiIp = ip;
                                } else if (ifName.contains("wlan1")) {
                                    dtTypeinfo.dtType |= DT_TYPE_WIFI_AUXILIARY;
                                    dtTypeinfo.wifiAuxiliaryIp = ip;
                                }
                            }
                            break;
                        }
                        default: {
                            // typeName可能为WIFI_SLAVE, ASUS的type是30
                            String typeName = netInfo.getTypeName();
                            if (typeName != null &&
                                    (typeName.contains("WIFI") || typeName.contains("wifi"))) {
                                dtTypeinfo.dtType |= DT_TYPE_WIFI_AUXILIARY;
                                dtTypeinfo.wifiAuxiliaryIp = ip;
                            }
                            break;
                        }
                    }
                }
                ZLog.d("getDtTypeInfo new:" + dtTypeinfo.toString());
            } else {
                dtTypeinfo = getDtTypeInfoByInterface(context);
            }
            // 检查VICE_BLUETOOTH,先删掉
            /*if (BluetoothConnectService.getInstance().isConnected()) {
                dtTypeinfo.dtType |= DT_TYPE_BLUETOOTH_AUXILIARY;
                // Android的RfComm的蓝牙方式没有IP
                dtTypeinfo.bluetoothAuxiliaryIp = BluetoothConnectService.getInstance().getConnectedBluetoothName();
            }*/

        } catch (Exception e) {
            ZLog.d("getDtTypeInfo Exception:" + e.toString());
        }
        return dtTypeinfo;
    }

    public static DtTypeInfo getDtTypeInfoByInterface(Context context) {
        String[] interfaces = getAllNetInterface();
        DtTypeInfo info = new DtTypeInfo();
        if (interfaces == null) {
            return info;
        }
        for (int i = 0; i < interfaces.length; i++) {
            String iface = interfaces[i];
            ZLog.d("getDtTypeInfo iface:" + iface);
            String[] tmps = iface.split(":");
            if (iface.contains("tun")) {
                if (tmps.length > 1) {
                    info.dtType |= DT_TYPE_VPN;
                    info.vpnIp = tmps[1];
                }
            } else if (iface.contains("eth")) {
                // 最高优先级，处理下网线断开地址仍在的情况
                if (context != null && !NetworkUtil.isEtherNet(context)) {
                    continue;
                }
                if (tmps.length > 1) {
                    info.dtType |= DT_TYPE_ETH;
                    info.ethIp = tmps[1];
                }
            } else if (iface.contains("wlan0")) {
                if (tmps.length > 1) {
                    info.dtType |= DT_TYPE_WIFI;
                    info.wifiIp = tmps[1];
                }
            } else if (iface.contains("wlan1")) {
                if (tmps.length > 1) {
                    info.dtType |= DT_TYPE_WIFI_AUXILIARY;
                    info.wifiAuxiliaryIp = tmps[1];
                }
            } else if (iface.contains("rmnet")) {
                if (tmps.length > 1) {
                    info.dtType |= DT_TYPE_MOBILE;
                    info.mobileIp = tmps[1];
                }
            } else if (iface.contains("bt")) {
                if (tmps.length > 1) {
                    info.dtType |= DT_TYPE_BLUETOOTH;
                    info.bluetoothIp = tmps[1];
                }
            }
        }
        ZLog.d("getDtTypeInfo:" + info.toString());
        return info;
    }

    public static class DtTypeInfo {

        public int dtType = 0;
        public String vpnIp = "";
        public String ethIp = "";
        public String wifiIp = "";
        public String mobileIp = "";
        public String bluetoothIp = "";
        public String wifiAuxiliaryIp = "";
        public String bluetoothAuxiliaryIp = "";

        @Override
        public String toString() {
            return "DtTypeInfo{" +
                    "dtType=" + dtType +
                    ", vpnIp='" + vpnIp + '\'' +
                    ", ethIp='" + ethIp + '\'' +
                    ", wifiIp='" + wifiIp + '\'' +
                    ", mobileIp='" + mobileIp + '\'' +
                    ", bluetoothIp='" + bluetoothIp + '\'' +
                    ", wifiAuxiliaryIp='" + wifiAuxiliaryIp + '\'' +
                    ", bluetoothAuxiliaryIp='" + bluetoothAuxiliaryIp + '\'' +
                    '}';
        }
    }


    /*判断当前网络是否能够访问网络,ping 3次百度*/
    public static boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 weixin.qq.com");
            int exitValue = ipProcess.waitFor();
            ZLog.i("hardy isNetworkOnline exitValue:" + exitValue);
            if (exitValue == 0) {
                return true;
            } else {
                int len;
                if ((len = ipProcess.getErrorStream().available()) > 0) {
                    byte[] buf = new byte[len];
                    ipProcess.getErrorStream().read(buf);
                    ZLog.i("hardy " + new String(buf));
                }
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean is4GReachable(String domain, int netid, int SOCKET_TIMEOUT) {
        boolean flag = true;
        try {
            ZLog.i("is4GReachable netid = " + netid);
            InetAddress inetAddress = IpUtils.getNetDomainFirstAddr(domain, netid);
            if (inetAddress != null) {
                flag = inetAddress.isReachable(SOCKET_TIMEOUT);
                ZLog.i("is4GReachable flag = " + flag + ";getHostAddress = " + inetAddress.getHostAddress()
                        + ";getHostName = " + inetAddress.getHostName());
            }
        } catch (Exception e) {
            // ignore
        } finally {
            return flag;
        }
    }

    public static String getMobileLocalIp(Context context) {
        String mobileLocalIp = "";
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return "";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] nets = cm.getAllNetworks();
                for (Network net : nets) {
                    NetworkInfo netInfo = cm.getNetworkInfo(net);
                    if (netInfo == null || !netInfo.isAvailable()) {
                        continue;
                    }
                    LinkProperties linkProperties = cm.getLinkProperties(net);
                    if (linkProperties == null) {
                        continue;
                    }
                    List<LinkAddress> las = linkProperties.getLinkAddresses();
                    if (las == null) {
                        continue;
                    }
                    String ip = "";
                    for (LinkAddress linkAddress : las) {
                        InetAddress ia = linkAddress.getAddress();
                        if (ia instanceof Inet6Address || ia.isLoopbackAddress()) {
                            continue; // skip ipv6 and lookback
                        }
                        ip = ia.getHostAddress();
                    }
                    if (ip == null || ip.length() <= 0) {
                        continue;
                    }
//                    ZLog.d("getDtTypeInfo linkProperties:" + linkProperties.toString());
//                    ZLog.d("getDtTypeInfo type:" + netInfo.getType() + ", netInfo:" + netInfo.toString());

                    if (ConnectivityManager.TYPE_MOBILE == netInfo.getType()) {
                        mobileLocalIp = ip;
                    }
                }
                ZLog.d("getMobileLocalIp mobileLocalIp:" + mobileLocalIp);
            } else {
                String[] interfaces = getAllNetInterface();
                if (interfaces == null) {
                    return "";
                }
                for (int i = 0; i < interfaces.length; i++) {
                    String iface = interfaces[i];
                    ZLog.d("getMobileLocalIp iface:" + iface);
                    String[] tmps = iface.split(":");
                    if (iface.contains("rmnet")) {
                        if (tmps.length > 1) {
                            mobileLocalIp = tmps[1];
                        }
                    }
                }

                ZLog.d("getMobileLocalIp mobileLocalIp lessthan L:" + mobileLocalIp);
            }
        } catch (Exception e) {
            ZLog.d("getMobileLocalIp Exception:" + e.toString());
        }

        return mobileLocalIp;
    }

    /*需要获取到有效的ipv4的mobile network*/
    public static Network getMobileNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] nets = cm.getAllNetworks();
            for (Network net : nets) {
                NetworkInfo info = cm.getNetworkInfo(net);
                if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (info.isAvailable()) {
                        ZLog.d("getMobileNetwork type:" + info.getType() + ", netInfo:" + info.toString());
                        LinkProperties linkProperties = cm.getLinkProperties(net);
                        if (linkProperties == null) {
                            continue;
                        }
                        ZLog.d("getMobileNetwork linkProperties:" + linkProperties.toString());
                        List<LinkAddress> las = linkProperties.getLinkAddresses();
                        if (las == null) {
                            continue;
                        }
                        String ip = "";
                        for (LinkAddress linkAddress : las) {
                            InetAddress ia = linkAddress.getAddress();
                            if (ia instanceof Inet6Address || ia.isLoopbackAddress()) {
                                continue; // skip ipv6 and lookback
                            }
                            ip = ia.getHostAddress();
                        }
                        if (ip == null || ip.length() <= 0) {
                            continue;
                        }
                        ZLog.d("getMobileNetwork mobileip:" + ip);
                        return net;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /*
     * 获取内网IP
     */
    public static String getInternalIp() {
        try {
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                if (!intf.getDisplayName().contains("wlan")) {
                    continue;
                }
                Enumeration enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && IpUtils.isIpv4Address(inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException var4) {
            ZLog.d("getInternalIp failed, " + var4.toString());
        }
        return null;
    }

    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                            en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                                enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = IpUtils.ipn2s(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    public static String getWifiIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
