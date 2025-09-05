package com.bihe0832.android.common.crop.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.bihe0832.android.common.crop.R;
import com.bihe0832.android.common.crop.callback.CropBoundsChangeListener;
import com.bihe0832.android.common.crop.callback.OverlayViewChangeListener;
import com.bihe0832.android.lib.log.ZLog;

public class CropView extends FrameLayout {

    private final OverlayView mViewOverlay;
    private GestureCropImageView mGestureCropImageView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mPendingScaleTask; // 管理延迟缩放任务
    private RectF lastAspectRect = null;
    private float lastAspect = 0f;
    private static final float ASPECT_RATIO_EPSILON = 0.001f; // 宽高比比较误差阈值（0.1%）
    private static final long SCALE_DELAY_MS = 100; // 缩放操作延迟（减少抖动）


    public CropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.com_bihe_0832_android_common_crop_crop_view, this, true);
        mGestureCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropView);
        mViewOverlay.processStyledAttributes(a);
        mGestureCropImageView.processStyledAttributes(a);
        a.recycle();

        setListenersToViews();
    }

    private boolean isValidCropRect(RectF rect) {
        return rect.right > rect.left && rect.bottom > rect.top && rect.left >= 0 && rect.top >= 0
                && rect.right <= getWidth() && rect.bottom <= getHeight();
    }

    /**
     * 相对误差浮点数比较（优化精度）
     *
     * @param a 浮点数A
     * @param b 浮点数B
     * @param relEpsilon 相对误差阈值（如0.001表示0.1%）
     * @return 是否相等（考虑相对误差）
     */
    public static boolean floatEquals(float a, float b, float relEpsilon) {
        if (a == b) {
            return true;
        }
        float max = Math.max(Math.abs(a), Math.abs(b));
        return Math.abs(a - b) <= max * relEpsilon || Math.abs(a - b) < 1e-6f;
    }

    private void cancleTask() {
        if (mPendingScaleTask != null) {
            mHandler.removeCallbacks(mPendingScaleTask);
            mPendingScaleTask = null;
        }
    }

    private void setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                ZLog.d("CropView", "图片宽高比变化，调整后截图框:" + cropRatio);
                mViewOverlay.setTargetAspectRatio(cropRatio);
            }
        });
        mViewOverlay.setOverlayViewChangeListener(new OverlayViewChangeListener() {
            @Override
            public void onCropRectUpdated(RectF newCropRect) {
                ZLog.d("CropView", "------------------------- CropView Start ------------------------------");
                // 1. 校验裁剪框有效性（严格检查）
                if (!isValidCropRect(newCropRect)) {
                    ZLog.e("CropView", "无效裁剪框：right≤left 或 bottom≤top");
                    return;
                }
                float newAspectRatio = (newCropRect.right - newCropRect.left) / (newCropRect.bottom - newCropRect.top);
                ZLog.d("CropView", "裁剪框 原有宽高比 newAspectRatio:" + lastAspect);
                ZLog.d("CropView", "裁剪框 新的宽高比 newAspectRatio:" + newAspectRatio);
                if (!floatEquals(lastAspect, newAspectRatio, ASPECT_RATIO_EPSILON)) {
                    ZLog.d("CropView", "裁剪框更新，记录调整后的截图框 1:" + newCropRect);
                    cancleTask();
                    mPendingScaleTask = () -> {
                        lastAspect = newAspectRatio;
                        lastAspectRect = new RectF(newCropRect);
                        mViewOverlay.setTargetAspectRatio(newAspectRatio);
                        ZLog.d("CropView", "------------------------- CropView End ------------------------------");
                    };
                    mHandler.postDelayed(mPendingScaleTask, SCALE_DELAY_MS);
                } else {
                    if (lastAspectRect != null) {
                        ZLog.d("CropView", "上次比例变动后的 RectF:" + lastAspectRect);
                        ZLog.d("CropView", "上次比例变动后最终的 RectF:" + newCropRect);
                        float scaleX = (float) newCropRect.width() / lastAspectRect.width();
                        float scaleY = (float) newCropRect.height() / lastAspectRect.height();
                        ZLog.d("CropView",
                                "从上次比例变动后的 RectF 到变动后最终的 RectF 的 scaleX:" + scaleX + ",scaleY: "
                                        + scaleY);
                        float scale = Math.min(scaleX, scaleY);
                        ZLog.d("CropView", "从上次比例变动后的 RectF 到变动后最终的 RectF 的 scale:" + scale);
                        ZLog.d("CropView", "框宽高比一致，执行缩放截图:" + scale);
                        ZLog.d("CropView", "缩放截图，图片缩放后中心坐标 centerX:" + lastAspectRect.centerX());
                        ZLog.d("CropView", "缩放截图，图片缩放后中心坐标 centerY:" + lastAspectRect.centerY());
                        ZLog.d("CropView", "缩放截图，图片二次缩放后中心坐标 centerX:" + newCropRect.centerX());
                        ZLog.d("CropView", "缩放截图，图片二次缩放后中心坐标 centerY:" + newCropRect.centerY());
                        cancleTask();
                        mPendingScaleTask = () -> {
                            float distanceX = newCropRect.centerX() - lastAspectRect.centerX();
                            float distanceY = newCropRect.centerY() - lastAspectRect.centerY();
                            ZLog.d("CropView", "缩放截图，图片平移 distanceX:" + distanceX);
                            ZLog.d("CropView", "缩放截图，图片平移 distanceY:" + distanceY);
                            mGestureCropImageView.postScaleAndTrans(scale, newCropRect.centerX(), newCropRect.centerY(),
                                    distanceX, distanceY);
                            lastAspectRect = new RectF(newCropRect);
                            ZLog.d("CropView", "裁剪框更新，记录调整后的截图框 2:" + newCropRect);
                            ZLog.d("CropView", "------------------------- CropView End ------------------------------");
                        };
                        mHandler.postDelayed(mPendingScaleTask, SCALE_DELAY_MS);
                    }
                }
            }
        });
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }


    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation. Be careful: this method recreate
     * UCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mGestureCropImageView);
        mGestureCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }
}