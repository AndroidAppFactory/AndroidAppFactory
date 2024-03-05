package com.bihe0832.android.lib.utils;

import android.text.TextUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 常用工具类
 */
public class ConvertUtils {

    private static final Pattern numPattenPool = Pattern.compile("^[-\\+]?[\\d]+$");
    private static final Pattern floatPattenPool = Pattern.compile("^[-+]?[\\d]*\\.?[\\d]+$");

    /**
     * 解析字符串为整数, 转换出错返回指定默认值
     *
     * @param str
     * @param defaultValue
     * @return
     */
    public static int parseInt(String str, int defaultValue) {
        int value = defaultValue;

        if (!android.text.TextUtils.isEmpty(str)) {
            try {
                if (numPattenPool.matcher(str).matches()) {
                    value = Integer.parseInt(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * 解析字符串为整数, 转换出错返回-1
     *
     * @param str
     * @return
     */
    public static int parseInt(String str) {
        return parseInt(str, -1);
    }

    /**
     * 解析字符串为整数, 转换出错返回指定默认值
     *
     * @param str
     * @param defaultValue
     * @return
     */
    public static long parseLong(String str, long defaultValue) {
        long value = defaultValue;
        if (!android.text.TextUtils.isEmpty(str)) {
            try {
                if (numPattenPool.matcher(str).matches()) {
                    value = Long.parseLong(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * 解析字符串为整数, 转换出错返回指定默认值
     *
     * @param str
     * @param defaultValue
     * @return
     */
    public static boolean parseBoolean(String str, boolean defaultValue) {
        boolean value = defaultValue;
        if (!android.text.TextUtils.isEmpty(str)) {
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
     * 解析字符串为浮点数, 转换出错返回指定默认值
     *
     * @param str
     * @param defaultValue
     * @return
     */
    public static float parseFloat(String str, float defaultValue) {
        float value = defaultValue;

        if (!android.text.TextUtils.isEmpty(str)) {
            try {
                if (floatPattenPool.matcher(str).matches()) {
                    value = Float.parseFloat(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    /**
     * 解析字符串为浮点数, 转换出错返回指定默认值
     *
     * @param str
     * @param defaultValue
     * @return
     */
    public static double parseDouble(String str, double defaultValue) {
        double value = defaultValue;
        if (!android.text.TextUtils.isEmpty(str)) {
            try {
                if (floatPattenPool.matcher(str).matches()) {
                    value = Double.parseDouble(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 将整形转为低字节在前，高字节在后的byte数组
     *
     * @param n the n
     * @return the byte [ ]
     */
    public static byte[] intToByte(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 将转为低字节在前，高字节在后的byte数组转换为整形
     *
     * @param b the b
     * @return the int
     */
    public static int byteArrayToInt(byte[] b) {
        return b[0] & 0xFF | (b[1] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[3] & 0xFF) << 24;
    }


    public static float[] floatArrayList2Array(List<Float> origin) {
        if (origin == null || origin.size() <= 0) {
            return null;
        }

        float[] result = new float[origin.size()];
        for (int i = 0; i < origin.size(); i++) {
            result[i] = origin.get(i);
        }

        return result;
    }

    public static long getUnsignedInt(int x) {
        return x & 0x00000000FFFFFFFFL;
    }

    public static String getSafeValueFromArray(String[] valueList, int index, String defaultValue) {
        if (valueList != null && index > -1 && index < valueList.length && !TextUtils.isEmpty(valueList[index])) {
            return valueList[index];
        }
        return defaultValue;
    }

    public static String getSafeValueFromList(List<String> valueList, int index, String defaultValue) {
        if (valueList != null && index > -1 && index < valueList.size() && !TextUtils.isEmpty(valueList.get(index))) {
            return valueList.get(index);
        }
        return defaultValue;
    }
}
