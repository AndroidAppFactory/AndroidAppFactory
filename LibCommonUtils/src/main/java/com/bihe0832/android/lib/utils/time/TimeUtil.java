package com.bihe0832.android.lib.utils.time;

import android.content.Context;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.common.R;

/**
 * 时间格式化工具类
 * 
 * @author zixie code@bihe0832.com
 * Created on 2022-02-24.
 * 
 * Description: 提供各种时间格式化功能，包括：
 * - 秒数转换为可读时长（如：1hour 30min 45s）
 * - 秒数转换为时分秒格式（如：01:30:45）
 * - 秒数转换为中文时长描述（如：1天2小时3分4秒）
 */
public class TimeUtil {

    private static final String TAG = "TimeUtil";

    /**
     * 将秒数格式化为可读的时长字符串
     * 例如：61秒 -> "1min 1s"，3661秒 -> "1hour 1min 1s"
     *
     * @param elapsedSeconds 经过的秒数（必须 >= 0）
     * @return 格式化后的时长字符串，如果参数为负数则返回"0min 0s"
     */
    public static String formatElapsedTime(long elapsedSeconds) {
        // 参数校验
        if (elapsedSeconds < 0) {
            ZLog.e(TAG, "formatElapsedTime: elapsedSeconds < 0, elapsedSeconds=" + elapsedSeconds);
            return "0min 0s";
        }
        
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        
        // 计算小时
        if (elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600;
            elapsedSeconds -= hours * 3600;
        }
        
        // 计算分钟
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        
        // 剩余秒数
        seconds = elapsedSeconds;

        // 格式化输出
        if (hours > 0) {
            return hours + "hour " + minutes + "min " + seconds + "s";
        } else {
            return minutes + "min " + seconds + "s";
        }
    }

    /**
     * 将秒数格式化为时分秒格式（支持自定义显示选项）
     * 例如：61秒 -> "01:01"，3661秒 -> "01:01:01"
     *
     * @param elapsedSeconds 经过的秒数（必须 >= 0）
     * @param needHour 是否需要显示小时部分
     * @param needHourAlways 是否始终显示小时部分（即使为0）
     * @param needSpace 分隔符是否需要空格（true: " : "，false: ":"）
     * @return 格式化后的时分秒字符串，如果参数为负数则返回"00:00"
     */
    public static String formatSecondsTo00(long elapsedSeconds, boolean needHour, boolean needHourAlways, boolean needSpace) {
        // 参数校验
        if (elapsedSeconds < 0) {
            ZLog.e(TAG, "formatSecondsTo00: elapsedSeconds < 0, elapsedSeconds=" + elapsedSeconds);
            return "00:00";
        }
        
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        
        // 计算小时
        if (needHour && elapsedSeconds >= 3600) {
            hours = elapsedSeconds / 3600;
            elapsedSeconds -= hours * 3600;
        }
        
        // 计算分钟
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        
        // 剩余秒数
        seconds = elapsedSeconds;

        // 确定分隔符
        String divider = needSpace ? " : " : ":";

        // 格式化分钟和秒
        String result = String.format("%02d", minutes) + divider + String.format("%02d", seconds);

        // 根据需要添加小时部分
        if (needHour) {
            if (hours > 0 || needHourAlways) {
                result = String.format("%02d", hours) + divider + result;
            }
        }

        return result;
    }

    /**
     * 将秒数格式化为时分秒格式（默认配置）
     * 例如：61秒 -> "01:01"，3661秒 -> "01:01:01"，2403564秒 -> "667:39:24"
     * 
     * 默认配置：
     * - 需要显示小时部分（当小时>0时）
     * - 不强制显示小时部分（小时为0时不显示）
     * - 分隔符不带空格（":"）
     *
     * @param elapsedSeconds 经过的秒数（必须 >= 0）
     * @return 格式化后的时分秒字符串，如果参数为负数则返回"00:00"
     */
    public static String formatSecondsTo00(long elapsedSeconds) {
        return formatSecondsTo00(elapsedSeconds, true, false, false);
    }

    /**
     * 将秒数格式化为中文时长描述
     * 例如：61秒 -> "1分1秒"，3661秒 -> "1小时1分1秒"，90061秒 -> "1天1小时1分1秒"
     * 
     * 注意：使用资源文件中的字符串，支持国际化
     *
     * @param context Android上下文，用于获取资源字符串
     * @param seconds 经过的秒数（必须 >= 0）
     * @return 格式化后的中文时长描述，如果参数为负数则返回"0秒"
     */
    public static String formatSecondsToDurationDesc(Context context, long seconds) {
        // 参数校验
        if (context == null) {
            ZLog.e(TAG, "formatSecondsToDurationDesc: context is null");
            return "";
        }
        
        if (seconds < 0) {
            ZLog.e(TAG, "formatSecondsToDurationDesc: seconds < 0, seconds=" + seconds);
            return "0" + context.getString(R.string.date_second_short);
        }
        
        String timeStr = "";
        
        if (seconds > 59) {
            // 计算秒
            long second = seconds % 60;
            long min = seconds / 60;
            timeStr = second + context.getString(R.string.date_second_short);
            
            if (min > 59) {
                // 计算分钟和小时
                long hour = min / 60;
                min = min % 60;
                timeStr = min + context.getString(R.string.date_minute_short) + timeStr;
                
                if (hour > 23) {
                    // 计算小时和天
                    long day = hour / 24;
                    hour = hour % 24;
                    timeStr = hour + context.getString(R.string.date_hour_short) + timeStr;
                    timeStr = day + context.getString(R.string.date_day_short) + timeStr;
                } else {
                    timeStr = hour + context.getString(R.string.date_hour_short) + timeStr;
                }
            } else {
                timeStr = min + context.getString(R.string.date_minute_short) + timeStr;
            }
        } else {
            timeStr = seconds + context.getString(R.string.date_second_short);
        }
        
        return timeStr;
    }
}