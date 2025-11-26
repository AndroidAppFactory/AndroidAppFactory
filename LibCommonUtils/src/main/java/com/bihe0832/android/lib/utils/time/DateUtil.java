package com.bihe0832.android.lib.utils.time;

import com.bihe0832.android.lib.log.ZLog;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * @author zixie code@bihe0832.com
 * Created on 2017/11/10.
 * Description: 日期时间工具类，提供各种日期转换、比较、格式化功能
 */
public class DateUtil {

    public static final long MILLISECOND_OF_MINUTE = 60 * 1000;
    public static final long MILLISECOND_OF_HOUR = MILLISECOND_OF_MINUTE * 60;
    public static final long MILLISECOND_OF_DAY = MILLISECOND_OF_HOUR * 24;
    // 注意：以下为近似值，实际月份天数不固定（28-31天）
    public static final long MILLISECOND_OF_MONTH = MILLISECOND_OF_DAY * 30;
    // 注意：以下为近似值，不考虑闰年（实际为365或366天）
    public static final long MILLISECOND_OF_YEAR = MILLISECOND_OF_MONTH * 12;
    private static final String TAG = "DateUtil";

    // ==================== 一、基础转换方法 ====================

    /**
     * 将时间戳格式化为指定格式的日期字符串
     * @param currentTime 时间戳（毫秒）
     * @param pattern 日期格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的日期字符串，如果pattern为空则返回空字符串
     */
    public static String getDateEN(long currentTime, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            ZLog.e(TAG, "getDateEN: pattern is null or empty");
            return "";
        }
        SimpleDateFormat format = SimpleDateFormatFactory.getSimpleDateFormat(pattern);
        return format.format(new Date(currentTime));
    }

    /**
     * 将时间戳格式化为默认格式的日期字符串（yyyy-MM-dd HH:mm:ss）
     * @param currentTime 时间戳（毫秒）
     * @return 格式化后的日期字符串
     */
    public static String getDateEN(long currentTime) {
        return getDateEN(currentTime, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前时间并格式化为指定格式的日期字符串
     * @param pattern 日期格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的当前日期字符串
     */
    public static String getCurrentDateEN(String pattern) {
        return getDateEN(System.currentTimeMillis(), pattern);
    }

    /**
     * 获取当前时间并格式化为默认格式的日期字符串（yyyy-MM-dd HH:mm:ss）
     * @return 格式化后的当前日期字符串
     */
    public static String getCurrentDateEN() {
        return getCurrentDateEN("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前时间作为文件名格式（yyyyMMddHHmmssSSS）
     * @return 适合作为文件名的时间字符串
     */
    public static String getCurrentTimeAsFileName() {
        return getCurrentDateEN("yyyyMMddHHmmssSSS");
    }

    /**
     * 获取指定时间的中文星期格式
     * @param currentTime 时间戳（毫秒）
     * @param pattern 日期格式模式，需包含星期信息，如 "E"
     * @return 中文星期字符串（如"星期一"），如果pattern为空则返回空字符串
     */
    public static String getCurrentWeekCN(long currentTime, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            ZLog.e(TAG, "getCurrentWeekCN: pattern is null or empty");
            return "";
        }
        SimpleDateFormat format = SimpleDateFormatFactory.getSimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(currentTime)).replace("周", "星期");
    }

    /**
     * 将日期字符串解析为时间戳
     * @param dateString 日期字符串
     * @param pattern 日期格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 时间戳（毫秒），如果解析失败则返回-1
     */
    public static long getTime(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) {
            ZLog.e(TAG, "getTime: dateString is null or empty");
            return -1;
        }
        if (pattern == null || pattern.isEmpty()) {
            ZLog.e(TAG, "getTime: pattern is null or empty");
            return -1;
        }
        DateFormat df = SimpleDateFormatFactory.getSimpleDateFormat(pattern);
        try {
            Date dt = df.parse(dateString);
            return dt.getTime();
        } catch (Exception exception) {
            ZLog.e(TAG, "getTime failed for dateString: " + dateString + ", pattern: " + pattern + ", error: " + exception.getMessage());
        }
        return -1;
    }

    // ==================== 二、日期起始时间方法 ====================

    /**
     * 获取指定时间戳所在天的起始时间戳（当天00:00:00.000）
     * @param currentTimestamp 时间戳（毫秒）
     * @return 当天起始时间戳，如果发生异常则返回原时间戳
     */
    public static long getDayStartTimestamp(long currentTimestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(currentTimestamp);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            ZLog.e(TAG, "getDayStartTimestamp failed for timestamp: " + currentTimestamp + ", error: " + e.getMessage());
            return currentTimestamp;
        }
    }

    /**
     * 将日期字符串解析为该日期所在天的起始时间戳（当天00:00:00.000）
     * @param dateString 日期字符串
     * @param pattern 日期格式模式，如 "yyyy-MM-dd HH:mm:ss"
     * @return 当天起始时间戳，如果解析失败则返回-1
     */
    public static long getDayStartTimestamp(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) {
            ZLog.e(TAG, "getDayStartTimestamp: dateString is null or empty");
            return -1;
        }
        if (pattern == null || pattern.isEmpty()) {
            ZLog.e(TAG, "getDayStartTimestamp: pattern is null or empty");
            return -1;
        }
        DateFormat df = SimpleDateFormatFactory.getSimpleDateFormat(pattern);
        try {
            Date dt = df.parse(dateString);
            return getDayStartTimestamp(dt.getTime());
        } catch (Exception exception) {
            ZLog.e(TAG, "getDayStartTimestamp failed for dateString: " + dateString + ", pattern: " + pattern + ", error: " + exception.getMessage());
        }
        return -1;
    }

    // ==================== 三、日期比较方法 ====================

    /**
     * 比较Date对象和日期字符串的大小
     *
     * @param date1 Date对象
     * @param date2 日期字符串
     * @param pattern2 date2的日期格式模式
     * @return 正数表示date1>date2，0表示date1=date2或比较失败，负数表示date1<date2
     */
    public static int compareDate(Date date1, String date2, String pattern2) {
        if (date1 == null) {
            ZLog.e(TAG, "compareDate: date1 is null");
            return 0;
        }
        if (date2 == null || date2.isEmpty()) {
            ZLog.e(TAG, "compareDate: date2 is null or empty");
            return 0;
        }
        try {
            long dt2 = getTime(date2, pattern2);
            if (dt2 == -1) {
                return 0;
            }
            long dt1 = date1.getTime();
            return Long.compare(dt1, dt2);
        } catch (Exception exception) {
            ZLog.e(TAG, "compareDate failed, error: " + exception.getMessage());
        }
        return 0;
    }

    /**
     * 比较Date对象和日期字符串的大小（使用默认格式 yyyyMMddHHmmss）
     *
     * @param date1 Date对象
     * @param date2 日期字符串，格式为 yyyyMMddHHmmss
     * @return 正数表示date1>date2，0表示date1=date2或比较失败，负数表示date1<date2
     */
    public static int compareDate(Date date1, String date2) {
        return compareDate(date1, date2, "yyyyMMddHHmmss");
    }

    /**
     * 计算两个时间戳之间相差的天数
     *
     * @param timestamp1 第一个时间戳
     * @param timestamp2 第二个时间戳
     * @return 天数差（timestamp1 - timestamp2）
     *         正数表示 timestamp1 更晚（未来几天）
     *         负数表示 timestamp1 更早（过去几天）
     *         0 表示同一天
     */
    public static int getDayDiff(long timestamp1, long timestamp2) {
        long day1Start = getDayStartTimestamp(timestamp1);
        long day2Start = getDayStartTimestamp(timestamp2);
        return (int) ((day1Start - day2Start) / MILLISECOND_OF_DAY);
    }

    /**
     * 判断指定时间戳是否为今天
     *
     * @param timestamp 待判断的时间戳
     * @return true 表示是今天，false 表示不是今天
     */
    public static boolean isToday(long timestamp) {
        return getDayDiff(timestamp, System.currentTimeMillis()) == 0;
    }

    // ==================== 四、时间差计算和展示方法 ====================

    /**
     * 时间差信息类，用于存储计算后的各个时间单位差值
     */
    private static class TimeDiff {
        long years;      // 年数差
        long months;     // 月数差
        long weeks;      // 周数差
        long days;       // 天数差
        long hours;      // 小时差
        long minutes;    // 分钟差
        long millis;     // 毫秒差

        TimeDiff(long diffMillis) {
            this.millis = diffMillis;
            this.years = diffMillis / MILLISECOND_OF_YEAR;
            this.months = diffMillis / MILLISECOND_OF_MONTH;
            this.weeks = diffMillis / (7 * MILLISECOND_OF_DAY);
            this.days = diffMillis / MILLISECOND_OF_DAY;
            this.hours = diffMillis / MILLISECOND_OF_HOUR;
            this.minutes = diffMillis / MILLISECOND_OF_MINUTE;
        }
    }

    /**
     * 计算时间差
     * @param oldTimestamp 旧时间戳
     * @param currentTimestamp 当前时间戳（默认为系统当前时间）
     * @return TimeDiff对象，包含各个时间单位的差值
     */
    private static TimeDiff calculateTimeDiff(long oldTimestamp, long currentTimestamp) {
        return new TimeDiff(currentTimestamp - oldTimestamp);
    }

    /**
     * 计算时间差（使用系统当前时间）
     * @param oldTimestamp 旧时间戳
     * @return TimeDiff对象，包含各个时间单位的差值
     */
    private static TimeDiff calculateTimeDiff(long oldTimestamp) {
        return calculateTimeDiff(oldTimestamp, System.currentTimeMillis());
    }

    /**
     * 计算两个日期字符串之间的时间差（秒）
     * @param date1 第一个日期字符串，格式为 "yyyy-MM-dd HH:mm:ss"
     * @param date2 第二个日期字符串，格式为 "yyyy-MM-dd HH:mm:ss"
     * @return 时间差的绝对值（秒），如果解析失败则返回-1
     */
    public static int getDateDistance(String date1, String date2) {
        if (date1 == null || date1.isEmpty() || date2 == null || date2.isEmpty()) {
            ZLog.e(TAG, "getDateDistance: date1 or date2 is null or empty");
            return -1;
        }
        DateFormat df = SimpleDateFormatFactory.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dt1 = df.parse(date1);
            Date dt2 = df.parse(date2);
            return (int) (Math.abs(dt2.getTime() - dt1.getTime()) / 1000);
        } catch (Exception exception) {
            ZLog.e(TAG, "getDateDistance failed for date1: " + date1 + ", date2: " + date2 + ", error: " + exception.getMessage());
        }
        return -1;
    }

    /**
     * 获取时间差的友好展示文本（样式1：超过1个月显示"M月d日"）
     * <p>展示规则：
     * <ul>
     *   <li>1年以上：显示"yyyy年M月d日"</li>
     *   <li>1个月-1年：显示"M月d日"</li>
     *   <li>2天-1个月：显示"M月d日"</li>
     *   <li>1-2天：显示"昨天"</li>
     *   <li>1小时-1天：显示"X小时前"</li>
     *   <li>1分钟-1小时：显示"X分钟前"</li>
     *   <li>1分钟内：显示"刚刚"</li>
     * </ul>
     * @param oldTimestamp 旧时间戳（毫秒）
     * @return 友好的时间差文本
     */
    public static String getDateCompareResult(long oldTimestamp) {
        TimeDiff diff = calculateTimeDiff(oldTimestamp);
        
        if (diff.years > 0) {
            return getDateEN(oldTimestamp, "yyyy年M月d日");
        } else if (diff.months > 0) {
            return getDateEN(oldTimestamp, "M月d日");
        } else if (diff.weeks > 0) {
            return getDateEN(oldTimestamp, "M月d日");
        } else if (diff.days > 1) {
            return getDateEN(oldTimestamp, "M月d日");
        } else if (diff.days > 0) {
            return "昨天";
        } else if (diff.hours > 0) {
            return diff.hours + "小时前";
        } else if (diff.minutes > 0) {
            return diff.minutes + "分钟前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 获取时间差的友好展示文本（样式2：显示"X年前"、"X月前"、"X周前"）
     * <p>展示规则：
     * <ul>
     *   <li>1年以上：显示"X年前"</li>
     *   <li>1个月-1年：显示"X月前"</li>
     *   <li>1周-1个月：显示"X周前"</li>
     *   <li>2天-1周：显示"M月d日"</li>
     *   <li>1-2天：显示"昨天"</li>
     *   <li>1小时-1天：显示"X小时前"</li>
     *   <li>1分钟-1小时：显示"X分钟前"</li>
     *   <li>1分钟内：显示"刚刚"</li>
     * </ul>
     * @param oldTimestamp 旧时间戳（毫秒）
     * @return 友好的时间差文本
     */
    public static String getDateCompareResult1(long oldTimestamp) {
        TimeDiff diff = calculateTimeDiff(oldTimestamp);
        
        if (diff.years > 0) {
            return diff.years + "年前";
        } else if (diff.months > 0) {
            return diff.months + "月前";
        } else if (diff.weeks > 0) {
            return diff.weeks + "周前";
        } else if (diff.days > 1) {
            return getDateEN(oldTimestamp, "M月d日");
        } else if (diff.days > 0) {
            return "昨天";
        } else if (diff.hours > 0) {
            return diff.hours + "小时前";
        } else if (diff.minutes > 0) {
            return diff.minutes + "分钟前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 获取时间差的友好展示文本（样式3：超过1年显示"yyyy-MM-dd"）
     * <p>展示规则：
     * <ul>
     *   <li>1年以上：显示"yyyy-MM-dd"</li>
     *   <li>1天-1年：显示"MM-dd HH:mm"</li>
     *   <li>1小时-1天：显示"X小时前"</li>
     *   <li>1分钟-1小时：显示"X分钟前"</li>
     *   <li>1分钟内：显示"刚刚"</li>
     * </ul>
     * @param oldTimestamp 旧时间戳（毫秒）
     * @return 友好的时间差文本
     */
    public static String getDateCompareResult2(long oldTimestamp) {
        TimeDiff diff = calculateTimeDiff(oldTimestamp);
        
        if (diff.years > 0) {
            return getDateEN(oldTimestamp, "yyyy-MM-dd");
        } else if (diff.days > 0) {
            return getDateEN(oldTimestamp, "MM-dd HH:mm");
        } else if (diff.hours > 0) {
            return diff.hours + "小时前";
        } else if (diff.minutes > 0) {
            return diff.minutes + "分钟前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 获取时间差的友好展示文本（自定义格式）
     * <p>展示规则：
     * <ul>
     *   <li>1年以上：使用 yearPattern 格式</li>
     *   <li>7天-1年：使用 monthPattern 格式</li>
     *   <li>2-6天：使用 weekPattern 格式（星期）</li>
     *   <li>昨天或今天之前：使用 yesterdayPattern 格式</li>
     *   <li>今天：使用 timePattern 格式</li>
     * </ul>
     * @param oldTimestamp 旧时间戳（毫秒）
     * @param currentTimestamp 当前时间戳（毫秒）
     * @param yearPattern 超过1年时的日期格式，如 "yyyy年M月d日"
     * @param monthPattern 超过6天时的日期格式，如 "M月d日"
     * @param weekPattern 2-6天时的日期格式（星期），如 "E HH:mm"
     * @param yesterdayPattern 昨天的日期格式，如 "昨天 HH:mm"
     * @param timePattern 今天的时间格式，如 "HH:mm"
     * @return 友好的时间差文本
     */
    public static String getDateCompareResult(long oldTimestamp, long currentTimestamp, String yearPattern,
            String monthPattern, String weekPattern, String yesterdayPattern, String timePattern) {
        // 获取今天的起始时间戳
        long todayStart = getDayStartTimestamp(currentTimestamp);
        
        // 计算时间差
        TimeDiff diff = calculateTimeDiff(oldTimestamp, currentTimestamp);
        
        if (diff.years > 0) {
            return getDateEN(oldTimestamp, yearPattern);
        } else if (diff.days > 6) {
            return getDateEN(oldTimestamp, monthPattern);
        } else if (diff.days > 1) {
            SimpleDateFormat format = SimpleDateFormatFactory.getSimpleDateFormat(weekPattern, Locale.CHINA);
            return format.format(new Date(oldTimestamp)).replace("周", "星期");
        } else if (diff.days > 0 || oldTimestamp < todayStart) {
            return getDateEN(oldTimestamp, yesterdayPattern);
        } else {
            return getDateEN(oldTimestamp, timePattern);
        }
    }
}