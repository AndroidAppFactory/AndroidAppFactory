package com.bihe0832.android.lib.ui.common;

import android.content.res.Resources;
import android.util.Log;


public class Res {

    private static final String TAG = "Res";

    private static int reflectResouce(Resources r, String type, String name, String pkg) {
        if (type == null || name == null) {
            Log.d(TAG, "type || name null");
            return -1;
        }
        try {
            return r.getIdentifier(name, type, pkg);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getIdentifier exception");
            return -1;
        }
    }

    public static int string(Resources r, String name, String pkg) {
        return reflectResouce(r, "string", name, pkg);
    }

    public static int drawable(Resources r, String name, String pkg) {
        return reflectResouce(r, "drawable", name, pkg);
    }

    public static int mipmap(Resources r, String name, String pkg) {
        return reflectResouce(r, "mipmap", name, pkg);
    }

    public static int layout(Resources r, String name, String pkg) {
        return reflectResouce(r, "layout", name, pkg);
    }

    public static int id(Resources r, String name, String pkg) {
        return reflectResouce(r, "id", name, pkg);
    }

    public static int style(Resources r, String name, String pkg) {
        return reflectResouce(r, "style", name, pkg);
    }

    public static int color(Resources r, String name, String pkg) {
        return reflectResouce(r, "color", name, pkg);
    }

    public static int array(Resources r, String name, String pkg) {
        return reflectResouce(r, "array", name, pkg);
    }

}
