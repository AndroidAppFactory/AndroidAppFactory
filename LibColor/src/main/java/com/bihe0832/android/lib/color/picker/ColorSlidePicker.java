package com.bihe0832.android.lib.color.picker;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Summary
 *
 * @author zixie code@bihe0832.com
 *         Created on 2023/9/5.
 *         Description:
 */
public interface ColorSlidePicker {

    /**
     * 是否有指示条
     */
    boolean hasSlideLine = true;
    /**
     * 指示条paint
     */
    Paint slideLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 指示条颜色
     */
    int slideLineColor = Color.WHITE;
    /**
     * 指示条宽度
     */
    int slideLineWidth = 5;

    void setPosition(float ratio);
}
