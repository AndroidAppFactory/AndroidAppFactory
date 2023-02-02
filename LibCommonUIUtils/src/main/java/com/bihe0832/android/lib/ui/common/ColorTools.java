package com.bihe0832.android.lib.ui.common;

import android.graphics.Color;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/8/5.
 * Description: Description
 */
public class ColorTools {

    private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal();

    public static int getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        return a + rgb;
    }

    public static boolean isLightColor(int color) {
        return calculateLuminance(color) > 0.5f;
    }

    private static double calculateLuminance(int color){
        double[] result = getTempDouble3Array();
        colorToXYZ(color, result);
        return result[1] / 100.0D;
    }
    private static double[] getTempDouble3Array() {
        double[] result = (double[]) TEMP_ARRAY.get();
        if (result == null) {
            result = new double[3];
            TEMP_ARRAY.set(result);
        }

        return result;
    }

    public static void colorToXYZ(int color, double[] outXyz) {
        RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
    }

    public static void RGBToXYZ(int r, int g, int b, double[] outXyz) {
        if (outXyz.length != 3) {
            throw new IllegalArgumentException("outXyz must have a length of 3.");
        } else {
            double sr = (double) r / 255.0D;
            sr = sr < 0.04045D ? sr / 12.92D : Math.pow((sr + 0.055D) / 1.055D, 2.4D);
            double sg = (double) g / 255.0D;
            sg = sg < 0.04045D ? sg / 12.92D : Math.pow((sg + 0.055D) / 1.055D, 2.4D);
            double sb = (double) b / 255.0D;
            sb = sb < 0.04045D ? sb / 12.92D : Math.pow((sb + 0.055D) / 1.055D, 2.4D);
            outXyz[0] = 100.0D * (sr * 0.4124D + sg * 0.3576D + sb * 0.1805D);
            outXyz[1] = 100.0D * (sr * 0.2126D + sg * 0.7152D + sb * 0.0722D);
            outXyz[2] = 100.0D * (sr * 0.0193D + sg * 0.1192D + sb * 0.9505D);
        }
    }
}
