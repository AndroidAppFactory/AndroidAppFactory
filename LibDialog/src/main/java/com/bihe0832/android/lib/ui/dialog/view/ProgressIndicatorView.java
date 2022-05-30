/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/26 下午12:32
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/23 下午3:23
 *
 */

package com.bihe0832.android.lib.ui.dialog.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.bihe0832.android.lib.utils.os.DisplayUtil;

import java.util.ArrayList;
import java.util.List;


public class ProgressIndicatorView extends View {

    public static final int DEFAULT_ITEM_SIZE = 16;
    public static final int DEFAULT_NUM = 3;
    public static final float SCALE = 1.0f;

    private boolean mAutoAnimation = true;
    private int mAnimationNum = DEFAULT_NUM;

    private List<Animator> mAnimators = null;
    private float[] scaleFloats = new float[]{};
    Paint mPaint;

    public ProgressIndicatorView(Context context) {
        super(context);
        initView();
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureDimension(DisplayUtil.dip2px(getContext(), DEFAULT_ITEM_SIZE * mAnimationNum), widthMeasureSpec);
        int height = measureDimension(DisplayUtil.dip2px(getContext(), DEFAULT_ITEM_SIZE * 3), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureDimension(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float xradius = getWidth() / (mAnimators.size() * 2 + 1);
        float yradius = getHeight() / mAnimators.size();
        float radius = Math.min(xradius, yradius) / 2;
        float circleSpacing = (getWidth() - radius * 2 * mAnimators.size()) / (mAnimators.size() + 1);
        float x = getWidth() / 2 - (radius * 2 + circleSpacing);
        float y = getHeight() / 2;
        for (int i = 0; i < mAnimators.size(); i++) {
            canvas.save();
            float translateX = x + (radius * 2) * i + circleSpacing * i;
            canvas.translate(translateX, y);
            if (scaleFloats.length > i) {
                canvas.scale(scaleFloats[i], scaleFloats[i]);
            }
            canvas.drawCircle(0, 0, radius, mPaint);
            canvas.restore();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (null == mAnimators || mAnimators.isEmpty()) {
            applyAnimation();
        }
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (mAutoAnimation) {
                if (v == GONE || v == INVISIBLE) {
                    setAnimationStatus(AnimStatus.END);
                } else {
                    setAnimationStatus(AnimStatus.START);
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setAnimationStatus(AnimStatus.CANCEL);
    }

    protected void applyAnimation() {
        mAnimators = createAnimation();
    }

    public enum AnimStatus {
        START, END, CANCEL
    }

    protected List<Animator> createAnimation() {
        List<Animator> animators = new ArrayList<>();
        scaleFloats = new float[mAnimationNum];
        for (int i = 0; i < mAnimationNum; i++) {
            final int index = i;
            scaleFloats[index] = SCALE;
            ValueAnimator scaleAnim = ValueAnimator.ofFloat(1, 0.3f, 1);
            scaleAnim.setDuration(375 * mAnimationNum);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(180 * i + 1);
            scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            scaleAnim.start();
            animators.add(scaleAnim);
        }
        return animators;
    }


    /**
     * make animation to start or end when target
     * view was be Visible or Gone or Invisible.
     * make animation to cancel when target view
     * be onDetachedFromWindow.
     *
     * @param animStatus
     */
    public void setAnimationStatus(AnimStatus animStatus) {
        if (mAnimators == null) {
            return;
        }
        int count = mAnimators.size();
        for (int i = 0; i < count; i++) {
            Animator animator = mAnimators.get(i);
            boolean isRunning = animator.isRunning();
            switch (animStatus) {
                case START:
                    if (!isRunning) {
                        animator.start();
                    }
                    break;
                case END:
                    if (isRunning) {
                        animator.end();
                    }
                    break;
                case CANCEL:
                    if (isRunning) {
                        animator.cancel();
                    }
                    break;
            }
        }
    }

    public void setAnimationNum(int num) {
        this.mAnimationNum = num;
    }

    public void seAutoAnimation(boolean mAutoAnimation) {
        this.mAutoAnimation = mAutoAnimation;
    }
}
