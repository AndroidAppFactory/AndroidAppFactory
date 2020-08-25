package com.bihe0832.android.lib.utils.os;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.bihe0832.android.lib.log.ZLog;

import java.lang.reflect.Method;

public class DisplayUtil {
    public static final int NAV_GESTURE = 0;
    public static final int NAV_VIRTUAL = 1;
    public static final int NAV_BUTTON = 2;

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(Activity act) {
        View v = act.getCurrentFocus();
        if(v != null && v.getWindowToken() != null) {
            InputMethodManager manager = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            boolean isOpen = manager.isActive();
            if(isOpen) {
                manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 判断虚拟导航栏是否显示
     *
     * @param context 上下文对象
     * @param window  当前窗口
     * @return true(显示虚拟导航栏)，false(不显示或不支持虚拟导航栏)
     */
    public static boolean checkNavigationBarShow(Context context,Window window) {
        boolean show;
        Display display = window.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);

        View decorView = window.getDecorView();
        Configuration conf = context.getResources().getConfiguration();
        if (Configuration.ORIENTATION_LANDSCAPE == conf.orientation) {
            View contentView = decorView.findViewById(android.R.id.content);
            ZLog.d("checkNavigationBarShow point.x:" + point.x + ",contentView.getWidth():" + contentView.getWidth());
            show = (point.x != contentView.getWidth());
        } else {
            Rect rect = new Rect();
            decorView.getWindowVisibleDisplayFrame(rect);
            ZLog.d("checkNavigationBarShow point.y:" + point.y + ",rect.bottom:" + rect.bottom);
            show = (rect.bottom != point.y);
        }
        return show;
    }

    public static boolean checkDeviceHasNavigationBar(Context context, Window window) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = window.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);  // app绘制区域
            display.getRealSize(realSize);
            ZLog.d("checkDeviceHasNavigationBar realSize.y:" + realSize.y + ",size.y:" + size.y);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);// 判断是否存在物理按键
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
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

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    @Deprecated
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获得实际可以用的屏幕宽度
     * 如果有状态栏，则减去
     *
     * @param act
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
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context,float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context,float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context,float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
     */
    public static int px2sp(Context context, int px) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / scale + 0.5f);
    }
    public static int getDimension(Context context,int resourceId) {
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static int getNavigationBarType(Context context, Window window) {
        if (hasNavigationBar()) {
            if (detectNavigationBarIsShow(context, window)) {
                return NAV_VIRTUAL;
            } else {
                return NAV_GESTURE;
            }
        } else {
            return NAV_BUTTON;
        }
    }

    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public static boolean hasNavigationBar() {
        boolean haveNav = false;
        try {
            Class<?> windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal");
            Method getWmServiceMethod =
                    windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService");
            getWmServiceMethod.setAccessible(true);
            Object iWindowManager = getWmServiceMethod.invoke(null);

            Class<?> iWindowManagerClass = iWindowManager.getClass();
            Method hasNavBarMethod = iWindowManagerClass.getDeclaredMethod("hasNavigationBar");
            hasNavBarMethod.setAccessible(true);
            haveNav = (boolean) hasNavBarMethod.invoke(iWindowManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return haveNav;
    }

    public static boolean detectNavigationBarIsShow(Context context, Window window) {
        ViewGroup vp = (ViewGroup) window.getDecorView();
        Resources resources = context.getResources();
        for (int i = 0; i < vp.getChildCount(); i++) {
            vp.getChildAt(i).getContext().getPackageName();
            if (vp.getChildAt(i).getId() != -1
                    && "navigationBarBackground".equals(resources.getResourceEntryName(vp.getChildAt(i).getId()))
                    ) {
                return true;
            }
        }
        return false;
    }

}
