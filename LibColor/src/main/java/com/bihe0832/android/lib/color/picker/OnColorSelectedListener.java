package com.bihe0832.android.lib.color.picker;


import androidx.annotation.ColorInt;

/**
 * 色值
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/9/5.
 *         Description:
 */
public interface OnColorSelectedListener {

    /**
     * 当前选中的颜色
     */
    void onColorSelecting(@ColorInt int color);

    /**
     * 最终颜色
     */
    void onColorSelected(@ColorInt int color);
}