package com.bihe0832.android.lib.immersion;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.bihe0832.android.lib.device.ManufacturerUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author：luck
 * @data：2018/3/28 下午1:01
 * @描述: 沉浸式
 */

public class LightStatusBarUtils {

    public static void setLightStatusBar(Activity activity, boolean isTransStatusBar, boolean dark) {
        switch (RomType.getRomType()) {
            case RomType.MIUI:
                if (ManufacturerUtil.getMiuiVersionCode() >= 7) {
                    setAndroidNativeLightStatusBar(activity, isTransStatusBar,
                            dark);
                } else {
                    setMIUILightStatusBar(activity, isTransStatusBar, dark);
                }
                break;

            case RomType.FLYME:
                setFlymeLightStatusBar(activity, isTransStatusBar, dark);
                break;

            case RomType.ANDROID_NATIVE:
                setAndroidNativeLightStatusBar(activity, isTransStatusBar,
                        dark);
                break;

            case RomType.NA:
                // N/A do nothing
                break;
        }
    }


    private static boolean setMIUILightStatusBar(Activity activity, boolean isTransStatusBar, boolean darkmode) {
        initStatusBarStyle(activity);

        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            setAndroidNativeLightStatusBar(activity, isTransStatusBar,
                    darkmode);
        }
        return false;
    }

    private static boolean setFlymeLightStatusBar(Activity activity, boolean isTransStatusBar, boolean dark) {
        boolean result = false;
        if (activity != null) {
            initStatusBarStyle(activity);
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;

                if (RomType.getFlymeVersion() >= 7) {
                    setAndroidNativeLightStatusBar(activity, isTransStatusBar,
                            dark);
                }
            } catch (Exception e) {
                setAndroidNativeLightStatusBar(activity, isTransStatusBar,
                        dark);
            }
        }
        return result;
    }

    @TargetApi(11)
    private static void setAndroidNativeLightStatusBar(Activity activity, boolean isTransStatusBar,
            boolean isDarkStatusBarIcon) {

        try {
            if (isTransStatusBar) {
                Window window = activity.getWindow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isDarkStatusBarIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    }
                }
            } else {
                View decor = activity.getWindow().getDecorView();
                if (isDarkStatusBarIcon && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    // We want to change tint color to white again.
                    // You can also record the flags in advance so that you can turn UI back completely if
                    // you have set other flags before, such as translucent or full screen.
                    decor.setSystemUiVisibility(0);
                }
            }
        } catch (Exception e) {
        }
    }

    private static void initStatusBarStyle(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

    }
}
