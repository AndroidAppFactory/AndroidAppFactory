package com.bihe0832.android.lib.utils;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import java.nio.ByteBuffer;
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

    public static byte[] mergeBytes(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

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
     * short 转 byte[]
     */
    public static byte[] shortToBytes(short value) {
        int size;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            size = Float.BYTES;
        } else {
            size = 4;
        }
        return ByteBuffer.allocate(size).putFloat(value).array();
    }

    public static byte[] longToBytes(long value) {
        int size;
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            size = Long.BYTES;
        } else {
            size = 8;
        }
        return ByteBuffer.allocate(size).putLong(value).array();
    }

    public static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }


    public static double getMax(double[] doubles) {
        double max = doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if (doubles[i] > max) {
                max = doubles[i];
            }
        }
        return max;
    }

    private static double getNormalizationFactor(double[] doubles, double maxValue) {
        double max = getMax(doubles);
        return max > maxValue ? max / (maxValue + 1) : 1f;
    }

    public static long getUnsignedInt(int x) {
        return x & 0x00000000FFFFFFFFL;
    }

    public static int longToIntWithLossOfPrecision(long value) {
        return (int) (value & 0xFFFFFFFFL);
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
