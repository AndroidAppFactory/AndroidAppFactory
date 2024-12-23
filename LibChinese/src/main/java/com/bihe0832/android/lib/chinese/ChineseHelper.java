package com.bihe0832.android.lib.chinese;

import android.content.Context;
import com.bihe0832.android.lib.file.content.FileContent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import kotlin.text.Regex;

/**
 * 汉字简繁体转换类
 */
public final class ChineseHelper {

    public static final String CHINESE_DICT_NAME = "default_chinese.dict";

    private static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]";

    private static final Map<String, String> CHINESE_MAP = new HashMap<>();

    public static void init(Context context) {
        CHINESE_MAP.putAll(FileContent.INSTANCE.parseAssetFileToMap(context, CHINESE_DICT_NAME, new Regex("=")));
    }

    public static void addChineseDict(String path) {
        CHINESE_MAP.putAll(FileContent.INSTANCE.parseFileToMap(path, new Regex("=")));
    }

    /**
     * 将单个繁体字转换为简体字
     *
     * @param c 需要转换的繁体字
     * @return 转换后的简体字
     */
    public static char convertToSimplifiedChinese(char c) {
        String simplifiedChinese = CHINESE_MAP.get(String.valueOf(c));
        if (simplifiedChinese != null) {
            return simplifiedChinese.charAt(0);
        }
        return c;
    }

    /**
     * 将单个简体字转换为繁体字
     *
     * @param c 需要转换的简体字
     * @return 转换后的繁字体
     */
    public static char convertToTraditionalChinese(char c) {
        String simplifiedChinese = String.valueOf(c);
        for (Entry<String, String> entry : CHINESE_MAP.entrySet()) {
            if (entry.getValue().equals(simplifiedChinese)) {
                return entry.getKey().charAt(0);
            }
        }

        return c;
    }

    /**
     * 将繁体字转换为简体字
     *
     * @param str 需要转换的繁体字
     * @return 转换后的简体体
     */
    public static String convertToSimplifiedChinese(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            sb.append(convertToSimplifiedChinese(c));
        }
        return sb.toString();
    }

    /**
     * 将简体字转换为繁体字
     *
     * @param str 需要转换的简体字
     * @return 转换后的繁字体
     */
    public static String convertToTraditionalChinese(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            sb.append(convertToTraditionalChinese(c));
        }
        return sb.toString();
    }

    /**
     * 判断某个字符是否为繁体字
     *
     * @param c 需要判断的字符
     * @return 是繁体字返回true，否则返回false
     */
    public static boolean isTraditionalChinese(char c) {
        return CHINESE_MAP.containsKey(String.valueOf(c));
    }

    /**
     * 判断某个字符是否为汉字
     *
     * @param c 需要判断的字符
     * @return 是汉字返回true，否则返回false
     */
    public static boolean isChinese(char c) {
        return '〇' == c || String.valueOf(c).matches(CHINESE_REGEX);
    }

    /**
     * 判断字符串中是否包含中文
     *
     * @param str 字符串
     * @return 包含汉字返回true，否则返回false
     */
    public static boolean containsChinese(String str) {
        for (int i = 0, len = str.length(); i < len; i++) {
            if (isChinese(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

}
