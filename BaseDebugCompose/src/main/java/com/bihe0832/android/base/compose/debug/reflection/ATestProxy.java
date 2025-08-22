package com.bihe0832.android.base.compose.debug.reflection;

import android.util.Log;
import com.bihe0832.android.lib.utils.ReflecterHelper;
import java.lang.reflect.Method;

public class ATestProxy {

    public static void a(final String pv_type) {
        try {
            Class<?> threadClazz = Class.forName("com.bihe0832.android.base.debug.reflection.ATest");
            Log.d("ATest", threadClazz.toString());
            Method method = threadClazz
                    .getMethod("test1", String.class, String.class, String.class, String.class, String.class,
                            String.class, String.class);
            Log.d("ATest", method.toString());
            method.invoke(null, pv_type, "", "", "", "", "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void b(final String login_type, final String error_no) {
        ATest.test3(login_type, error_no);
    }

    public static void c(final String login_type, final String error_no) {
        try {
            Class<?> threadClazz = Class.forName("com.bihe0832.android.base.debug.reflection.ATest");
            Log.d("ATest", threadClazz.toString());
            Method method = threadClazz.getMethod("test3", String.class, String.class);
            Log.d("ATest", method.toString());
            method.invoke(null, login_type, error_no);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void d(final String login_type, final String error_no) {
        try {
            Object args[] = {login_type, error_no};
            ReflecterHelper.invokeStaticMethod(
                    "com.bihe0832.android.test.module.reflection.ATest", "test3", args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
