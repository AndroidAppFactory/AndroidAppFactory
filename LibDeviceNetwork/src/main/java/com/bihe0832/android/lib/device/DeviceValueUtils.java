package com.bihe0832.android.lib.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.encypt.HexUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

public class DeviceValueUtils {
    private static String sAndroidId = "";

    public static final int BUFFERED_SIZE = 1024; // 其实此时已经是2K字节，Java的char是2Byte
    public static final String MARSHMALLOW_MAC = "02:00:00:00:00:00"; // 6.0以后获取到的手机MAC地址

    public static boolean isAirplaneMode(Context context) {
        int isAirplaneMode = Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        return isAirplaneMode == 1;
    }

    public static String getImei(Context context) {
        String imei = "UNKNOWN";
        try {
            if(context != null) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                if(telephonyManager != null) {
                    imei = telephonyManager.getDeviceId();
                }
            }
        } catch(SecurityException se) {
            // ignore
        } catch(Throwable e) {
            ZLog.d("getImei error:" + e.getMessage());
        }
        return imei;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机型号
     *
     * @return 型号
     */
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static int getApiLevel() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static int getBatteryLevel(Context context) {
        if(context == null) {
            ZLog.d("battery context null");
            return -1;
        }
        int level = -1;
        try {
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if(batteryIntent != null) {
                level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        } catch(Exception e) {
            ZLog.d("battery exception:" + e.getMessage());
        }

        return level;
    }

    // acc_back{电量等级，是否充电(充电为1，否则为0)}
    public static int[] getBatteryLevelAndCharging(Context context) {
        int[] value = {-1, 0};
        if(context == null) {
            ZLog.e("battery context null");
            return value;
        }
        try {
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if(batteryIntent != null) {
                value[0] = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = batteryIntent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                if(status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL) {
                    value[1] = 1;
                }
            }
        } catch(Exception e) {
            ZLog.d("battery " + e.getMessage());
        }

        return value;
    }

    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        String mac = "";
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(wm != null) {
                WifiInfo wifiInfo = wm.getConnectionInfo();
                if(wifiInfo != null) {
                    mac = wifiInfo.getMacAddress(); // 方法1
                }
                if(MARSHMALLOW_MAC.equals(mac)) { // MAC地址，6.0以后为02:00:00:00:00:00
                    String result = null;
                    result = getMacAddressByInterface(); // 方法2
                    if(result != null && result.length() > 0) {
                        return result.toUpperCase();
                    } else {
                        result = getAddressMacByFile(); // 方法3
                        if(result != null && result.length() > 0) {
                            return result.toUpperCase();
                        }
                    }
                } else if(mac != null) {
                    return mac.toUpperCase();
                }
            }
        } catch(Exception e) {
            // ignore
        }

        return "";
    }

    private static String getAddressMacByFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/sys/class/net/wlan0/address")), BUFFERED_SIZE);
            String line = reader.readLine();
            if(line != null) {
                return line.toUpperCase();
            }
        } catch(Exception e) {
            ZLog.d("getMacAddress ByFile failed, exception:" + e.getMessage());
        } finally {
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch(Exception e) {
                // ignore
            }
        }
        return "";
    }

    private static String getMacAddressByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for(NetworkInterface nif : all) {
                if(nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = new byte[0];
                    // SDK 9才支持
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                        macBytes = nif.getHardwareAddress();
                    }
                    if(macBytes == null || macBytes.length <= 0) {
                        return "";
                    }
                    StringBuilder res1 = new StringBuilder();
                    for(byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if(res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch(Exception e) {
            ZLog.d("getMacAddress ByInterface failed, exception:" + e.getMessage());
        }
        return "";
    }

    public static String getFingerprint(Context context) {
        String hexString = "";
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signatures = packageInfo.signatures;
            byte[] cert = signatures[0].toByteArray();
            InputStream input = new ByteArrayInputStream(cert);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate c = (X509Certificate) cf.generateCertificate(input);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            hexString = HexUtils.bytes2HexStr(publicKey);
        } catch(Exception e) {
            // ignore
        }
        return hexString;
    }

    /*
     * 获取设备唯一ID AndroidID
     * */
    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        if(sAndroidId == null || sAndroidId.length() <= 0) {
            String androidId = "0";
            try {
                if(context != null) {
                    androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    ZLog.d("getDeviceId androidId:" + androidId + ",hashCode:" + androidId.hashCode());
                }
            } catch(Exception e) {
                ZLog.d("getDeviceId error:" + e.getMessage());
            }
            sAndroidId = androidId;
        }
        return sAndroidId;
    }

    /*
     * 获取设备唯一ID AndroidID_内置卡ID()
     */
    public static String getDeviceId(Context mContext) {
        String mDeviceId = "";
        if(mDeviceId == null || mDeviceId.length() == 0) {
            String androidId = "0";
            String internalSdcardId = "0";
            try {
                if(mContext != null) {
                    androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                }
                String cidString = "/sys/block/mmcblk0/device/cid";
                Object localOb = new FileReader(cidString); // SD Card ID
                if(localOb != null) {
                    internalSdcardId = new BufferedReader((Reader) localOb).readLine();
                }
            } catch(Exception e) {
                ZLog.d("getDeviceId error:" + e.getMessage());
            }
            mDeviceId = androidId + "_" + internalSdcardId;
        }
        return mDeviceId;
    }
}
