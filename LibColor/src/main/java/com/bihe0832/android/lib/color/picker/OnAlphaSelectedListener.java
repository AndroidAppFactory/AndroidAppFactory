package com.bihe0832.android.lib.color.picker;

/**
 * 透明度
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/9/5.
 *         Description:
 */
public interface OnAlphaSelectedListener {

    /**
     * 当前选中的透明度
     *
     * @param alpha 0-1f
     */
    void onAlphaSelecting(float alpha);

    /**
     * 最终的透明度
     *
     * @param alpha 0-1f
     */
    void onAlphaSelected(float alpha);
}