package com.bihe0832.android.lib.floatview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bihe0832.android.lib.utils.os.DisplayUtil;

public abstract class BaseIconView extends ConstraintLayout {

    private static final String TAG = "ICON_VIEW";

    public static int sViewWidth;//悬浮窗的宽度
    public static int sViewHeight;//悬浮窗的高度
    private static int sStatusBarHeight;//状态栏的高度

    private WindowManager mWindowManager;//管理窗口类
    private WindowManager.LayoutParams mParams;//窗口的一些参数

    private float xInScreen;//记录当前手指位置在屏幕上的x坐标值
    private float yInScreen;//录当当前手指位置在屏幕上的y坐标值
    private float xDownInScreen;//手指按下时在屏幕中的x坐标
    private float yDownInScreen;//手指按下时在屏幕中的y坐标
    private float xInView;//手指按下时在view中的x坐标
    private float yInView;//手指按下时在view中的y坐标
    public ImageView mView;//小图标正常态
    private View layout;//小图标外层布局


    public BaseIconView(Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(getLayoutId(), this);
        layout = findViewById(getRootId());
        sViewWidth = layout.getLayoutParams().width;
        sViewHeight = layout.getLayoutParams().height;
    }

    public abstract int getLayoutId();

    public abstract int getRootId();

    public abstract int getDefaultX();

    public abstract int getDefaultY();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xInScreen = event.getRawX();
        if (ignoreStatusBar()) {
            yInScreen = event.getRawY();
        } else {
            yInScreen = event.getRawY() - getStatusBarHeight();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Icon开始旋转
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                if (ignoreStatusBar()) {
                    yDownInScreen = event.getRawY();
                } else {
                    yDownInScreen = event.getRawY() - getStatusBarHeight();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (needUpdateViewPosition()) {
                    updateViewPosition();
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    private boolean needUpdateViewPosition() {
        return Math.abs(xInScreen - xDownInScreen) > 12 || Math.abs(yInScreen - yDownInScreen) > 12;
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }


    /**
     * 更新悬浮窗的位置
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView) - 60;

        int minUnit = 0;
        if (ignoreStatusBar()) {
            minUnit = 0;
        } else {
            minUnit = getStatusBarHeight();
        }
        if (mParams.y < minUnit) {
            mParams.y = minUnit;
        } else if (mParams.y > DisplayUtil.getScreenHeight(getContext()) - minUnit) {
            mParams.y = DisplayUtil.getScreenHeight(getContext()) - minUnit;
        }
        mWindowManager.updateViewLayout(this, mParams);
    }

    public int getLocationX() {
        if (null == mParams) {
            return getDefaultX();
        } else {
            return mParams.x;
        }
    }

    public int getLocationY() {
        if (null == mParams) {
            return getDefaultY();
        } else {
            return mParams.y;
        }
    }

    protected boolean ignoreStatusBar() {
        return false;
    }

    protected int getStatusBarHeight() {
        if (sStatusBarHeight == 0) {
            sStatusBarHeight = DisplayUtil.getStatusBarHeight(getContext());
        }
        return sStatusBarHeight;
    }
}