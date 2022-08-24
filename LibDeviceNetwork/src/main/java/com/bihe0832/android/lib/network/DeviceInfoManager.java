package com.bihe0832.android.lib.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

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
    public boolean ishasSimCard() {
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
            // Android 4.4.4：http://androidxref.com/4.4.4_r1/xref/frameworks/base/core/java/android/net/ConnectivityManager.java
            // Android 5.0.0：http://androidxref.com/5.0.0_r2/xref/frameworks/base/core/java/android/net/ConnectivityManager.java
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
        return ishasSimCard() && isMobileSwitchOpened();
    }

    /*默认读取上网卡的运营商信息*/
    public int getMobileMutisetId() {
        if (telephonyManager == null) {
            return -1;
        }
        String simOperator = telephonyManager.getSimOperator();
        ZLog.d("getSimOperator simOperator:" + simOperator);
        return parseOperatorCode(simOperator);
    }

    /*基于服务器返回的setID判断网络的运营商类型*/
    public String getOperatorType(int setId) {
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
                result = "其他";
                break;
            default:
                break;
        }
        return result;
    }

    /*获取4G的运营商类型*/
    public String getMobileOperatorType() {
        String result = "unknow";
        int setId = getMobileMutisetId();
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

    public int parseOperatorCode(String operatorCode) {
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

    /*默认读取拨号卡的运营商信息，而不是上网卡的运营商信息*/
    public String getOperatorName(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         * IMSI相关链接：http://baike.baidu.com/item/imsi
         */
        if (telephonyManager == null) {
            return "";
        }
        // getSimOperatorName就可以直接获取到运营商的名字
        String OperatorName = telephonyManager.getSimOperatorName();
        ZLog.d("getOperatorName OperatorName:" + OperatorName);
        return OperatorName;
    }

    /*默认读取拨号卡的运营商信息，而不是上网卡的运营商信息*/
    public String getProvidersName(Context context) {
        if (telephonyManager == null) {
            return "";
        }
        String ProvidersName = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
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
        ZLog.d("getProvidersName IMSI：" + IMSI);

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

        ZLog.d("getProvidersName 当前卡为：" + ProvidersName);
        return ProvidersName;
    }
}
