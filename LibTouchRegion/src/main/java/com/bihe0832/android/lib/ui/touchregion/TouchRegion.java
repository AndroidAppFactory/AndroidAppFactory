package com.bihe0832.android.lib.ui.touchregion;

import android.graphics.Rect;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;


public class TouchRegion {
    private static final String TAG = TouchRegion.class.getSimpleName();

    private TouchDelegateGroup touchDelegateGroup;

    /**
     * 构造方法
     *
     * @param viewGroup 触摸view的父类
     */
    public TouchRegion(ViewGroup viewGroup) {
        touchDelegateGroup = new TouchDelegateGroup(viewGroup);
    }

    /**
     * 构造方法
     *
     * @param view 触摸view
     */
    public TouchRegion(View view) {
        touchDelegateGroup = new TouchDelegateGroup((View) view.getParent());
    }

    /**
     * 扩大View的触摸和点击范围，最大不超过其父View范围
     *
     * @param view
     * @param margin
     */

    public void expandViewTouchRegion(View view, float margin) {
        expandViewTouchRegion(view, (int)margin);
    }

    public void expandViewTouchRegion(View view, int margin) {
        expandViewTouchRegion(view, margin, margin, margin, margin);
    }

    /**
     * 扩大View的触摸和点击范围，最大不超过其父View范围
     *
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public void expandViewTouchRegion(final View view, final float left, final float top, final float right, final float bottom) {
        expandViewTouchRegion(view, (int) left,(int) top,(int) right,(int) bottom);
    }
        /**
     * 扩大View的触摸和点击范围，最大不超过其父View范围
     *
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public void expandViewTouchRegion(final View view, final int left, final int top, final int right, final int bottom) {

        if (view == null) {
            Log.e(TAG, "expandViewTouchRegion -> view cannot be null!!!");
            return;
        }

        restoreViewTouchRegion(view);

        final ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (viewGroup != null) {
            viewGroup.post(new Runnable() {
                @Override
                public void run() {
                    Rect bounds = new Rect();
                    view.setEnabled(true);
                    view.getHitRect(bounds);

                    bounds.left -= left;
                    bounds.top -= top;
                    bounds.right += right;
                    bounds.bottom += bottom;

                    touchDelegateGroup.addTouchDelegate(new TouchDelegate(bounds, view));

                    if (View.class.isInstance(viewGroup)) {
                        viewGroup.setTouchDelegate(touchDelegateGroup);
                    }
                }
            });
        }

    }

    /**
     * 恢复View的触摸和点击范围，最小不小于View自身范围
     *
     * @param view
     */
    public void restoreViewTouchRegion(final View view) {

        if (view == null) {
            Log.e(TAG, "restoreViewTouchRegion -> view cannot be null!!!");
            return;
        }

        final ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (viewGroup != null) {
            viewGroup.post(new Runnable() {
                @Override
                public void run() {
                    Rect bounds = new Rect();
                    bounds.setEmpty();
                    touchDelegateGroup.addTouchDelegate(new TouchDelegate(bounds, view));

                    if (View.class.isInstance(viewGroup)) {
                        viewGroup.setTouchDelegate(touchDelegateGroup);
                    }
                }
            });
        }
    }
}
