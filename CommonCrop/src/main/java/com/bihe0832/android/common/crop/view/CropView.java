package com.bihe0832.android.common.crop.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.bihe0832.android.common.crop.R;
import com.bihe0832.android.common.crop.callback.CropBoundsChangeListener;
import com.bihe0832.android.common.crop.callback.OverlayViewChangeListener;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

public class CropView extends FrameLayout {

    private final OverlayView mViewOverlay;
    private GestureCropImageView mGestureCropImageView;
    private RectF lastCropRect = null;

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

    // 基于精度比较两个float值
    public static boolean floatEquals(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
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
                ZLog.d("CropView", "-----------");
                float newAspectRatio = (newCropRect.right - newCropRect.left) / (newCropRect.bottom - newCropRect.top);
                ZLog.d("CropView", "裁剪框 新的宽高比 newAspectRatio:" + newAspectRatio);
                ZLog.d("CropView", "裁剪框 原有宽高比 newAspectRatio:" + mViewOverlay.getTargetAspectRatio());
                if (floatEquals(mViewOverlay.getTargetAspectRatio(), newAspectRatio, 0.001f)) {
                    if (null != lastCropRect) {
                        ZLog.d("CropView", "上次比例变动前的 RectF:" + lastCropRect);
                        ZLog.d("CropView", "上次比例变动后的 RectF:" + newCropRect);
                        ZLog.d("CropView",
                                "当前View 边界:" + getLeft() + " " + getTop() + " " + getRight() + " " + getBottom());
                        float scaleX = (float) newCropRect.width() / lastCropRect.width();
                        float scaleY = (float) newCropRect.height() / lastCropRect.height();
                        ZLog.d("CropView", "从上次的 RectF 到本次 RectF 的 scaleX:" + scaleX + ",scaleY: " + scaleY);

                        float scale = Math.max(scaleX, scaleY);
                        ZLog.d("CropView", "从上次的 RectF 到本次 RectF 的 scale:" + scale);
                        // 新的宽高比
                        ZLog.d("CropView", "框宽高比一致，执行缩放截图:" + scale);
                        ZLog.d("CropView", "裁剪框宽高比一致，缩放截图:" + newCropRect);
                        ZLog.d("CropView", "裁剪框宽高比一致，缩放截图:" + lastCropRect);
                        float lastCenterX = (lastCropRect.right + lastCropRect.left) / 2;
                        float lastCenterY = (lastCropRect.bottom + lastCropRect.top) / 2;
                        ZLog.d("CropView", "缩放截图，图片缩放前中心坐标 X:" + lastCenterX);
                        ZLog.d("CropView", "缩放截图，图片缩放前中心坐标 Y:" + lastCenterY);
                        float newCenterX = (newCropRect.right + newCropRect.left) / 2;
                        float newCenterY = (newCropRect.bottom + newCropRect.top) / 2;
                        ZLog.d("CropView", "缩放截图，图片缩放后中心坐标 X:" + newCenterX);
                        ZLog.d("CropView", "缩放截图，图片缩放后中心坐标 Y:" + newCenterY);
                        float centerX = (float) (getRight() + getLeft()) / 2;
                        float centerY = (float) (getBottom() + getTop()) / 2;
                        ZLog.d("CropView", "缩放截图，页面中心坐标 X:" + centerX);
                        ZLog.d("CropView", "缩放截图，页面中心坐标 Y:" + centerY);
                        ZLog.d("CropView", "缩放截图，newCenterY 与 centerY 差额:" + (newCenterY - centerY));
                        ZLog.d("CropView", "缩放截图，newCenterY 与 centerY 差额DP:" + DisplayUtil.px2dip(getContext(),(newCenterY - centerY)));
                        ZLog.d("CropView", "缩放截图，getNavigationBarHeight 差额:" + (DisplayUtil.getNavigationBarHeight(getContext())));
                        ZLog.d("CropView", "缩放截图，图片平移  mViewOverlay.getPaddingTop:" + mViewOverlay.getPaddingTop());
                        ZLog.d("CropView", "缩放截图，图片平移  getPaddingTop:" + getPaddingTop());

                        if (lastCropRect != null) {
                            postDelayed(() -> {
                                float distanceX = newCenterX - lastCenterX;
                                float distanceY = newCenterY - lastCenterY;
                                ZLog.d("CropView", "缩放截图，图片平移 distanceX:" + distanceX);
                                ZLog.d("CropView", "缩放截图，图片平移 distanceY:" + distanceY);
                                mGestureCropImageView.postTranslate(distanceX, distanceY);
                                ZLog.d("CropView", "执行缩放截图，图片缩放比例:" + scale);
                                mGestureCropImageView.zoomImageToPosition(
                                        mGestureCropImageView.getCurrentScale() * scale, lastCenterX, lastCenterY, 200);
                            }, 200);
                        }
                    }
                } else {
                    ZLog.d("CropView", "裁剪框宽高比变化，记录调整后的截图框:" + newCropRect);
                    postDelayed(() -> {
                        lastCropRect = new RectF(newCropRect);
                        mViewOverlay.setTargetAspectRatio(newAspectRatio);
                    }, 200);

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
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mGestureCropImageView);
        mGestureCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }
}