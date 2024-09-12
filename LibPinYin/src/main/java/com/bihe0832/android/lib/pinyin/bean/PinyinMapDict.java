package com.bihe0832.android.lib.pinyin.bean;

import java.util.Map;
import java.util.Set;

/**
 * 基于{@link Map}的字典实现，利于添加自定义字典
 *
 */

public abstract class PinyinMapDict implements PinyinDict {

    /**
     * Key为字典的词，Value为该词所对应的拼音
     *
     * @return 包含词和对应拼音的 {@link Map}
     */
    public abstract Map<String, String[]> mapping();


    @Override
    public Set<String> words() {
        return mapping() != null ? mapping().keySet() : null;
    }

    @Override
    public String[] toPinyin(String word) {
        return mapping() != null ? mapping().get(word) : null;
    }
}
