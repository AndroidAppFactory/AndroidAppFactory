package com.bihe0832.android.lib.pinyin.bean;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ahocorasick.trie.Trie;

/**
 * 字典接口，字典应保证对{@link PinyinDict#words()}中的所有词，{@link PinyinDict#toPinyin(String)}均返回非null的结果
 */

public interface PinyinDict {

    static Trie dictsToTrie(List<PinyinDict> pinyinDicts) {
        Set<String> all = new TreeSet<String>();
        Trie.TrieBuilder builder = Trie.builder();
        if (pinyinDicts != null) {
            for (PinyinDict dict : pinyinDicts) {
                if (dict != null && dict.words() != null) {
                    all.addAll(dict.words());
                }
            }
            if (!all.isEmpty()) {
                for (String key : all) {
                    builder.addKeyword(key);
                }
                return builder.build();
            }
        }

        return null;
    }

    /**
     * 字典所包含的所有词
     *
     * @return 所包含的所有词
     */
    Set<String> words();

    /**
     * 将词转换为拼音
     *
     * @param word 词
     * @return 应保证对{@link PinyinDict#words()}中的所有词，{@link PinyinDict#toPinyin(String)}均返回非null的结果
     */
    String[] toPinyin(String word);
}
