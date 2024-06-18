package com.bihe0832.android.lib.utils;

import android.graphics.Point;

import com.bihe0832.android.lib.log.ZLog;
import java.util.Locale;

/**
 * 坐标点工具类：主要是将宽高原始坐标点到宽高变化之后目标坐标点之间进行转换
  */
public final class PointUtils {

    private PointUtils() {
        throw new AssertionError();
    }

    /**
     * 转换坐标：将原始 point 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
     *
     * @param point      原始坐标点
     * @param srcWidth   原始宽度
     * @param srcHeight  原始高度
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     * @return 转换之后的坐标点
     * @return
     */
    public static Point transform(Point point, int srcWidth, int srcHeight, int destWidth, int destHeight) {
        return transform(point, srcWidth, srcHeight, destWidth, destHeight, false);
    }

    /**
     * 转换坐标：将原始 point 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
     *
     * @param point      原始坐标点
     * @param srcWidth   原始宽度
     * @param srcHeight  原始高度
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     * @param isFit      是否自适应，如果为 true 表示：宽或高自适应铺满，如果为 false 表示：填充铺满（可能会出现裁剪）
     * @return 转换之后的坐标点
     */
    public static Point transform(Point point, int srcWidth, int srcHeight, int destWidth, int destHeight, boolean isFit) {
        return transform(point.x, point.y, srcWidth, srcHeight, destWidth, destHeight, isFit);
    }

    /**
     * 转换坐标：将原始 x，y 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
     *
     * @param x          原始X坐标
     * @param y          原值Y坐标
     * @param srcWidth   原始宽度
     * @param srcHeight  原始高度
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     * @return 转换之后的坐标点
     */
    public static Point transform(int x, int y, int srcWidth, int srcHeight, int destWidth, int destHeight) {
        return transform(x, y, srcWidth, srcHeight, destWidth, destHeight, false);
    }

    /**
     * 转换坐标：将原始 x，y 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
     *
     * @param x          原始X坐标
     * @param y          原值Y坐标
     * @param srcWidth   原始宽度
     * @param srcHeight  原始高度
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     * @param isFit      是否自适应，如果为 true 表示：宽或高自适应铺满，如果为 false 表示：填充铺满（可能会出现裁剪）
     * @return 转换之后的坐标点
     */
    public static Point transform(int x, int y, int srcWidth, int srcHeight, int destWidth, int destHeight, boolean isFit) {
        ZLog.d(String.format(Locale.getDefault(), "transform: %d,%d | %d,%d", srcWidth, srcHeight, destWidth, destHeight));
        float widthRatio = destWidth * 1.0f / srcWidth;
        float heightRatio = destHeight * 1.0f / srcHeight;
        Point point = new Point();
        if (isFit) {
            // 宽或高自适应铺满
            float ratio = Math.min(widthRatio, heightRatio);
            float left = Math.abs(srcWidth * ratio - destWidth) / 2;
            float top = Math.abs(srcHeight * ratio - destHeight) / 2;
            point.x = (int) (x * ratio + left);
            point.y = (int) (y * ratio + top);
        } else {
            // 填充铺满（可能会出现裁剪）
            float ratio = Math.max(widthRatio, heightRatio);
            float left = Math.abs(srcWidth * ratio - destWidth) / 2;
            float top = Math.abs(srcHeight * ratio - destHeight) / 2;
            point.x = (int) (x * ratio - left);
            point.y = (int) (y * ratio - top);
        }

        return point;
    }
}
