package com.bihe0832.android.lib.color.picker.dialog.impl;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.core.widget.NestedScrollView;

import com.bihe0832.android.lib.color.picker.OnAlphaSelectedListener;
import com.bihe0832.android.lib.color.picker.OnColorSelectedListener;
import com.bihe0832.android.lib.color.picker.alpha.AlphaSlideView;
import com.bihe0832.android.lib.color.picker.color.ColorRingPickerView;
import com.bihe0832.android.lib.color.picker.color.ColorWheelPickerView;
import com.bihe0832.android.lib.color.picker.deep.DeepSlideView;
import com.bihe0832.android.lib.color.utils.ColorUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.custom.view.background.ViewWithBackground;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.utils.os.DisplayUtil;
import com.ricky.color_picker.R;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/7/20.
 * Description: Description
 */
public class ColorPickDialog extends CommonDialog {

    private static final String TAG = "ColorPickDialog";

    public static final int TYPE_WHELL = 1;
    public static final int TYPE_RING = 2;

    private int pickType = TYPE_WHELL;
    private int defaultValue = Color.BLACK;
    private int defaultAlpha = 255;
    private float width = 160.0f;

    private int currentColorValue = Color.BLACK;
    private int currentColorAlpha = 255;

    private ColorWheelPickerView wheelPickerView = null;

    private ColorRingPickerView ringPickerView = null;

    private AlphaSlideView alphaSlideView = null;
    private DeepSlideView deepSlideView = null;

    private ViewWithBackground currentColorBackground = null;

    private EditText currentColorText = null;
    private NestedScrollView pickLayout = null;
    private OnColorSelectedListener mOnColorSelectedListener = new OnColorSelectedListener() {
        @Override
        public void onColorSelecting(int color) {
            currentColorValue = color;
            updateViewColor(true, true);
        }

        @Override
        public void onColorSelected(int color) {
            currentColorValue = color;
            updateViewColor(true, true);
        }
    };

    private OnAlphaSelectedListener mOnAlphaSelectedListener = new OnAlphaSelectedListener() {
        @Override
        public void onAlphaSelecting(float alpha) {
            currentColorAlpha = (int) (alpha * 255);
            updateViewColor(true, true);
        }

        @Override
        public void onAlphaSelected(float alpha) {
            currentColorAlpha = (int) (alpha * 255);
            updateViewColor(true, true);
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
        try {
            if (pickType == TYPE_WHELL) {
                wheelPickerView = findViewById(R.id.dialog_color_wheel_view);
                wheelPickerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (pickType == TYPE_RING) {
                ringPickerView = findViewById(R.id.dialog_color_ring_view);
                ringPickerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        alphaSlideView = findViewById(R.id.dialog_color_alpha_slide_view);
        deepSlideView = findViewById(R.id.dialog_color_deep_slide_view);
        currentColorBackground = findViewById(R.id.dialog_color_current_background);
        currentColorText = findViewById(R.id.dialog_color_current);
        pickLayout = findViewById(R.id.dialog_color_pick);
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
        alphaSlideView.setOnAlphaSelectedListener(mOnAlphaSelectedListener);
        currentColorText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String uppercase = s.toString().toUpperCase().trim();
                if (!s.toString().equals(uppercase)) {
                    currentColorText.removeTextChangedListener(this);
                    currentColorText.setText(uppercase);
                    currentColorText.setSelection(start + count);
                    currentColorText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputColor = currentColorText.getText().toString();
                try {
                    int color = Color.parseColor(inputColor);
                    if (currentColorAlpha != ColorUtils.getAlpha(color) || currentColorValue != ColorUtils.removeAlpha(color)) {
                        currentColorValue = ColorUtils.removeAlpha(color);
                        currentColorAlpha = ColorUtils.getAlpha(color);
                        updateViewColor(false, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        currentColorText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String inputColor = currentColorText.getText().toString();
                try {
                    int color = Color.parseColor(inputColor);
                    currentColorValue = ColorUtils.removeAlpha(color);
                    currentColorAlpha = ColorUtils.getAlpha(color);
                    updateViewColor(true, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            return false;
        });

        deepSlideView.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelecting(int color) {
                currentColorValue = color;
                updateViewColor(true, false);
            }

            @Override
            public void onColorSelected(int color) {
                currentColorValue = color;
                updateViewColor(true, true);
            }
        });
    }

    @Override
    protected void refreshView() {
        super.refreshView();
        updateViewColor(true, true);
        ViewGroup.LayoutParams para = null;
        if (null != wheelPickerView) {
            para = wheelPickerView.getLayoutParams();
        }
        if (null != ringPickerView) {
            para = ringPickerView.getLayoutParams();
        }
        if (null != para) {
            para.width = (int) width;
            para.height = (int) width;
        }

        try {
            if (null != wheelPickerView && para != null) {
                wheelPickerView.setLayoutParams(para);
            }
            if (null != ringPickerView && para != null) {
                ringPickerView.setLayoutParams(para);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getFeedback() != null) {
            int screenWidth = DisplayUtil.getScreenWidth(getContext());
            int screenHeight = DisplayUtil.getScreenHeight(getContext());
            if (screenWidth > screenHeight && width > screenHeight * 0.40f) {
                getFeedback().setVisibility(View.VISIBLE);
            } else {
                getFeedback().setVisibility(View.GONE);
            }
        }
    }

    private void updateViewColor(boolean updateText, boolean updateDepth) {
        ZLog.d(TAG, "currentValue:" + ColorUtils.color2Hex(ColorUtils.addAlpha(currentColorValue, currentColorValue)));
        ZLog.d(TAG, "currentAlpha:" + currentColorAlpha);
        ZLog.d(TAG, "currentDepth:" + ColorUtils.getColorBrightness(currentColorValue));
        ZLog.d(TAG, "currentValue removeAlpha:" + ColorUtils.color2Hex(ColorUtils.removeAlpha(currentColorValue)));
        ZLog.d(TAG, "currentValue complementary:" + ColorUtils.color2Hex(ColorUtils.getComplementaryColor(currentColorValue)));

        if (null != currentColorText) {
            if (updateText) {
                currentColorText.setText(ColorUtils.color2Hex(currentColorAlpha, currentColorValue));
            }
            if (ColorUtils.isLightColor(currentColorValue) || currentColorAlpha < 128) {
                currentColorText.setTextColor(Color.BLACK);
            } else {
                currentColorText.setTextColor(Color.WHITE);
            }
        }

        if (null != currentColorBackground) {
            currentColorBackground.setBackgroundColor(currentColorValue);
            currentColorBackground.setAlpha(currentColorAlpha / 255f);
        }

        if (null != alphaSlideView) {
            alphaSlideView.setBaseAlpha(currentColorAlpha);
            alphaSlideView.setBaseColor(currentColorValue);
        }
        if (null != deepSlideView) {
            if (updateDepth) {
                deepSlideView.setPosition((float) ColorUtils.getColorBrightness(currentColorValue));
                deepSlideView.setBaseColor(currentColorValue);
            }
        }
    }

    public int getCurrentColorText() {
        return ColorUtils.addAlpha(currentColorAlpha, currentColorValue);
    }

    public void setCurrentColorText(int value) {
        this.defaultValue = value;
        this.currentColorValue = defaultValue;
        refreshView();
    }

    public void setWidth(float widthDP) {
        this.width = widthDP;
    }

    public int getDefaultColor() {
        return ColorUtils.addAlpha(defaultAlpha, defaultValue);
    }

    public void setCurrentColorAlpha(int currentColorAlpha) {
        this.defaultAlpha = currentColorAlpha;
        this.currentColorAlpha = currentColorAlpha;
        refreshView();
    }

    public void reset() {
        this.currentColorAlpha = defaultAlpha;
        this.currentColorValue = defaultValue;
        if (pickLayout != null) {
            pickLayout.scrollTo(0, 0);
        }
        refreshView();
    }
}
