package com.bihe0832.android.lib.pinyin;

import com.bihe0832.android.lib.chinese.ChineseHelper;
import com.bihe0832.android.lib.pinyin.bean.PinyinDict;
import com.bihe0832.android.lib.pinyin.bean.PinyinRules;
import com.bihe0832.android.lib.pinyin.code.PinyinCode1;
import com.bihe0832.android.lib.pinyin.code.PinyinCode2;
import com.bihe0832.android.lib.pinyin.code.PinyinCode3;
import com.bihe0832.android.lib.pinyin.code.PinyinData;
import com.bihe0832.android.lib.pinyin.core.Engine;
import com.bihe0832.android.lib.pinyin.core.SegmentationSelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ahocorasick.trie.Trie;

public final class PinYinTools {


    static Trie mTrieDict = null;
    static SegmentationSelector mSelector = null;
    static List<PinyinDict> mPinyinDicts = null;

    private PinYinTools() {
    }

    /**
     * 使用 {@link PinYinConfig} 初始化Pinyin。
     *
     * @param config 相应的设置，传入null则会清空所有的词典
     */
    public static void init(PinYinConfig config) {
        if (config == null) {
            // 清空设置
            mPinyinDicts = null;
            mTrieDict = null;
            mSelector = null;
            return;
        }

        if (!config.valid()) {
            // 忽略无效Config
            return;
        }
        mPinyinDicts = Collections.unmodifiableList(config.getPinyinDicts());
        mTrieDict = PinyinDict.dictsToTrie(config.getPinyinDicts());
        mSelector = config.getSelector();
    }

    /**
     * 向Pinyin中追加词典。 注意: 若有多个词典，推荐使用性能更优的 {@link PinYinTools#init(PinYinConfig)} 初始化Pinyin。
     *
     * @param dict 输入的词典
     */
    public static void addPinyinDict(PinyinDict dict) {
        if (dict == null || dict.words() == null || dict.words().isEmpty()) {
            // 无效字典
            return;
        }
        init(new PinYinConfig(mPinyinDicts).with(dict));
    }

    /**
     * 返回新的{@link PinYinConfig} 对象
     *
     * @return 新的Config对象
     */
    public static PinYinConfig newConfig() {
        return new PinYinConfig(null);
    }

    /**
     * 将输入字符串转为拼音，转换过程中会使用之前设置的用户词典，以字符为单位插入分隔符
     *
     * 例: "hello:中国"  在separator为","时，输出： "h,e,l,l,o,:,ZHONG,GUO,!"
     *
     * @param str 输入字符串
     * @param separator 分隔符
     * @return 中文转为拼音的字符串
     */
    public static String toPinyin(String str, String separator) {
        return Engine.toPinyin(str, mTrieDict, mPinyinDicts, separator, mSelector);
    }


    /**
     * 将输入字符串转为拼音，转换过程中会使用之前设置的用户词典，以字符为单位插入分隔符
     *
     * 例: "hello:中国!"  在separator为","时，输出： "h,e,l,l,o,:,ZHONG,GUO,!"
     *
     * @param str 输入字符串
     * @param separator 分隔符
     * @param rules 自定义的规则，具有最高优先级
     * @return 中文转为拼音的字符串
     */
    public static String toPinyin(String str, String separator, PinyinRules rules) {
        if (rules != null) {
            List<PinyinDict> dicts = new ArrayList();
            dicts.add(rules.toPinyinMapDict());
            if (mPinyinDicts != null) {
                dicts.addAll(mPinyinDicts);
            }
            PinYinConfig config = new PinYinConfig(dicts);

            return Engine.toPinyin(str, config, separator);
        } else {
            return toPinyin(str, separator);
        }
    }

    /**
     * 将输入字符转为拼音
     *
     * @param c 输入字符
     * @return return pinyin if c is chinese in uppercase, String.valueOf(c) otherwise.
     */
    public static String toPinyin(char c) {
        if (ChineseHelper.isChinese(c)) {
            if (c == PinyinData.CHAR_12295) {
                return PinyinData.PINYIN_12295;
            } else {
                return PinyinData.PINYIN_TABLE[getPinyinCode(c)];
            }
        } else {
            return String.valueOf(c);
        }
    }

    /**
     * 将输入字符转为拼音
     *
     * @param c 输入字符
     * @param rules 自定义规则，具有最高优先级
     * @return return pinyin if c is chinese in uppercase, String.valueOf(c) otherwise.
     */
    public static String toPinyin(char c, PinyinRules rules) {
        if (rules != null && rules.toPinyin(c) != null) {
            return rules.toPinyin(c);
        } else {
            return toPinyin(c);
        }
    }


    private static int getPinyinCode(char c) {
        int offset = c - PinyinData.MIN_VALUE;
        if (0 <= offset && offset < PinyinData.PINYIN_CODE_1_OFFSET) {
            return decodeIndex(PinyinCode1.PINYIN_CODE_PADDING, PinyinCode1.PINYIN_CODE, offset);
        } else if (PinyinData.PINYIN_CODE_1_OFFSET <= offset && offset < PinyinData.PINYIN_CODE_2_OFFSET) {
            return decodeIndex(PinyinCode2.PINYIN_CODE_PADDING, PinyinCode2.PINYIN_CODE,
                    offset - PinyinData.PINYIN_CODE_1_OFFSET);
        } else {
            return decodeIndex(PinyinCode3.PINYIN_CODE_PADDING, PinyinCode3.PINYIN_CODE,
                    offset - PinyinData.PINYIN_CODE_2_OFFSET);
        }
    }

    private static short decodeIndex(byte[] paddings, byte[] indexes, int offset) {
        //CHECKSTYLE:OFF
        int index1 = offset / 8;
        int index2 = offset % 8;
        short realIndex;
        realIndex = (short) (indexes[offset] & 0xff);
        //CHECKSTYLE:ON
        if ((paddings[index1] & PinyinData.BIT_MASKS[index2]) != 0) {
            realIndex = (short) (realIndex | PinyinData.PADDING_MASK);
        }
        return realIndex;
    }
}
