package com.bihe0832.android.lib.pinyin.core;


import android.text.TextUtils;
import com.bihe0832.android.lib.pinyin.PinYinConfig;
import com.bihe0832.android.lib.pinyin.PinYinTools;
import com.bihe0832.android.lib.pinyin.bean.PinyinDict;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;


public final class Engine {

    static final EmitComparator EMIT_COMPARATOR = new EmitComparator();

    private Engine() {
        //no instance
    }

    public static String toPinyin(final String inputStr, PinYinConfig config, String separator) {
        List<PinyinDict> pinyinDicts = Collections.unmodifiableList(config.getPinyinDicts());
        Trie trie = PinyinDict.dictsToTrie(config.getPinyinDicts());
        SegmentationSelector selector = config.getSelector();
        return toPinyin(inputStr, trie, pinyinDicts, separator, selector);
    }

    public static String toPinyin(final String inputStr, final Trie trie, final List<PinyinDict> pinyinDictList,
            final String separator, final SegmentationSelector selector) {
        if (TextUtils.isEmpty(inputStr)) {
            return inputStr;
        }

        if (trie == null || selector == null) {
            // 没有提供字典或选择器，按单字符转换输出
            StringBuffer resultPinyinStrBuf = new StringBuffer();
            for (int i = 0; i < inputStr.length(); i++) {
                resultPinyinStrBuf.append(PinYinTools.toPinyin(inputStr.charAt(i)));
                if (i != inputStr.length() - 1) {
                    resultPinyinStrBuf.append(separator);
                }
            }
            return resultPinyinStrBuf.toString();
        }

        List<Emit> selectedEmits = selector.select(trie.parseText(inputStr));

        Collections.sort(selectedEmits, EMIT_COMPARATOR);

        StringBuffer resultPinyinStrBuf = new StringBuffer();

        int nextHitIndex = 0;

        for (int i = 0; i < inputStr.length(); ) {
            // 首先确认是否有以第i个字符作为begin的hit
            if (nextHitIndex < selectedEmits.size() && i == selectedEmits.get(nextHitIndex).getStart()) {
                // 有以第i个字符作为begin的hit
                String[] fromDicts = pinyinFromDict(selectedEmits.get(nextHitIndex).getKeyword(), pinyinDictList);
                for (int j = 0; j < fromDicts.length; j++) {
                    resultPinyinStrBuf.append(fromDicts[j].toUpperCase());
                    if (j != fromDicts.length - 1) {
                        resultPinyinStrBuf.append(separator);
                    }
                }

                i = i + selectedEmits.get(nextHitIndex).size();
                nextHitIndex++;
            } else {
                // 将第i个字符转为拼音
                resultPinyinStrBuf.append(PinYinTools.toPinyin(inputStr.charAt(i)));
                i++;
            }

            if (i != inputStr.length()) {
                resultPinyinStrBuf.append(separator);
            }
        }

        return resultPinyinStrBuf.toString();
    }

    public static String[] pinyinFromDict(String wordInDict, List<PinyinDict> pinyinDictSet) {
        if (pinyinDictSet != null) {
            for (PinyinDict dict : pinyinDictSet) {
                if (dict != null && dict.words() != null
                        && dict.words().contains(wordInDict)) {
                    return dict.toPinyin(wordInDict);
                }
            }
        }
        throw new IllegalArgumentException("No pinyin dict contains word: " + wordInDict);
    }

    public static final class EmitComparator implements Comparator<Emit> {

        @Override
        public int compare(Emit o1, Emit o2) {
            if (o1.getStart() == o2.getStart()) {
                // 起点相同时，更长的排前面
                return Integer.compare(o2.size(), o1.size());
            } else {
                // 起点小的放前面
                return Integer.compare(o1.getStart(), o2.getStart());
            }
        }
    }

}
