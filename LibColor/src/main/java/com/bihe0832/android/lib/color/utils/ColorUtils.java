package com.bihe0832.android.lib.color.utils;

import android.graphics.Color;

/**
 * Summary
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/9/5.
 *         Description:
 */
public class ColorUtils {

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int removeAlpha(int color) {
        return color | 0xFF000000;
    }

    public static int getColorDepth(int color) {
        int tempColor = removeAlpha(color);
        int red = (tempColor >> 16) & 0xFF;
        int green = (tempColor >> 8) & 0xFF;
        int blue = tempColor & 0xFF;
        // 计算颜色的亮度（使用标准的相对亮度公式）
        double brightness = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        if (brightness > 254) {
            return 255;
        } else {
            return (int) brightness;
        }
    }

    public static double getColorBrightness(int color) {
        // 将亮度映射到 0（最暗）和 1（最亮）之间的范围
        return getColorDepth(color) / 255.0d;
    }

    public static boolean hasAlpha(int color) {
        int alpha = (color >> 24) & 0xFF;
        return alpha < 255;
    }

    public static int getComplementaryColor(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        int complementaryRed = 255 - red;
        int complementaryGreen = 255 - green;
        int complementaryBlue = 255 - blue;

        return (alpha << 24) | (complementaryRed << 16) | (complementaryGreen << 8) | complementaryBlue;
    }

    public static boolean isLightColor(int color) {
        return getColorBrightness(color) > 0.5f;
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int addAlpha(int alpha, int color) {
        int tempAlpha = alpha;
        if (alpha < 0) {
            tempAlpha = 0;
        }
        if (alpha > 255) {
            tempAlpha = 255;
        }
        return (color & 0x00FFFFFF) | (tempAlpha << 24);
    }

    public static int addAlpha(float alpha, int baseColor) {
        return addAlpha((int) (255 * alpha), baseColor);
    }

    public static final String color2Hex(int color) {
        return color2Hex(255, color, false, true);
    }

    public static final String color2Hex(int alpha, int color) {
        return color2Hex(alpha, color, true, true);
    }

    public static final String color2Hex(int alpha, int color, boolean hasAlpha, boolean isUpperCase) {
        StringBuffer sb = new StringBuffer();
        String alphaSring = Integer.toHexString(alpha);
        String redString = Integer.toHexString(Color.red(color));
        String greenSring = Integer.toHexString(Color.green(color));
        String blueSring = Integer.toHexString(Color.blue(color));
        if (hasAlpha) {
            alphaSring = alphaSring.length() == 1 ? '0' + alphaSring : alphaSring;
        }

        redString = redString.length() == 1 ? '0' + redString : redString;
        greenSring = greenSring.length() == 1 ? '0' + greenSring : greenSring;
        blueSring = blueSring.length() == 1 ? '0' + blueSring : blueSring;

        sb.append("#");
        if (hasAlpha) {
            sb.append(alphaSring);
        }

        sb.append(redString);
        sb.append(greenSring);
        sb.append(blueSring);
        if (isUpperCase) {
            return sb.toString().toUpperCase();
        } else {
            return sb.toString();
        }
    }

    public final int hex2Color(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
