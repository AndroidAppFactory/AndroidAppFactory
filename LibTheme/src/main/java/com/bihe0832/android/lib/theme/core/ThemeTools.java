package com.bihe0832.android.lib.theme.core;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by liupei on 2018/3/16.
 */

public class ThemeTools {
    public static int[] getThemeResid(Context context, int[] attrs) {
        int[] resIds = new int[]{attrs.length};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        for (int i = 0; i < typedArray.length(); i++) {
            int resourceId = typedArray.getResourceId(i, 0);
            resIds[i] = resourceId;
        }
        typedArray.recycle();
        return resIds;
    }
}
