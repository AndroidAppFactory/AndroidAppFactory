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
import com.bihe0832.android.lib.network.DeviceInfoManager;
import com.bihe0832.android.lib.network.MobileUtil;
import com.bihe0832.android.lib.network.NetworkUtil;
import com.bihe0832.android.lib.network.WifiManagerWrapper;
import com.bihe0832.android.lib.thread.ThreadManager;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class NetworkChangeManager {

    private static final String TAG = "NetworkChangeManager";
    private int mNetType = NetworkUtil.NETWORK_CLASS_UNKNOWN;
    private boolean canGetNetType = false;
    private boolean canGetBSSID = false;
    private boolean canGetCELLID = false;

    private String mWifiBssId = "unknown";
    private String mCellID = "-1_-1_-1_-1";

    private BroadcastReceiver mNetReceiver = null;
    private Handler mBgHandler = null;
    private ArrayList<NetworkChangeListener> networkChangeListeners = new ArrayList<NetworkChangeListener>();

    private static volatile NetworkChangeManager instance;

    public static NetworkChangeManager getInstance() {
        if (instance == null) {
            synchronized (NetworkChangeManager.class) {
                if (instance == null) {
                    instance = new NetworkChangeManager();
                }
            }
        }
        return instance;
    }

    private NetworkChangeManager() {
    }


    public void init(Context context, boolean getNetType) {
        init(context, getNetType, false, false);
    }

    public void init(Context context, boolean getNetType, boolean getBssID, boolean curCellId) {
        DeviceInfoManager.getInstance().init(context.getApplicationContext());
        canGetNetType = getNetType;
        canGetBSSID = getBssID;
        canGetCELLID = curCellId;

        mBgHandler = new Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_NORMAL));

        WifiManagerWrapper.INSTANCE.init(context, ZixieContext.INSTANCE.isDebug());
        if (canGetNetType) {
            mNetType = WifiManagerWrapper.INSTANCE.getNetType(context);
        }
        if (NetworkUtil.isWifiNet(mNetType) && canGetBSSID) {
            mWifiBssId = WifiManagerWrapper.INSTANCE.getBSSID();
        } else if (NetworkUtil.isMobileNet(mNetType) && canGetCELLID) {
            mCellID = MobileUtil.getPhoneCellInfo(context);
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
        ZLog.d(TAG, "change");
        if (mBgHandler == null) {
            init(context, canGetNetType, canGetBSSID, canGetCELLID);
            return;
        }
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                int curNetType = WifiManagerWrapper.INSTANCE.getNetType(context);
                ZLog.d(TAG, "network change >> netType: " + curNetType + ", sPreNetType: " + mNetType);
                String curBssid = "unknown";
                String curCellId = "-1_-1_-1_-1";
                String curWifiSsid = "";
                int curWifiSignal = 0;
                boolean needPost = false;
                ZLog.d(TAG, "network change >> netType correctAfter: " + curNetType + ",intent.getAction():" + intent
                        .getAction());
                if (NetworkUtil.isWifiNet(curNetType) && canGetBSSID) {
                    curBssid = WifiManagerWrapper.INSTANCE.getBSSID();
                    curWifiSsid = WifiManagerWrapper.INSTANCE.getSSID();
                    curWifiSignal = WifiManagerWrapper.INSTANCE.getSignalLevel();
                    if (curNetType != mNetType || (mWifiBssId != null && !mWifiBssId.equals(curBssid))) {
                        needPost = true;
                    }
                } else if (NetworkUtil.isMobileNet(curNetType) && canGetCELLID) {
                    curCellId = MobileUtil.getPhoneCellInfo(context);
                    if (curNetType != mNetType || (mCellID != null && !mCellID.equals(curCellId))) {
                        needPost = true;
                    }
                } else {
                    if (curNetType != mNetType) {
                        needPost = true;
                    }
                }
                if (needPost) {
                    ZLog.e(TAG, "notify change: preNetType:" + mNetType + " curNetType:" + curNetType + " curWifiSsid:"
                            + curWifiSsid + " preBssId:" + mWifiBssId
                            + " bssId:" + curBssid + " curWifiSignal:" + curWifiSignal + " curCellId:" + curCellId);
                    postNetworkChangeEvent(mNetType, curNetType, intent);
                    mWifiBssId = curBssid;
                    mCellID = curCellId;
                    mNetType = curNetType;
                }
            }
        });
    }

    public interface NetworkChangeListener {

        void onNetworkChange(final int sPreNetType, final int curNetType, Intent intent);
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

    public final void postNetworkChangeEvent(final int sPreNetType, final int curNetType, Intent intent) {
        for (NetworkChangeListener networkChangeListener : networkChangeListeners) {
            networkChangeListener.onNetworkChange(sPreNetType, curNetType, intent);
        }
    }

    public String getNetworkName(){
        return NetworkUtil.getNetworkName(mNetType);
    }

    public int getCachedNetType() {
        return mNetType;
    }

    public String getCachedWifiBssId() {
        return mWifiBssId;
    }

    public String getCachedCellID() {
        return mCellID;
    }
}
