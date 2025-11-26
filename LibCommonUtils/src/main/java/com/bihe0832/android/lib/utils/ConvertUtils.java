package com.bihe0832.android.lib.utils;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import com.bihe0832.android.lib.log.ZLog;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 类型转换工具类
 * 
 * @author zixie code@bihe0832.com
 * Created on 2025-01-15.
 * 
 * Description: 提供常用的类型转换功能，包括：
 * - 字符串到基本类型的安全转换（int、long、float、double、boolean）
 * - 基本类型与字节数组的相互转换
 * - 数组和列表的安全取值
 * - 数值处理工具方法
 */
public class ConvertUtils {

    private static final String TAG = "ConvertUtils";

    /** 无符号int转long时的掩码 */
    private static final long UNSIGNED_INT_MASK = 0x00000000FFFFFFFFL;
    
    /** long转int时的掩码（会丢失精度） */
    private static final long LONG_TO_INT_MASK = 0xFFFFFFFFL;

    /** 整数正则表达式 */
    private static final Pattern numPattenPool = Pattern.compile("^[-\\+]?[\\d]+$");
    
    /** 浮点数正则表达式 */
    private static final Pattern floatPattenPool = Pattern.compile("^[-+]?[\\d]*\\.?[\\d]+$");

    /**
     * 解析字符串为整数，转换出错返回指定默认值
     *
     * @param str 待转换的字符串
     * @param defaultValue 转换失败时的默认值
     * @return 转换后的整数值，失败时返回defaultValue
     */
    public static int parseInt(String str, int defaultValue) {
        int value = defaultValue;

        if (!TextUtils.isEmpty(str)) {
            try {
                if (numPattenPool.matcher(str).matches()) {
                    value = Integer.parseInt(str);
                }
            } catch (Exception e) {
                ZLog.e(TAG, "parseInt failed: " + str + ", error: " + e.getMessage());
            }
        }

        return value;
    }

    /**
     * 解析字符串为整数，转换出错返回-1
     *
     * @param str 待转换的字符串
     * @return 转换后的整数值，失败时返回-1
     */
    public static int parseInt(String str) {
        return parseInt(str, -1);
    }

    /**
     * 解析字符串为长整数，转换出错返回指定默认值
     *
     * @param str 待转换的字符串
     * @param defaultValue 转换失败时的默认值
     * @return 转换后的长整数值，失败时返回defaultValue
     */
    public static long parseLong(String str, long defaultValue) {
        long value = defaultValue;
        if (!TextUtils.isEmpty(str)) {
            try {
                if (numPattenPool.matcher(str).matches()) {
                    value = Long.parseLong(str);
                }
            } catch (Exception e) {
                ZLog.e(TAG, "parseLong failed: " + str + ", error: " + e.getMessage());
            }
        }

        return value;
    }

    /**
     * 解析字符串为布尔值，转换出错返回指定默认值
     * 支持的格式："true"/"false"（不区分大小写）、"1"/"0"
     *
     * @param str 待转换的字符串
     * @param defaultValue 转换失败时的默认值
     * @return 转换后的布尔值，失败时返回defaultValue
     */
    public static boolean parseBoolean(String str, boolean defaultValue) {
        boolean value = defaultValue;
        if (!TextUtils.isEmpty(str)) {
            if ("true".equalsIgnoreCase(str)) {
                return true;
            } else if ("0".equalsIgnoreCase(str)) {
                return false;
            } else if ("1".equalsIgnoreCase(str)) {
                return true;
            } else if ("false".equalsIgnoreCase(str)) {
                return false;
            }
        }
        return value;
    }

    /**
     * 解析字符串为单精度浮点数，转换出错返回指定默认值
     *
     * @param str 待转换的字符串
     * @param defaultValue 转换失败时的默认值
     * @return 转换后的浮点数值，失败时返回defaultValue
     */
    public static float parseFloat(String str, float defaultValue) {
        float value = defaultValue;

        if (!TextUtils.isEmpty(str)) {
            try {
                if (floatPattenPool.matcher(str).matches()) {
                    value = Float.parseFloat(str);
                }
            } catch (Exception e) {
                ZLog.e(TAG, "parseFloat failed: " + str + ", error: " + e.getMessage());
            }
        }

        return value;
    }

    /**
     * 解析字符串为双精度浮点数，转换出错返回指定默认值
     *
     * @param str 待转换的字符串
     * @param defaultValue 转换失败时的默认值
     * @return 转换后的双精度浮点数值，失败时返回defaultValue
     */
    public static double parseDouble(String str, double defaultValue) {
        double value = defaultValue;
        if (!TextUtils.isEmpty(str)) {
            try {
                if (floatPattenPool.matcher(str).matches()) {
                    value = Double.parseDouble(str);
                }
            } catch (Exception e) {
                ZLog.e(TAG, "parseDouble failed: " + str + ", error: " + e.getMessage());
            }
        }
        return value;
    }

    /**
     * 合并两个字节数组
     *
     * @param bt1 第一个字节数组
     * @param bt2 第二个字节数组
     * @return 合并后的字节数组
     */
    public static byte[] mergeBytes(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    /**
     * 将整数转换为字节数组（大端序）
     *
     * @param value 待转换的整数
     * @return 字节数组（4字节）
     */
    public static byte[] intToBytes(int value) {
        int size;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            size = Integer.BYTES;
        } else {
            size = 4;
        }
        return ByteBuffer.allocate(size).putInt(value).array();
    }

    /**
     * 将短整数转换为字节数组（大端序）
     *
     * @param value 待转换的短整数
     * @return 字节数组（2字节）
     */
    public static byte[] shortToBytes(short value) {
        int size;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            size = Short.BYTES;
        } else {
            size = 2;
        }
        return ByteBuffer.allocate(size).putShort(value).array();
    }

    /**
     * 将长整数转换为字节数组（大端序）
     *
     * @param value 待转换的长整数
     * @return 字节数组（8字节）
     */
    public static byte[] longToBytes(long value) {
        int size;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            size = Long.BYTES;
        } else {
            size = 8;
        }
        return ByteBuffer.allocate(size).putLong(value).array();
    }

    /**
     * 将单精度浮点数转换为字节数组（大端序）
     *
     * @param value 待转换的单精度浮点数
     * @return 字节数组（4字节）
     */
    public static byte[] floatToBytes(float value) {
        int size;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            size = Float.BYTES;
        } else {
            size = 4;
        }
        return ByteBuffer.allocate(size).putFloat(value).array();
    }

    /**
     * 将字节数组转换为整数（大端序）
     *
     * @param bytes 字节数组（至少4字节）
     * @return 转换后的整数
     */
    public static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * 将字节数组转换为长整数（大端序）
     *
     * @param bytes 字节数组（至少8字节）
     * @return 转换后的长整数
     */
    public static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    /**
     * 将字节数组转换为短整数（大端序）
     *
     * @param bytes 字节数组（至少2字节）
     * @return 转换后的短整数
     */
    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

    /**
     * 将字节数组转换为单精度浮点数（大端序）
     *
     * @param bytes 字节数组（至少4字节）
     * @return 转换后的单精度浮点数
     */
    public static float bytesToFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }


    /**
     * 获取double数组中的最大值
     *
     * @param doubles double数组
     * @return 数组中的最大值
     * @throws ArrayIndexOutOfBoundsException 如果数组为空
     */
    public static double getMax(double[] doubles) {
        double max = doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if (doubles[i] > max) {
                max = doubles[i];
            }
        }
        return max;
    }

    /**
     * 获取归一化因子
     *
     * @param doubles 待归一化的数组
     * @param maxValue 目标最大值
     * @return 归一化因子
     */
    public static double getNormalizationFactor(double[] doubles, double maxValue) {
        double max = getMax(doubles);
        return max > maxValue ? max / (maxValue + 1) : 1f;
    }

    /**
     * 将有符号int转换为无符号long
     *
     * @param x 有符号整数
     * @return 无符号长整数
     */
    public static long getUnsignedInt(int x) {
        return x & UNSIGNED_INT_MASK;
    }

    /**
     * 将long转换为int（会丢失高32位精度）
     *
     * @param value 长整数
     * @return 转换后的整数（仅保留低32位）
     */
    public static int longToIntWithLossOfPrecision(long value) {
        return (int) (value & LONG_TO_INT_MASK);
    }

    /**
     * 从字符串数组中安全获取指定索引的值
     *
     * @param valueList 字符串数组
     * @param index 索引位置
     * @param defaultValue 默认值（当索引越界或值为空时返回）
     * @return 指定索引的值，失败时返回defaultValue
     */
    public static String getSafeValueFromArray(String[] valueList, int index, String defaultValue) {
        if (valueList != null && index > -1 && index < valueList.length && !TextUtils.isEmpty(valueList[index])) {
            return valueList[index];
        }
        return defaultValue;
    }

    /**
     * 从字符串列表中安全获取指定索引的值
     *
     * @param valueList 字符串列表
     * @param index 索引位置
     * @param defaultValue 默认值（当索引越界或值为空时返回）
     * @return 指定索引的值，失败时返回defaultValue
     */
    public static String getSafeValueFromList(List<String> valueList, int index, String defaultValue) {
        if (valueList != null && index > -1 && index < valueList.size() && !TextUtils.isEmpty(valueList.get(index))) {
            return valueList.get(index);
        }
        return defaultValue;
    }


}
