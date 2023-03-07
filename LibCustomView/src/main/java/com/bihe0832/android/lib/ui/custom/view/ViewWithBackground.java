package com.bihe0832.android.lib.ui.custom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.bihe0832.android.lib.ui.view.ext.DrawableFactoryKt;
import com.bihe0832.android.lib.ui.view.ext.R;
import com.bihe0832.android.lib.utils.os.DisplayUtil;


/**
 * 用于需要圆角矩形框背景的TextView的情况,减少直接使用TextView时引入的shape资源文件
 */
public class ViewWithBackground extends View {
    private Context context;
    private int backgroundColor;
    private int cornerRadius;
    private int strokeWidth;
    private int strokeColor;
    private boolean isRadiusHalfHeight;
    private boolean isWidthHeightEqual;

    public ViewWithBackground(Context context) {
        this(context, null);
    }

    public ViewWithBackground(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewWithBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        obtainAttributes(context, attrs);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewWithBackground);
        backgroundColor = ta.getColor(R.styleable.ViewWithBackground_bgtv_backgroundColor, Color.TRANSPARENT);
        cornerRadius = ta.getDimensionPixelSize(R.styleable.ViewWithBackground_bgtv_cornerRadius, 0);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.ViewWithBackground_bgtv_strokeWidth, 0);
        strokeColor = ta.getColor(R.styleable.ViewWithBackground_bgtv_strokeColor, Color.TRANSPARENT);
        isRadiusHalfHeight = ta.getBoolean(R.styleable.ViewWithBackground_bgtv_isRadiusHalfHeight, false);
        isWidthHeightEqual = ta.getBoolean(R.styleable.ViewWithBackground_bgtv_isWidthHeightEqual, false);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isWidthHeightEqual() && getWidth() > 0 && getHeight() > 0) {
            int max = Math.max(getWidth(), getHeight());
            int measureSpec = View.MeasureSpec.makeMeasureSpec(max, View.MeasureSpec.EXACTLY);
            super.onMeasure(measureSpec, measureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isRadiusHalfHeight()) {
            setCornerRadius(getHeight() / 2);
        } else {
            setBgSelector();
        }
    }


    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        setBgSelector();
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = DisplayUtil.dip2px(context, cornerRadius);
        setBgSelector();
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = DisplayUtil.dip2px(context, strokeWidth);
        setBgSelector();
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        setBgSelector();
    }

    public void setIsRadiusHalfHeight(boolean isRadiusHalfHeight) {
        this.isRadiusHalfHeight = isRadiusHalfHeight;
        setBgSelector();
    }

    public void setIsWidthHeightEqual(boolean isWidthHeightEqual) {
        this.isWidthHeightEqual = isWidthHeightEqual;
        setBgSelector();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public boolean isRadiusHalfHeight() {
        return isRadiusHalfHeight;
    }

    public boolean isWidthHeightEqual() {
        return isWidthHeightEqual;
    }


    public void setBgSelector() {
        DrawableFactoryKt.setDrawableBackground(this, backgroundColor, (float) cornerRadius, strokeWidth, strokeColor);
    }
}
