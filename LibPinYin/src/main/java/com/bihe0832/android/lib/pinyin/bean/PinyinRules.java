package com.bihe0832.android.lib.pinyin.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PinyinRules {

    private final Map<String, String[]> mOverrides = new ConcurrentHashMap<>();

    public PinyinRules add(char c, String pinyin) {
        mOverrides.put(String.valueOf(c), new String[]{pinyin});
        return this;
    }

    public PinyinRules add(String str, String pinyin) {
        mOverrides.put(str, new String[]{pinyin});
        return this;
    }

    public String toPinyin(char c) {
        return mOverrides.get(String.valueOf(c))[0];
    }

    public PinyinMapDict toPinyinMapDict() {
        return new PinyinMapDict() {
            @Override
            public Map<String, String[]> mapping() {
                return mOverrides;
            }
        };
    }
}
