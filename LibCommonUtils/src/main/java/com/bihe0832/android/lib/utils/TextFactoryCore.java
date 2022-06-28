package com.bihe0832.android.lib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zixie on 16/11/21.
 */
public class TextFactoryCore {


    /**
     * 过滤字符串的空格
     */
    public static String trimSpace(String str) {
        if (null == str) {
            return null;
        }
        String dest = "";
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(str);
        dest = m.replaceAll("");
        return dest;
    }
}
