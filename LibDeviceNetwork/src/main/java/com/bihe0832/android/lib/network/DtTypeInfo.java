package com.bihe0832.android.lib.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Description
 */
public class DtTypeInfo {

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
        return "DtTypeInfo{" + "dtType=" + dtType + ", vpnIp='" + vpnIp + '\'' + ", ethIp='" + ethIp + '\'' + ", wifiIp='" + wifiIp + '\'' + ", mobileIp='" + mobileIp + '\'' + ", bluetoothIp='" + bluetoothIp + '\'' + ", wifiAuxiliaryIp='" + wifiAuxiliaryIp + '\'' + ", bluetoothAuxiliaryIp='" + bluetoothAuxiliaryIp + '\'' + '}';
    }

    public static DtTypeInfo getDtTypeInfoByInterface(Context context) {
        String[] interfaces = NetworkUtil.getAllNetInterface();
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
                    info.dtType |= NetworkUtil.DT_TYPE_VPN;
                    info.vpnIp = tmps[1];
                }
            } else if (iface.contains("eth")) {
                // 最高优先级，处理下网线断开地址仍在的情况
                if (context != null && !NetworkUtil.isEtherNet(context)) {
                    continue;
                }
                if (tmps.length > 1) {
                    info.dtType |= NetworkUtil.DT_TYPE_ETH;
                    info.ethIp = tmps[1];
                }
            } else if (iface.contains("wlan0")) {
                if (tmps.length > 1) {
                    info.dtType |= NetworkUtil.DT_TYPE_WIFI;
                    info.wifiIp = tmps[1];
                }
            } else if (iface.contains("wlan1")) {
                if (tmps.length > 1) {
                    info.dtType |= NetworkUtil.DT_TYPE_WIFI_AUXILIARY;
                    info.wifiAuxiliaryIp = tmps[1];
                }
            } else if (iface.contains("rmnet")) {
                if (tmps.length > 1) {
                    info.dtType |= NetworkUtil.DT_TYPE_MOBILE;
                    info.mobileIp = tmps[1];
                }
            } else if (iface.contains("bt")) {
                if (tmps.length > 1) {
                    info.dtType |= NetworkUtil.DT_TYPE_BLUETOOTH;
                    info.bluetoothIp = tmps[1];
                }
            }
        }
        ZLog.d("getDtTypeInfo:" + info.toString());
        return info;
    }

    public static DtTypeInfo getDtTypeInfo(Context context) {
        DtTypeInfo dtTypeinfo = new DtTypeInfo();
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return dtTypeinfo;
            }

            if (BuildUtils.INSTANCE.getSDK_INT() >= Build.VERSION_CODES.LOLLIPOP) {
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
                            dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_VPN;
                            dtTypeinfo.vpnIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_ETHERNET: {
                            dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_ETH;
                            dtTypeinfo.ethIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_BLUETOOTH: {
                            dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_BLUETOOTH;
                            dtTypeinfo.bluetoothIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_MOBILE: {
                            dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_MOBILE;
                            dtTypeinfo.mobileIp = ip;
                            break;
                        }
                        case ConnectivityManager.TYPE_WIFI: {
                            String ifName = linkProperties.getInterfaceName();
                            if (ifName != null) {
                                if (ifName.contains("wlan0")) {
                                    dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_WIFI;
                                    dtTypeinfo.wifiIp = ip;
                                } else if (ifName.contains("wlan1")) {
                                    dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_WIFI_AUXILIARY;
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
                                dtTypeinfo.dtType |= NetworkUtil.DT_TYPE_WIFI_AUXILIARY;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DtTypeInfo that = (DtTypeInfo) o;
        return dtType == that.dtType &&
                vpnIp.equalsIgnoreCase(that.vpnIp) &&
                ethIp.equalsIgnoreCase(that.ethIp) &&
                wifiIp.equalsIgnoreCase(that.wifiIp) &&
                mobileIp.equalsIgnoreCase(that.mobileIp) &&
                bluetoothIp.equalsIgnoreCase(that.bluetoothIp) &&
                wifiAuxiliaryIp.equalsIgnoreCase(that.wifiAuxiliaryIp) &&
                bluetoothAuxiliaryIp.equalsIgnoreCase(that.bluetoothAuxiliaryIp);
    }
}
