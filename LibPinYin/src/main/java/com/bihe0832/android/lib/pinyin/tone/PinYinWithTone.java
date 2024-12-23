package com.bihe0832.android.lib.pinyin.tone;

import android.content.Context;
import com.bihe0832.android.lib.aaf.tools.AAFException;
import com.bihe0832.android.lib.chinese.ChineseHelper;
import com.bihe0832.android.lib.file.content.FileContent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import kotlin.text.Regex;

/**
 * 汉字转拼音类
 */
public final class PinYinWithTone {

    public static final String PINYIN_DICT_NAME = "default_pinyin.dict";
    public static final String MUTIL_PINYIN_DICT_NAME = "default_mutil_pinyin.dict";

    private static final List<String> sPinYinDict = new ArrayList<String>();
    private static final Map<String, String> sPinYin = new HashMap<>();
    private static final Map<String, String> sMutilPinYin = new HashMap<>();
    private static final DoubleArrayTrie sDoubleArrayTrie = new DoubleArrayTrie();
    private static final String PINYIN_SEPARATOR = ","; // 拼音分隔符
    private static final char CHINESE_LING = '〇';
    private static final String ALL_UNMARKED_VOWEL = "aeiouv";
    private static final String ALL_MARKED_VOWEL = "āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ"; // 所有带声调的拼音字母


    private PinYinWithTone() {
    }

    public static void init(Context context) {
        addPinyinDict(FileContent.INSTANCE.parseAssetFileToMap(context, PINYIN_DICT_NAME, new Regex("=")));
        addMutilPinyinDict(FileContent.INSTANCE.parseAssetFileToMap(context, MUTIL_PINYIN_DICT_NAME, new Regex("=")));
    }


    /**
     * 将带声调格式的拼音转换为数字代表声调格式的拼音
     *
     * @param pinyinArrayString 带声调格式的拼音
     * @return 数字代表声调格式的拼音
     */
    private static String[] convertPinYinWithToneNumber(String pinyinArrayString) {
        String[] pinyinArray = pinyinArrayString.split(PINYIN_SEPARATOR);
        for (int i = pinyinArray.length - 1; i >= 0; i--) {
            boolean hasMarkedChar = false;
            String originalPinyin = pinyinArray[i].replace("ü", "v"); // 将拼音中的ü替换为v

            for (int j = originalPinyin.length() - 1; j >= 0; j--) {
                char originalChar = originalPinyin.charAt(j);

                // 搜索带声调的拼音字母，如果存在则替换为对应不带声调的英文字母
                if (originalChar < 'a' || originalChar > 'z') {
                    int indexInAllMarked = ALL_MARKED_VOWEL.indexOf(originalChar);
                    int toneNumber = indexInAllMarked % 4 + 1; // 声调数
                    char replaceChar = ALL_UNMARKED_VOWEL.charAt((indexInAllMarked - indexInAllMarked % 4) / 4);
                    pinyinArray[i] = originalPinyin.replace(String.valueOf(originalChar), String.valueOf(replaceChar))
                            + toneNumber;
                    hasMarkedChar = true;
                    break;
                }
            }
            if (!hasMarkedChar) {
                // 找不到带声调的拼音字母说明是轻声，用数字5表示
                pinyinArray[i] = originalPinyin + "5";
            }
        }

        return pinyinArray;
    }

    /**
     * 将带声调格式的拼音转换为不带声调格式的拼音
     *
     * @param pinyinArrayString 带声调格式的拼音
     * @return 不带声调的拼音
     */
    private static String[] convertPinYinWithoutTone(String pinyinArrayString) {
        String[] pinyinArray;
        for (int i = ALL_MARKED_VOWEL.length() - 1; i >= 0; i--) {
            char originalChar = ALL_MARKED_VOWEL.charAt(i);
            char replaceChar = ALL_UNMARKED_VOWEL.charAt((i - i % 4) / 4);
            pinyinArrayString = pinyinArrayString.replace(String.valueOf(originalChar), String.valueOf(replaceChar));
        }
        // 将拼音中的ü替换为v
        pinyinArray = pinyinArrayString.replace("ü", "v").split(PINYIN_SEPARATOR);

        // 去掉声调后的拼音可能存在重复，做去重处理
        LinkedHashSet<String> pinyinSet = new LinkedHashSet<String>();
        Collections.addAll(pinyinSet, pinyinArray);

        return pinyinSet.toArray(new String[pinyinSet.size()]);
    }

    /**
     * 将带声调的拼音格式化为相应格式的拼音
     *
     * @param pinyinString 带声调的拼音
     * @param pinyinFormat 拼音格式：WITH_TONE_NUMBER--数字代表声调，WITHOUT_TONE--不带声调，WITH_TONE_MARK--带声调
     * @return 格式转换后的拼音
     */
    private static String[] formatPinyin(String pinyinString, PinyinFormat pinyinFormat) {
        if (pinyinFormat == PinyinFormat.WITH_TONE_MARK) {
            return pinyinString.split(PINYIN_SEPARATOR);
        } else if (pinyinFormat == PinyinFormat.WITH_TONE_NUMBER) {
            return convertPinYinWithToneNumber(pinyinString);
        } else if (pinyinFormat == PinyinFormat.WITHOUT_TONE) {
            return convertPinYinWithoutTone(pinyinString);
        }
        return new String[0];
    }

    /**
     * 将单个汉字转换为相应格式的拼音
     *
     * @param c 需要转换成拼音的汉字
     * @param pinyinFormat 拼音格式：WITH_TONE_NUMBER--数字代表声调，WITHOUT_TONE--不带声调，WITH_TONE_MARK--带声调
     * @return 汉字的拼音
     */
    public static String[] toPinYin(char c, PinyinFormat pinyinFormat) {
        String pinyin = sPinYin.get(String.valueOf(c));
        if ((pinyin != null) && (!"null".equals(pinyin))) {
            return formatPinyin(pinyin, pinyinFormat);
        }
        return new String[0];
    }

    /**
     * 将单个汉字转换成带声调格式的拼音
     *
     * @param c 需要转换成拼音的汉字
     * @return 字符串的拼音
     */
    public static String[] toPinYin(char c) {
        return toPinYin(c, PinyinFormat.WITH_TONE_MARK);
    }

    /**
     * 将字符串转换成相应格式的拼音
     *
     * @param str 需要转换的字符串
     * @param separator 拼音分隔符
     * @param pinyinFormat 拼音格式：WITH_TONE_NUMBER--数字代表声调，WITHOUT_TONE--不带声调，WITH_TONE_MARK--带声调
     * @return 字符串的拼音
     * @throws AAFException
     */
    public static String toPinYin(String str, String separator, PinyinFormat pinyinFormat)
            throws AAFException {
        str = ChineseHelper.convertToSimplifiedChinese(str);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int strLen = str.length();
        while (i < strLen) {
            String substr = str.substring(i);
            List<Integer> commonPrefixList = sDoubleArrayTrie.commonPrefixSearch(substr);
            if (commonPrefixList.size() == 0) {
                char c = str.charAt(i);
                // 判断是否为汉字或者〇
                if (ChineseHelper.isChinese(c) || c == CHINESE_LING) {
                    String[] pinyinArray = toPinYin(c, pinyinFormat);
                    if (pinyinArray != null) {
                        if (pinyinArray.length > 0) {
                            sb.append(pinyinArray[0]);
                        } else {
                            throw new AAFException("Can't convert to pinyin: " + c);
                        }
                    } else {
                        sb.append(str.charAt(i));
                    }
                } else {
                    sb.append(c);
                }
                i++;
            } else {
                String words = sPinYinDict.get(commonPrefixList.get(commonPrefixList.size() - 1));
                String[] pinyinArray = formatPinyin(sMutilPinYin.get(words), pinyinFormat);
                for (int j = 0, l = pinyinArray.length; j < l; j++) {
                    sb.append(pinyinArray[j]);
                    if (j < l - 1) {
                        sb.append(separator);
                    }
                }
                i += words.length();
            }

            if (i < strLen) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串转换成带声调格式的拼音
     *
     * @param str 需要转换的字符串
     * @param separator 拼音分隔符
     * @return 转换后带声调的拼音
     * @throws AAFException
     */
    public static String toPinYin(String str, String separator) throws AAFException {
        return toPinYin(str, separator, PinyinFormat.WITH_TONE_MARK);
    }

    /**
     * 判断一个汉字是否为多音字
     *
     * @param c 汉字
     * @return 判断结果，是汉字返回true，否则返回false
     */
    public static boolean hasMultiPinyin(char c) {
        String[] pinyinArray = toPinYin(c);
        return pinyinArray != null && pinyinArray.length > 1;
    }

    /**
     * 获取字符串对应拼音的首字母
     *
     * @param str 需要转换的字符串
     * @return 对应拼音的首字母
     * @throws AAFException
     */
    public static String getShortPinyin(String str) throws AAFException {
        String separator = "#"; // 使用#作为拼音分隔符
        StringBuilder sb = new StringBuilder();

        char[] charArray = new char[str.length()];
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);

            // 首先判断是否为汉字或者〇，不是的话直接将该字符返回
            if (!ChineseHelper.isChinese(c) && c != CHINESE_LING) {
                charArray[i] = c;
            } else {
                int j = i + 1;
                sb.append(c);

                // 搜索连续的汉字字符串
                while (j < len && (ChineseHelper.isChinese(str.charAt(j)) || str.charAt(j) == CHINESE_LING)) {
                    sb.append(str.charAt(j));
                    j++;
                }
                String hanziPinyin = toPinYin(sb.toString(), separator, PinyinFormat.WITHOUT_TONE);
                String[] pinyinArray = hanziPinyin.split(separator);
                for (String string : pinyinArray) {
                    charArray[i] = string.charAt(0);
                    i++;
                }
                i--;
                sb.setLength(0);
            }
        }
        return String.valueOf(charArray);
    }

    public static void addPinyinDict(Map<String, String> dict3) {
        sPinYin.putAll(dict3);
    }

    public static void addPinyinDict(String path) {
        addPinyinDict(FileContent.INSTANCE.parseFileToMap(path, new Regex("=")));
    }

    public static void addMutilPinyinDict(Map<String, String> dict3) {
        sMutilPinYin.putAll(dict3);
        sPinYinDict.clear();
        sDoubleArrayTrie.clear();
        for (String word : sMutilPinYin.keySet()) {
            sPinYinDict.add(word);
        }
        Collections.sort(sPinYinDict);
        sDoubleArrayTrie.build(sPinYinDict);
    }

    public static void addMutilPinyinDict(String path) {
        addMutilPinyinDict(FileContent.INSTANCE.parseFileToMap(path, new Regex("=")));
    }


}
