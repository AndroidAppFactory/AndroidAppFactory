package com.bihe0832.android.app.network;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.MobileUtil;
import com.bihe0832.android.lib.network.NetworkUtil;
import com.bihe0832.android.lib.network.WifiManagerWrapper;
import com.bihe0832.android.lib.network.WifiUtil;
import com.bihe0832.android.app.network.listener.GlobalNetworkInfo;
import com.bihe0832.android.app.network.listener.NetworkChangeEvent;
import com.bihe0832.android.app.network.listener.NetworkChangeObserver;

public class NetworkChangeManager {

    private static Handler sBgHandler = null;
    private static int sPreNetType = 0;
    private static int sPreDtType = 0;
    private static String sPreBssId = "";
    private static String sPreCellID = "";

    public static void init(Context context) {
        WifiManagerWrapper.Companion.getInstance().init(context, ZixieContext.INSTANCE.isDebug());
        int curNetType = WifiManagerWrapper.Companion.getInstance().getNetType(context);
        String curBssid = "unknown";
        String curCellId = "-1_-1_-1_-1";
        String curWifiSsid = "";
        if (NetworkUtil.isWifiNet(curNetType)) {
            curBssid = WifiUtil.getBssid(context);
            curWifiSsid = WifiUtil.getWifiSSID(context); // 链接的WiFi名称
        } else if (NetworkUtil.isMobileNet(curNetType)) {
            curCellId =  MobileUtil.getPhoneCellInfo(context);
        }

        GlobalNetworkInfo.NetType = curNetType;
        GlobalNetworkInfo.CellId = curCellId;
        GlobalNetworkInfo.WifiBssid = curBssid;
        GlobalNetworkInfo.WifiSsid = curWifiSsid;

        sPreNetType = GlobalNetworkInfo.NetType;
        sPreCellID = GlobalNetworkInfo.CellId;
        sPreBssId = GlobalNetworkInfo.WifiBssid;
        if (sPreBssId == null) {
            sPreBssId = "";
        }
        if (sPreCellID == null) {
            sPreCellID = "";
        }
        HandlerThread mBgThread = new HandlerThread("NetworkChange");
        mBgThread.start();
        sBgHandler = new Handler(mBgThread.getLooper());
    }

    public static void change(final Context context, final Intent intent) {
        if (sBgHandler == null) {
            init(context);
            return;
        }
        sBgHandler.post(new Runnable() {
            @Override
            public void run() {
                int curNetType = WifiManagerWrapper.Companion.getInstance().getNetType(context);
                final NetworkUtil.DtTypeInfo curDtTypeInfo = NetworkUtil.getDtTypeInfo(context);
                int curDtType = curDtTypeInfo.dtType;
                ZLog.d("network change >> netType: " + curNetType + ", deType: " + curDtType);
                ZLog.d("network change >> netType before: " + curNetType + ", dtType: " + curDtType + ",intent.getAction():" + intent.getAction());
                String curBssid = "unknown";
                String curCellId = "-1_-1_-1_-1";
                String curWifiSsid = "";
                int curWifiSignal = 0;
                boolean isPost = false;
                ZLog.d("network change >> netType correctAfter: " + curNetType + ", dtType: " + curDtType + ",intent.getAction():" + intent.getAction());
                if (NetworkUtil.isWifiNet(curNetType)) {
                    WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    curBssid = WifiUtil.getBssid(context);
                    curWifiSsid = WifiUtil.getWifiSSID(context); // 链接的WiFi名称
                    curWifiSignal = WifiUtil.getWifiSignalLevel(context);
                    if (curNetType != sPreNetType || curDtType != sPreDtType || (
                            sPreBssId != null && !sPreBssId.equals(curBssid))) {
                        isPost = true;
                    }
                } else if (NetworkUtil.isMobileNet(curNetType)) {
                    curCellId = MobileUtil.getPhoneCellInfo(context);
                    if (curNetType != sPreNetType || curDtType != sPreDtType || (sPreCellID != null && !sPreCellID
                            .equals(curCellId))) {
                        isPost = true;
                    }
                } else {
                    if (curNetType != sPreNetType || curDtType != sPreDtType) {
                        isPost = true;
                    }
                }

                // 先保存到全局状态中
                GlobalNetworkInfo.NetType = curNetType;
                GlobalNetworkInfo.CellId = curCellId;
                GlobalNetworkInfo.WifiBssid = curBssid;
                GlobalNetworkInfo.WifiSsid = curWifiSsid;

                if (isPost) {
                    ZLog.i("event bus post begin preNetType:" + sPreNetType + " curNetType:" + curNetType + " curWifiSsid:" + curWifiSsid + " preBssId:" + sPreBssId
                            + " bssId:" + curBssid + " curWifiSignal:" + curWifiSignal + " curCellId:" + curCellId + ", preDtType：" + sPreDtType + ",curDtType:" + curDtType);
                    NetworkChangeEvent networkChangeEvent = new NetworkChangeEvent(sPreNetType, curNetType,
                            sPreDtType, curDtType, curWifiSsid, curBssid,
                            curWifiSignal, curCellId, intent, curDtTypeInfo);
                    NetworkChangeObserver.INSTANCE.postNetworkChangeEvent(networkChangeEvent);
                    sPreDtType = curDtType;
                    sPreBssId = curBssid;
                    sPreCellID = curCellId;
                }
            }
        });
    }
}
