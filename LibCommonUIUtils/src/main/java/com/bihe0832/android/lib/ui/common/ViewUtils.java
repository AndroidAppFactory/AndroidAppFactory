package com.bihe0832.android.lib.ui.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Dimension;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class ViewUtils {

    private static String TAG = "com.jygaming.android.lib.ui.utils.ViewUtils";
    private static float DEVICE_DENSITY = 0;
    private static int statusBarHeight;//状态栏的高度

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dip2px(Context context, @Dimension(unit = Dimension.DP) float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static float sp2px(Context context, @Dimension(unit = Dimension.SP) float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, @Dimension float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据某个密度从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(@Dimension float pxValue, float density) {
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 获取一个View的截图Bitmap
     */
    public static Bitmap loadBitmapFromView(View v) {
        if (v == null) {
            return null;
        }
        Bitmap screenshot;
        screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(screenshot);
        c.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(c);
        return screenshot;
    }

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight == 0) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return statusBarHeight;
    }

    /**
     * 获得实际可以用的屏幕宽度
     * 如果有状态栏，则减去
     *
     * @param context
     * @return
     */
    public static int getScreenAvailabe(Activity act) {
        WindowManager.LayoutParams attrs = act.getWindow().getAttributes();
        if (attrs.flags == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return getScreenHeight(act);
        } else {
            return getScreenHeight(act) - getStatusBarHeight(act);
        }

    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }



    /**
     * 虚拟按键的情况下获取物理分辨率
     *
     * @param context
     * @return 屏幕宽度
     */
    public static int getRealScreenSizeX(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point outSize = new Point();
            display.getRealSize(outSize);
            return outSize.x;
        } else {
            return getScreenWidth(context);
        }
    }

    /**
     * 虚拟按键的情况下获取物理分辨率
     *
     * @param context
     * @return 屏幕高度
     */
    public static int getRealScreenSizeY(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point outSize = new Point();
            display.getRealSize(outSize);
            return outSize.y;
        } else {
            return getScreenHeight(context);
        }

    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public  static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static Drawable getUsefulDrawable(Context context, int id){
        Drawable drawable = context.getResources().getDrawable(id);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());
        return drawable;
    }
}
