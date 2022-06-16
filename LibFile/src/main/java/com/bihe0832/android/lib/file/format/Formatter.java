/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/6/16 下午3:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/6/16 下午3:09
 *
 */

package com.bihe0832.android.lib.file.format;

import android.content.Context;

import java.util.Locale;

/**
 * @author zixie code@bihe0832.com
 * Created on 2022/6/16.
 * Description: Description
 */
public class Formatter {
    /**
     * get file format size * * @param context context * @param roundedBytes file size * @return file format size (like 2.12k)
     */
    public static String formatFileSize(Context context, long roundedBytes) {
        return formatFileSize(context, roundedBytes, false, Locale.US);
    }

    public static String formatFileSize(Context context, long roundedBytes, Locale locale) {
        return formatFileSize(context, roundedBytes, false, locale);
    }


    private static String formatFileSize(Context context, long roundedBytes, boolean shorter, Locale locale) {
        if (context == null) {
            return "";
        }
        float result = roundedBytes;
        String suffix = "B";
        if (result > 900) {
            suffix = "KB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "GB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "TB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = "PB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format(locale, "%.2f", result);
        } else if (result < 10) {
            if (shorter) {
                value = String.format(locale, "%.1f", result);
            } else {
                value = String.format(locale, "%.2f", result);
            }
        } else if (result < 100) {
            if (shorter) {
                value = String.format(locale, "%.0f", result);
            } else {
                value = String.format(locale, "%.2f", result);
            }
        } else {
            value = String.format(locale, "%.0f", result);
        }
        return String.format("%s%s", value, suffix);
    }

}
