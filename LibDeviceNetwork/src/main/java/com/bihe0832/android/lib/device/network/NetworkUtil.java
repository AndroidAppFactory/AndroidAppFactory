package com.bihe0832.android.lib.device.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;


import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * @author hardyshi code@bihe0832.com
 * Created on 2019-07-10.
 * Description: 获取网络相关的基础参数，例如当前的网络状态等
 */
public class NetworkUtil {

    private static final String TAG = "NetworkUtil";
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

    private static String getNetworkName(int netState) {
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
            ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            if (info == null) {
                netValue = NETWORK_CLASS_NONET;
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netValue = NETWORK_CLASS_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                netValue = MobileNetworkUtils.getMobileNetworkClass(context, info);
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                netValue = NETWORK_CLASS_ETHERNET;
            } else if (info.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
                netValue = NETWORK_CLASS_BLUETOOTH;
            } else {
                netValue = NETWORK_CLASS_UNKNOWN;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netValue;
    }

    public boolean isMobile(Context context) {
        return ConnectivityManager.TYPE_MOBILE ==  getNetworkState(context);
    }

    public static boolean isNoNetOr2G(int networkType) {
        return networkType == NetworkUtil.NETWORK_CLASS_NONET
                || networkType == NetworkUtil.NETWORK_CLASS_2_G;
    }

    public static boolean isNoNetOr2G(Context context) {
        int networkType = getNetworkState(context);
        return isNoNetOr2G(networkType);
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

    /*基于服务器返回的setID判断网络的运营商类型*/
    public static String getOperatorType(int setId) {
        String result = "unknow";
        switch (setId) {
            case 0:
                result = "中国电信";
                break;
            case 1:
                result = "中国移动";
                break;
            case 2:
                result = "中国联通";
                break;
            case 3:
                result = "cap";
                break;
            default:
                break;
        }
        return result;
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
                    Log.d(TAG,"getAllNetInterface, available interface:" + ni.getName() + ", address:" + ip);
                    // 过滤掉127段的ip地址
                    if (!ni.isLoopback()) {
                        availableInterface.add(ni.getName() + ":" + ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"getAllNetInterface, all interface:" + availableInterface.toString());
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
        String[] interfaces = getAllNetInterface();
        DtTypeInfo info = new DtTypeInfo();
        if (interfaces == null) {
            return info;
        }
        for (int i = 0; i < interfaces.length; i++) {
            String iface = interfaces[i];
            Log.d(TAG,"getDtTypeInfo iface:" + iface);
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
        Log.d(TAG,"getDtTypeInfo:" + info.toString());
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
                    '}';
        }
    }

    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public static boolean ishasSimCard(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telephonyManager.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        Log.d(TAG,result ? "has SimCard" : "has not SimCard");
        return result;
    }

    public static int getSimOperator(Context context) {
        if (context == null) {
            return -1;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return -1;
        }
        String simOperator = telephonyManager.getSimOperator();
        Log.d(TAG,"getSimOperator simOperator:" + simOperator);
        return parseOperatorCode(simOperator);
    }

    /*默认读取拨号卡的运营商信息，而不是上网卡的运营商信息*/
    public static String getOperatorName(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         * IMSI相关链接：http://baike.baidu.com/item/imsi
         */
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // getSimOperatorName就可以直接获取到运营商的名字
        String OperatorName = telephonyManager.getSimOperatorName();
        Log.d(TAG,"getOperatorName OperatorName:" + OperatorName);
        return OperatorName;
    }

    /*默认读取拨号卡的运营商信息，而不是上网卡的运营商信息*/
    public static String getProvidersName(Context context) {
        String ProvidersName = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "unknow";
        }
        String IMSI = telephonyManager.getSubscriberId();
        if (IMSI == null) {
            return "unknow";
        }
        Log.d(TAG,"getProvidersName IMSI：" + IMSI);

        if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46004") || IMSI.startsWith("46007")) {
            ProvidersName = "中国移动";
        } else if (IMSI.startsWith("46001") || IMSI.startsWith("46006") || IMSI.startsWith("46009")) {
            ProvidersName = "中国联通";
        } else if (IMSI.startsWith("46003") || IMSI.startsWith("46005") || IMSI.startsWith("46011")) {
            ProvidersName = "中国电信";
        } else if (IMSI.startsWith("46020")) {
            ProvidersName = "中国铁通";
        } else {
            ProvidersName = "unknow";
        }

        Log.d(TAG,"getProvidersName 当前卡为：" + ProvidersName);
        return ProvidersName;
    }

    public static int parseOperatorCode(String operatorCode) {
        if (operatorCode == null || "".equals(operatorCode)) {
            return -1;
        }
        switch (operatorCode) {
            case "46000":
            case "46002":
            case "46004":
            case "46007":
            case "46008":
                return 1;
            case "46001":
            case "46006":
            case "46009":
                return 2;
            case "46003":
            case "46005":
            case "46011":
                return 0;
            default:
                return -1;
        }
    }

    /*获取vpn状态，用于上报*/
    public static String getVpnStatus(Context context){
        NetworkUtil.DtTypeInfo dtTypeInfo = NetworkUtil.getDtTypeInfo(context);
        boolean isVpnAvailable = NetworkUtil.checkDtTypeAvailable(dtTypeInfo.dtType, NetworkUtil.DT_TYPE_VPN);
        if (!isVpnAvailable) {
            return "0";
        }
        return dtTypeInfo.vpnIp;
    }

    /*判断当前网络是否能够访问网络,ping 3次百度*/
    public static boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            Log.d(TAG,"hardy isNetworkOnline exitValue:"+exitValue);
            if(exitValue == 0){
                return true;
            }else{
                int len;
                if ((len = ipProcess.getErrorStream().available()) > 0) {
                    byte[] buf = new byte[len];
                    ipProcess.getErrorStream().read(buf);
                    Log.d(TAG,"hardy " + new String(buf));
                }
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    //TODO hardy 临时注释
//    public static boolean is4GReachable(String domain , int netid , int SOCKET_TIMEOUT) {
//        boolean flag = true;
//        try {
//            Log.d(TAG,"is4GReachable netid = " + netid);
//            InetAddress inetAddress = IpUtils.getNetDomainFirstAddr(domain , netid);
//            if(inetAddress != null) {
//                flag = inetAddress.isReachable(SOCKET_TIMEOUT);
//                Log.d(TAG,"is4GReachable flag = "+flag + ";getHostAddress = " + inetAddress.getHostAddress() + ";getHostName = " + inetAddress.getHostName() );
//            }
//        } catch(Exception e) {
//            // ignore
//        }finally {
//            return flag;
//        }
//    }

    //TODO hardy 临时注释
    /*检测4G是否打开*/
//    public static boolean isMobileSwitchOpened(Context context) {
//        return NetworkUtil.ishasSimCard(context) && DeviceInfo.isMobileOpened(context);
//    }
    public static boolean isValidMac(String macStr) {
        if (macStr == null || macStr.length() <= 0) {
            return false;
        }
        String macAddressRule = "([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}";
        // 这是真正的MAC地址；正则表达式；
        if (macStr.matches(macAddressRule)) {
            return true;
        } else {
            Log.d(TAG,"it is not a valid MAC address!!!");
            return false;
        }
    }

    /*获取辅WiFi ip*/
    public static String getWlan1Ip(String[] interfaces) {
        for (int i = 0; i < interfaces.length; i++) {
            String iface = interfaces[i];
            String[] tmps = iface.split(":");
            if (iface.contains("wlan1")) {
                if (tmps.length > 1) {
                    return tmps[1];
                }
            }
        }
        return "";
    }
}
