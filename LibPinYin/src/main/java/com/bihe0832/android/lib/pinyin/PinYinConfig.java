package com.bihe0832.android.lib.pinyin;

import com.bihe0832.android.lib.pinyin.bean.PinyinDict;
import com.bihe0832.android.lib.pinyin.core.ForwardLongestSelector;
import com.bihe0832.android.lib.pinyin.core.SegmentationSelector;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/9/12.
 *         Description:
 */
public final class PinYinConfig {

        SegmentationSelector mSelector;

        List<PinyinDict> mPinyinDicts;

        PinYinConfig(List<PinyinDict> dicts) {
            if (dicts != null) {
                mPinyinDicts = new ArrayList<>(dicts);
            }
            mSelector = new ForwardLongestSelector();
        }

        /**
         * 添加字典
         *
         * @param dict 字典
         * @return 返回Config对象，支持继续添加字典
         */
        public PinYinConfig with(PinyinDict dict) {
            if (dict != null) {
                if (mPinyinDicts == null) {
                    mPinyinDicts = new ArrayList<>();
                    mPinyinDicts.add(dict);
                } else if (!mPinyinDicts.contains(dict)) {
                    mPinyinDicts.add(dict);
                }
            }
            return this;
        }

        public boolean valid() {
            return getPinyinDicts() != null && getSelector() != null;
        }

        public SegmentationSelector getSelector() {
            return mSelector;
        }

        public List<PinyinDict> getPinyinDicts() {
            return mPinyinDicts;
        }
    }