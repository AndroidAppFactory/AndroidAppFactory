package com.bihe0832.android.lib.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.lang.reflect.Method;

/**
 * 实现与移动网络相关的网络信息获取方法
 */
public class MobileUtil {

    @SuppressLint("StaticFieldLeak")
    private static MobileSignalListener sMobileSignalListener = null;
    private static int sSignalLevel = -1;
    private static int sSignalValue = 1;

    // 必须在有Looper的线程调用，其实主要是Listener的构造函数
    public static void registerMobileSignalListener(Context context) {
        if (context == null) {
            return;
        }
        if(Looper.myLooper() == null) {
            ZLog.d("registerMobileSignalListener failed, looper is null");
            return;
        }
        try {
            // 保证只register一个
            if(sMobileSignalListener == null) {
                sMobileSignalListener = new MobileSignalListener(context);
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    telephonyManager.listen(sMobileSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                }
            }
        } catch(Exception e) {
            ZLog.d("registerMobileSignalListener exception:" + e.getMessage());
        }
    }

    public static void unregisterMobileSignalListener(Context context) {
        if(context == null || sMobileSignalListener == null) {
            return;
        }
        try{
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(telephonyManager != null) {
                telephonyManager.listen(sMobileSignalListener, PhoneStateListener.LISTEN_NONE);
                sMobileSignalListener = null;
            }
        } catch(Exception e) {
            ZLog.d("unregisterMobileSignalListener:" + e.getMessage());
        }
    }

    public static int getSignalLevel() {
        return sSignalLevel;
    }

    public static int getSignalValue() {
        return sSignalValue;
    }

    // 注意PhoneStateListener的构造方法会调用Looper.myLooper()，所以构造函数的线程需要有Looper
    static class MobileSignalListener extends PhoneStateListener {
        private Context mContext;

        public MobileSignalListener(Context context) {
            mContext = context.getApplicationContext(); // 获取Application的context
        }

        // 得到信号的强度由每个tiome供应商,有更新
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            // 调用超类的该方法，在网络信号变化时得到回答信号
            super.onSignalStrengthsChanged(signalStrength);
            // signalLevel
            try {
                int level;
                if (BuildUtils.INSTANCE.getSDK_INT() >= VERSION_CODES.M) {
                    Method method = SignalStrength.class.getMethod("getLevel");
                    method.setAccessible(true);
                    level = (Integer) method.invoke(signalStrength);
                } else {
                    Method method = SignalStrength.class.getMethod("getEvdoLevel");
                    method.setAccessible(true);
                    level = (Integer) method.invoke(signalStrength);
                }
                // 小米手机有level==5的情况，如果遇到这种情况取level=4
                if(level > 4) {
                    level = 4;
                }
                sSignalLevel = level;
                Method getDbmMethod = SignalStrength.class.getMethod("getDbm");
                // 若调用私有方法，必须抑制java对权限的检查
                getDbmMethod.setAccessible(true);
                // 使用invoke调用方法，并且获取方法的返回值，需要传入一个方法所在类的对象，new Object[]
                sSignalValue = (Integer) getDbmMethod.invoke(signalStrength);
            } catch(Exception e) {
                ZLog.d("MobileSignalListener exception:" + e.getMessage());
            }
        }
    }

    @SuppressLint("NewApi")
    public static int getMobileSignalValue(Context ctx) {
        final TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            for(final CellInfo info : telephonyManager.getAllCellInfo()) {
                if(info instanceof CellInfoGsm) {
                    final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                    return gsm.getDbm();
                } else if(info instanceof CellInfoCdma) {
                    final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                    return cdma.getDbm();
                } else if(info instanceof CellInfoLte) {
                    final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                    return lte.getDbm();
                } else {
                    throw new Exception("Unknown type of cell signal!");
                }
            }
        } catch(Exception e) {
            ZLog.e("Unable to obtain cell signal information");
        }
        return 0;
    }



    /**
     * 获取基站信息 返回mcc_mnc_lac_cid格式
     */
    public static String getPhoneCellInfo(Context context) {
        String cellInfo = "0_0_0_0";
        try {
            String mcc, mnc;
            int lac = -1, cid = -1;
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = mTelephonyManager != null ? mTelephonyManager.getNetworkOperator() : null;
            if(operator == null) {
                return cellInfo;
            }
            // 取mcc和mnc
            mcc = operator.substring(0, 3);
            mnc = operator.substring(3);
            if(mcc == null || mnc == null) {
                ZLog.d("cellinfo:" + cellInfo);
                return cellInfo;
            }
            // 中国移动和中国联通获取LAC、CID的方式
            CellLocation cellLocation = mTelephonyManager.getCellLocation();
            if(cellLocation instanceof GsmCellLocation) { // 移动、联通
                GsmCellLocation gsmlocation = (GsmCellLocation) cellLocation;
                lac = gsmlocation.getLac();
                cid = gsmlocation.getCid();
            } else if(cellLocation instanceof CdmaCellLocation) { // 电信
                CdmaCellLocation cdmalocation = (CdmaCellLocation) cellLocation;
                lac = cdmalocation.getNetworkId();
                cid = cdmalocation.getBaseStationId();
            }
            cellInfo = mcc + "_" + mnc + "_" + lac + "_" + cid;
        } catch(Exception e) {
            ZLog.d("getPhoneCellInfo exception:" + e.getMessage());
        }
        return cellInfo;
    }
}
