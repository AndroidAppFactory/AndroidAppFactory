package com.bihe0832.android.lib.utils.time;


import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/5/26.
 * Description: Description
 */
public class SimpleDateFormatFactory {
    private static final ThreadLocal<Map<String, SimpleDateFormat>> pool = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        protected Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<String, SimpleDateFormat>();
        }
    };

    public static SimpleDateFormat getSimpleDateFormat(String pattern, Locale locale) {
        String key = locale.toString() + " " + pattern;
        Map<String, SimpleDateFormat> formatMap = pool.get();
        SimpleDateFormat sdf = formatMap.get(key);
        if (sdf == null) {
            sdf = new SimpleDateFormat(pattern, locale);
            formatMap.put(key, sdf);
        }
        return sdf;
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return getSimpleDateFormat(pattern, Locale.US);
    }
}