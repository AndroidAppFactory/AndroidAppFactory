package com.bihe0832.android.lib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本处理核心工具类
 * 
 * @author zixie code@bihe0832.com
 * Created on 2016-11-21.
 * 
 * Description: 提供常用的文本处理功能，包括：
 * - 字符串空格过滤
 * - 正则表达式特殊字符转义
 * - 字符串空白检查
 * - 安全的正则表达式模式创建
 */
public class TextFactoryCore {

    /**
     * 过滤字符串中的所有空白字符（空格、制表符、换行符等）
     * 
     * @param str 待处理的字符串
     * @return 过滤后的字符串，如果输入为null则返回null
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

    /**
     * 创建安全的正则表达式模式
     * 
     * 自动转义输入字符串中的正则表达式特殊字符，避免正则表达式语法错误。
     * 
     * @param key 待转换为正则表达式的字符串
     * @return 编译后的正则表达式模式
     */
    public static Pattern getSafePattern(String key) {
        return Pattern.compile(escapeExprSpecialWord(key));
    }

    /**
     * 转义正则表达式特殊字符
     * 
     * 将字符串中的正则表达式特殊字符进行转义，包括：
     * \ $ ( ) * + . [ ] ? ^ { } | 
     * 
     * 使用场景：当需要将用户输入的字符串作为正则表达式的字面量匹配时使用。
     *
     * @param keyword 待转义的字符串
     * @return 转义后的字符串，如果输入为空则返回原字符串
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 检查字符串是否为空白
     * 
     * 判断条件：
     * - 字符串为 null
     * - 字符串长度为 0
     * - 字符串只包含空白字符（空格、制表符、换行符等）
     *
     * @param str 待检查的字符串
     * @return 如果字符串为空白则返回 true，否则返回 false
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查字符串是否不为空白
     * 
     * 与 isBlank 方法相反，判断字符串是否包含非空白字符。
     *
     * @param str 待检查的字符串
     * @return 如果字符串不为空白则返回 true，否则返回 false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
