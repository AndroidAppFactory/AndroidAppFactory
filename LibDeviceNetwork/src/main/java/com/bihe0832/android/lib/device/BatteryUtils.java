package com.bihe0832.android.lib.device;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.bihe0832.android.lib.log.ZLog;

public class BatteryUtils {

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
}
