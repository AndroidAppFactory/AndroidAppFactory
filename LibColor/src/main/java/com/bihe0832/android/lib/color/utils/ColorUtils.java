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
        if (alpha < 0 || alpha > 255) {
            return color;
        }

        return (color & 0x00FFFFFF) | (alpha << 24);
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
