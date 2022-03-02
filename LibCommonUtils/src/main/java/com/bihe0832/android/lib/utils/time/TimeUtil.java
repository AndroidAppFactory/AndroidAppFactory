package com.bihe0832.android.lib.utils.time;

/**
 * Created by zixie on 2022/02/24.
 * 各式时间转换
 */
public class TimeUtil {

    private static final String TAG = "TimeUtil";

    //计算时长，结果为例如 61 结果为 1min 1s
    public static String formatElapsedTime(long elapsedSeconds) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        if (elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600;
            elapsedSeconds -= hours * 3600;
        }
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        seconds = elapsedSeconds;

        if (hours > 0) {
            return hours + "hour " + minutes + "min " + seconds + "s";
        } else {
            return minutes + "min " + seconds + "s";
        }
    }

    //计算时长，结果为例如 61 结果为01:01
    public static String formatSecondsTo00(long elapsedSeconds, boolean needHour,boolean needHourAlways, boolean needSpace) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        if (needHour && elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600;
            elapsedSeconds -= hours * 3600;
        }
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        seconds = elapsedSeconds;

        String divider = ":";
        if (needSpace){
            divider = " : ";
        }

        String result = String.format("%02d", minutes) + divider + String.format("%02d", seconds);

        if(needHour){
            if (hours > 0  ||  needHourAlways) {
                result =  String.format("%02d", hours) + divider  + result;
            }
        }

        return result;
    }

    //计算时长，结果为例如: 61 结果为 01:01 , 2403564 结果为 667:39:24
    public static String formatSecondsTo00(long elapsedSeconds) {
        return formatSecondsTo00(elapsedSeconds,true,false,false);
    }
}