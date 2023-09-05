package com.bihe0832.android.lib.color.picker.dialog.impl;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.bihe0832.android.lib.color.picker.OnAlphaSelectedListener;
import com.bihe0832.android.lib.color.picker.OnColorSelectedListener;
import com.bihe0832.android.lib.color.picker.alpha.AlphaSlideView;
import com.bihe0832.android.lib.color.picker.color.ColorRingPickerView;
import com.bihe0832.android.lib.color.picker.color.ColorWheelPickerView;
import com.bihe0832.android.lib.color.utils.ColorUtils;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.ricky.color_picker.R;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2023/7/20.
 *         Description: Description
 */
public class ColorPickDialog extends CommonDialog {

    public static final int TYPE_WHELL = 1;
    public static final int TYPE_RING = 2;

    private int pickType = TYPE_WHELL;
    private int defaultValue = Color.BLACK;
    private int defaultAlpha = 255;

    private int currentValue = Color.BLACK;
    private int currentAlpha = 255;
    private ColorWheelPickerView wheelPickerView = null;

    private ColorRingPickerView ringPickerView = null;

    private AlphaSlideView alphaSlideView = null;

    private TextView textView = null;
    private OnColorSelectedListener mOnColorSelectedListener = new OnColorSelectedListener() {
        @Override
        public void onColorSelecting(int color) {
            currentValue = color;
            updateViewColor(true);
        }

        @Override
        public void onColorSelected(int color) {
            currentValue = color;
            updateViewColor(true);
        }
    };

    public ColorPickDialog(Context context, int type) {
        super(context);
        pickType = type;
    }

    protected int getLayoutID() {
        return R.layout.com_bihe0832_color_dialog_layout;
    }

    protected void initView() {
        super.initView();
        this.setShouldCanceled(true);
        if (pickType == TYPE_WHELL) {
            try {
                wheelPickerView = (ColorWheelPickerView) findViewById(R.id.dialog_color_wheel_view);
                wheelPickerView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (pickType == TYPE_RING) {
            try {
                ringPickerView = (ColorRingPickerView) findViewById(R.id.dialog_color_ring_view);
                ringPickerView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        alphaSlideView = (AlphaSlideView) findViewById(R.id.dialog_color_alpha_slide_view);
        textView = findViewById(R.id.dialog_color_current);
        updateViewColor(true);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        if (null != wheelPickerView) {
            wheelPickerView.setOnColorSelectedListener(mOnColorSelectedListener);
        }
        if (null != ringPickerView) {
            ringPickerView.setOnColorSelectedListener(mOnColorSelectedListener);
        }
        alphaSlideView.setOnAlphaSelectedListener(new OnAlphaSelectedListener() {
            @Override
            public void onAlphaSelecting(float alpha) {
                currentAlpha = (int) (alpha * 255);
                updateViewColor(false);
            }

            @Override
            public void onAlphaSelected(float alpha) {
                currentAlpha = (int) (alpha * 255);
                updateViewColor(false);
            }
        });
    }

    @Override
    protected void refreshView() {
        super.refreshView();
        updateViewColor(true);
    }


    private void updateViewColor(boolean updateAlpha) {
        if (null != textView) {
            textView.setTextColor(ColorUtils.removeAlpha(currentValue));
            textView.setAlpha(currentAlpha / 255f);
            textView.setText(ColorUtils.color2Hex(currentAlpha, currentValue));
        }

        if (null != alphaSlideView) {
            if (updateAlpha) {
                alphaSlideView.post(() -> alphaSlideView.setBaseAlpha(currentAlpha));

            }
            alphaSlideView.setBaseColor(currentValue);
        }
    }

    public int getCurrentColor() {
        return ColorUtils.addAlpha(currentAlpha, currentValue);
    }

    public void setCurrentColor(int value) {
        this.defaultValue = value;
        this.currentValue = defaultValue;
        refreshView();
    }

    public int getDefaultColor() {
        return ColorUtils.addAlpha(defaultAlpha, defaultValue);
    }

    public void setCurrentAlpha(int currentAlpha) {
        this.defaultAlpha = currentAlpha;
        this.currentAlpha = currentAlpha;
        refreshView();
    }

    public void reset() {
        this.currentAlpha = defaultAlpha;
        this.currentValue = defaultValue;
        refreshView();
    }
}
