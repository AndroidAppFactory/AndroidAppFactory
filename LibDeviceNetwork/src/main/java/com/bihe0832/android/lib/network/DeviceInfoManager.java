package com.bihe0832.android.lib.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DeviceInfoManager {
    private static volatile DeviceInfoManager INSTANCE = null;
    private ConnectivityManager connectivityManager;
    private TelephonyManager telephonyManager;

    public static DeviceInfoManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DeviceInfoManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeviceInfoManager();
                }
            }
        }
        return INSTANCE;
    }

    public DeviceInfoManager() {

    }

    public void init(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public boolean hasSimCard() {
        if (null == telephonyManager) {
            return true;
        }
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
        ZLog.d(result ? "has SimCard" : "has not SimCard");

        return result;
    }


    /*判断数据开关是否打开，不包含SIM卡的判定*/
    public boolean isMobileSwitchOpened() {
        if (connectivityManager == null) {
            return false;
        }

        // ConnectivityManager类
        Class<?> conMgrClass = null;
        // getMobileDataEnabled方法
        Method getMobileDataEnabledMethod = null;
        boolean isMobileOpened = true;//默认为true，即使反射失败，也要返回true，防止误判
        try {
            // 此处需要区分5.0以下和5.0以下，因为5.0以下需要通过mService.getMobileDataEnabled
            // Android 4.4.4：https://androidxref.com/4.4.4_r1/xref/frameworks/base/core/java/android/net/ConnectivityManager.java
            // Android 5.0.0：https://androidxref.com/5.0.0_r2/xref/frameworks/base/core/java/android/net/ConnectivityManager.java
            conMgrClass = Class.forName(connectivityManager.getClass().getName());

            if (BuildUtils.INSTANCE.getSDK_INT() < Build.VERSION_CODES.LOLLIPOP) {
                Field iConnectivityManagerField = conMgrClass.getDeclaredField("mService");
                iConnectivityManagerField.setAccessible(true);

                Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
                Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                getMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                isMobileOpened = (Boolean) getMobileDataEnabledMethod.invoke(iConnectivityManager);
            } else {
                getMobileDataEnabledMethod = conMgrClass.getDeclaredMethod("getMobileDataEnabled");
                getMobileDataEnabledMethod.setAccessible(true);
                isMobileOpened = (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
            }
        } catch (Exception e) {
            ZLog.e("isMobileOpened " + e.getMessage());
            e.printStackTrace();
        }
        ZLog.d("isMobileOpened:" + isMobileOpened);
        return isMobileOpened;
    }

    /*检测4G是否打开*/
    public boolean isMobileOpened() {
        return hasSimCard() && isMobileSwitchOpened();
    }

    /*获取运营商类型*/
    public String getOperatorName() {
        if (telephonyManager == null) {
            return IspUtil.ISP_TYPE_UNKNOW;
        }
        String simOperator = telephonyManager.getSimOperator();
        ZLog.d("getSimOperator simOperator:" + simOperator);
        return IspUtil.getOperatorDesc(simOperator);
    }

    /*通过系统接口获取运营商类型*/
    public String getOperatorName(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         * IMSI相关链接：https://baike.baidu.com/item/imsi
         */
        if (telephonyManager == null) {
            return "";
        }
        // getSimOperatorName就可以直接获取到运营商的名字
        String OperatorName = telephonyManager.getSimOperatorName();
        ZLog.d("getOperatorName OperatorName:" + OperatorName);
        return OperatorName;
    }
}
