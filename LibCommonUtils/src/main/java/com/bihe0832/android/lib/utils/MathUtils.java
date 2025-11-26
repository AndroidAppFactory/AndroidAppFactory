package com.bihe0832.android.lib.utils;

import com.bihe0832.android.lib.log.ZLog;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Random;

/**
 * 数学计算工具类
 * 
 * @author zixie code@bihe0832.com
 * Created on 2025-01-15.
 * 
 * Description: 提供常用的数学计算功能，包括：
 * - 随机数生成（区间随机数、不重复随机采样）
 * - 数值比较（最大值、最小值）
 * - 百分比计算和格式化
 * - 高精度数值运算
 */
public class MathUtils {

    private static final String TAG = "MathUtils";

    /**
     * 获取两个数字区间的随机数（包含边界）
     *
     * @param min 下限（包含）
     * @param max 上限（包含）
     * @return 区间内的随机整数，如果min > max则返回min
     */
    public static int getRandNumByLimit(int min, int max) {
        if (min > max) {
            ZLog.e(TAG, "getRandNumByLimit: min > max, min=" + min + ", max=" + max);
            return min;
        }
        return (int) Math.round(Math.random() * (max - min) + min);
    }

    /**
     * 从非负整数 [0, N) 中等概率不重复地选择 K 个数
     * 使用水塘抽样算法（Reservoir Sampling）实现
     *
     * @param N 总数量（必须 > 0）
     * @param K 选择数量（必须满足 0 < K <= N）
     * @return 包含K个不重复随机数的数组，如果参数无效则返回空数组
     */
    public static int[] sample(int N, int K) {
        if (N <= 0 || K <= 0 || K > N) {
            ZLog.e(TAG, "sample: invalid parameters, N=" + N + ", K=" + K);
            return new int[0];
        }
        
        Random random = new Random();
        int result[] = new int[K];
        int selection = K, remains = N;
        for (int i = 0; i < N; i++) {
            if (random.nextInt(remains) < selection) {
                result[K - selection] = i;
                selection--;
            }
            remains--;
        }
        return result;
    }

    /**
     * 获取多个整数中的最小值
     *
     * @param params 可变参数，至少需要一个参数
     * @return 最小值，如果参数为空则返回Integer.MAX_VALUE
     */
    public static int getMin(int... params) {
        if (params == null || params.length == 0) {
            ZLog.e(TAG, "getMin: params is null or empty");
            return Integer.MAX_VALUE;
        }
        
        int min = params[0];
        for (int para : params) {
            if (para < min) {
                min = para;
            }
        }
        return min;
    }

    /**
     * 获取多个整数中的最大值
     *
     * @param params 可变参数，至少需要一个参数
     * @return 最大值，如果参数为空则返回Integer.MIN_VALUE
     */
    public static int getMax(int... params) {
        if (params == null || params.length == 0) {
            ZLog.e(TAG, "getMax: params is null or empty");
            return Integer.MIN_VALUE;
        }
        
        int max = params[0];
        for (int para : params) {
            if (para > max) {
                max = para;
            }
        }
        return max;
    }

    /**
     * 计算百分比（int类型）
     *
     * @param numerator 分子
     * @param denominator 分母
     * @param scale 小数位数
     * @return 百分比值（0.0-1.0），分母为0时返回0
     */
    public static float getFormatPercent(int numerator, int denominator, int scale) {
        BigDecimal numeratorBig = new BigDecimal(numerator);
        BigDecimal denominatorBig = new BigDecimal(denominator);
        return getFormatPercent(numeratorBig, denominatorBig, scale);
    }

    /**
     * 计算百分比（long类型）
     *
     * @param numerator 分子
     * @param denominator 分母
     * @param scale 小数位数
     * @return 百分比值（0.0-1.0），分母为0时返回0
     */
    public static float getFormatPercent(long numerator, long denominator, int scale) {
        BigDecimal numeratorBig = new BigDecimal(numerator);
        BigDecimal denominatorBig = new BigDecimal(denominator);
        return getFormatPercent(numeratorBig, denominatorBig, scale);
    }

    /**
     * 计算百分比（double类型）
     *
     * @param numerator 分子
     * @param denominator 分母
     * @param scale 小数位数
     * @return 百分比值（0.0-1.0），分母为0时返回0
     */
    public static float getFormatPercent(double numerator, double denominator, int scale) {
        BigDecimal numeratorBig = new BigDecimal(numerator);
        BigDecimal denominatorBig = new BigDecimal(denominator);
        return getFormatPercent(numeratorBig, denominatorBig, scale);
    }

    /**
     * 计算百分比（BigDecimal类型，核心实现）
     * 注意：返回值范围为 [0.0, 1.0]，而非 [0, 100]
     *
     * @param numeratorBig 分子
     * @param denominatorBig 分母
     * @param scale 小数位数（建议范围：0-10）
     * @return 百分比值（0.0-1.0），分母为0时返回0，负数时返回0
     */
    public static float getFormatPercent(BigDecimal numeratorBig, BigDecimal denominatorBig, int scale) {
        float percent = 0f;
        
        // 分母为0，返回0
        if (denominatorBig.equals(BigDecimal.ZERO)) {
            ZLog.e(TAG, "getFormatPercent: denominator is zero");
            return percent;
        }
        
        // 参数校验
        if (scale < 0) {
            ZLog.e(TAG, "getFormatPercent: scale < 0, scale=" + scale);
            scale = 0;
        }
        
        try {
            // 使用ROUND_DOWN模式进行除法运算
            percent = numeratorBig.divide(denominatorBig, scale, BigDecimal.ROUND_DOWN)
                    .floatValue();
        } catch (Exception e) {
            ZLog.e(TAG, "getFormatPercent: divide failed, error: " + e.getMessage());
            // 降级处理：使用double除法
            percent = (float) (numeratorBig.doubleValue() / denominatorBig.doubleValue());
        }
        
        // 负数处理
        if (percent < 0) {
            percent = 0f;
        }
        
        // 修正精度问题：当结果>=1但分母>分子时，减去最小精度单位
        if (percent >= 1 && denominatorBig.compareTo(numeratorBig) == 1) {
            return (float) (percent - Math.pow(0.1, scale));
        }
        
        return percent;
    }

    /**
     * 将百分比值格式化为百分比字符串
     * 例如：0.1234 -> "12.34%" (scale=2)
     *
     * @param percent 百分比值（0.0-1.0）
     * @param scale 小数位数
     * @return 格式化后的百分比字符串（如："12.34%"）
     */
    public static String getFormatPercentDesc(float percent, int scale) {
        if (scale < 0) {
            ZLog.e(TAG, "getFormatPercentDesc: scale < 0, scale=" + scale);
            scale = 0;
        }
        
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(percent);
    }
}
