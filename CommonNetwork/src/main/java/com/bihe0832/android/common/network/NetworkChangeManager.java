package com.bihe0832.android.common.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.bihe0832.android.framework.ZixieContext;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.MobileUtil;
import com.bihe0832.android.lib.network.NetworkUtil;
import com.bihe0832.android.lib.network.WifiManagerWrapper;
import com.bihe0832.android.lib.thread.ThreadManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NetworkChangeManager {


    private static final String TAG = "NetworkChangeManager";
    private int sPreNetType = 0;
    private int sPreDtType = 0;
    private String sPreBssId = "";
    private String sPreCellID = "";

    private BroadcastReceiver mNetReceiver = null;
    private Handler mBgHandler = null;
    private ArrayList<NetworkChangeListener> networkChangeListeners = new ArrayList<NetworkChangeListener>();

    private static volatile NetworkChangeManager instance;
    public static NetworkChangeManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new NetworkChangeManager();
                }
            }
        }
        return instance;
    }

    private NetworkChangeManager() {
    }


    public void init(Context context) {
        mBgHandler = new Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_NORMAL));
        WifiManagerWrapper.Companion.getInstance().init(context, ZixieContext.INSTANCE.isDebug());
        int curNetType = WifiManagerWrapper.Companion.getInstance().getNetType(context);
        String curBssid = "unknown";
        String curCellId = "-1_-1_-1_-1";
        if (NetworkUtil.isWifiNet(curNetType)) {
            curBssid = WifiManagerWrapper.Companion.getInstance().getBSSID();
        } else if (NetworkUtil.isMobileNet(curNetType)) {
            curCellId = MobileUtil.getPhoneCellInfo(context);
        }

        sPreNetType = curNetType;
        sPreCellID = curCellId;
        sPreBssId = curBssid;
        if (sPreBssId == null) {
            sPreBssId = "";
        }
        if (sPreCellID == null) {
            sPreCellID = "";
        }

        listenNetChange(context);
    }

    private void listenNetChange(Context ctx) {
        mNetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                change(context.getApplicationContext(), intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        ctx.registerReceiver(mNetReceiver, intentFilter);
    }

    private void change(final Context context, final Intent intent) {
        ZLog.d(TAG,"change");
        if (mBgHandler == null) {
            init(context);
            return;
        }
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                int curNetType = WifiManagerWrapper.Companion.getInstance().getNetType(context);
                final NetworkUtil.DtTypeInfo curDtTypeInfo = NetworkUtil.getDtTypeInfo(context);
                int curDtType = curDtTypeInfo.dtType;
                ZLog.d(TAG, "network change >> netType: " + curNetType + ", deType: " + curDtType);
                ZLog.d(TAG, "network change >> netType before: " + curNetType + ", dtType: " + curDtType + ",intent.getAction():" + intent.getAction());
                String curBssid = "unknown";
                String curCellId = "-1_-1_-1_-1";
                String curWifiSsid = "";
                int curWifiSignal = 0;
                boolean needPost = false;
                ZLog.d(TAG, "network change >> netType correctAfter: " + curNetType + ", dtType: " + curDtType + ",intent.getAction():" + intent.getAction());
                if (NetworkUtil.isWifiNet(curNetType)) {
                    curBssid = WifiManagerWrapper.Companion.getInstance().getBSSID();
                    curWifiSsid = WifiManagerWrapper.Companion.getInstance().getSSID();
                    curWifiSignal = WifiManagerWrapper.Companion.getInstance().getSignalLevel();
                    if (curNetType != sPreNetType || curDtType != sPreDtType || (sPreBssId != null && !sPreBssId.equals(curBssid))) {
                        needPost = true;
                    }
                } else if (NetworkUtil.isMobileNet(curNetType)) {
                    curCellId = MobileUtil.getPhoneCellInfo(context);
                    if (curNetType != sPreNetType || curDtType != sPreDtType || (sPreCellID != null && !sPreCellID.equals(curCellId))) {
                        needPost = true;
                    }
                } else {
                    if (curNetType != sPreNetType || curDtType != sPreDtType) {
                        needPost = true;
                    }
                }
                if (needPost) {
                    ZLog.e(TAG, "notify change: preNetType:" + sPreNetType + " curNetType:" + curNetType + " curWifiSsid:" + curWifiSsid + " preBssId:" + sPreBssId
                            + " bssId:" + curBssid + " curWifiSignal:" + curWifiSignal + " curCellId:" + curCellId + ", preDtType：" + sPreDtType + ",curDtType:" + curDtType);
                    NetworkChangeEvent networkChangeEvent = new NetworkChangeEvent(sPreNetType, curNetType, sPreDtType, curDtType, curWifiSsid, curBssid, curWifiSignal, curCellId, curDtTypeInfo);
                    postNetworkChangeEvent(networkChangeEvent, intent);
                    sPreDtType = curDtType;
                    sPreBssId = curBssid;
                    sPreCellID = curCellId;
                }
            }
        });
    }

    public final void addListener(@NotNull NetworkChangeListener networkChangeListener) {
        if (null != networkChangeListener) {
            networkChangeListeners.add(networkChangeListener);
        }
    }

    public final void removeListener(@NotNull NetworkChangeListener networkChangeListener) {
        if (null != networkChangeListener) {
            networkChangeListeners.remove(networkChangeListener);
        }
    }

    public final void postNetworkChangeEvent(@NotNull final NetworkChangeEvent networkChangeEvent, Intent intent) {
        for (NetworkChangeListener networkChangeListener : networkChangeListeners) {
            networkChangeListener.onNetworkChange(networkChangeEvent, intent);
        }
    }
}
