package com.bihe0832.android.lib.floatview;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bihe0832.android.lib.aaf.tools.AAFException;
import com.bihe0832.android.lib.config.Config;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

public abstract class IconView extends LinearLayout implements View.OnClickListener {

    public static final String TAG = "ICON_VIEW";


    public abstract ImageView getIconView();//小图标正常态

    public abstract View getIconLayout();//小图标外层布局

    public abstract int getLayoutId();//小图标正常态

    public abstract void initView();//小图标正常态


    public static final int TIME_DELAY_SHADOW_ICON = 3000;
    public static final int TIME_DELAY_HIDE_ICON = 2000;

    private static final int MSG_ICON_CHANGE_ICON_WINDOW_SHADOW = 1;
    private static final int MSG_ICON_CHANGE_ICON_WINDOW_HIDE = -1;

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
    private float mSlidePercent = 0.3f;//icon 隐藏时隐藏的比例
    private Animation mIconMovingAnim;//悬浮窗拖动时的动画
    private boolean isLogoAnimRunning;//Icon选中动画是否正在运行
    public volatile boolean hasBeenAdded = false;
    private OnClickListener mOnClickListener = null;

    Handler mUiHandler = new Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_ANDROID_MAIN)) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ICON_CHANGE_ICON_WINDOW_SHADOW:
                    shadowIcon();
                    break;
                case MSG_ICON_CHANGE_ICON_WINDOW_HIDE:
                    hideIcon();
                    break;
            }
        }
    };

    public IconView(Context context) throws AAFException {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(getLayoutId(), this);

        initView();
        if (null == getIconView()) {
            throw new AAFException("IconView must have IconView");
        }

        if (null == getIconLayout()) {
            throw new AAFException("IconView must have IconLayout");
        }

        getIconView().setOnClickListener(this);
        sViewWidth = getIconLayout().getLayoutParams().width;
        sViewHeight = getIconLayout().getLayoutParams().height;
        initIconMovingAnim();
    }


    public void setSlidePercent(float percent) {
        if (percent < 0.8f) {
            mSlidePercent = percent;
        }
    }

    public void setClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    private void initIconMovingAnim() {
        if (mIconMovingAnim == null) {
            mIconMovingAnim = new RotateAnimation(359f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mIconMovingAnim.setInterpolator(new LinearInterpolator());
            mIconMovingAnim.setDuration(800l);
            mIconMovingAnim.setRepeatCount(-1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        clearIconLayoutAnimation();
        xInScreen = event.getRawX();
        if (ignoreStatusBar()) {
            yInScreen = event.getRawY();
        } else {
            yInScreen = event.getRawY() - getStatusBarHeight();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Icon开始旋转
                getIconView().startAnimation(mIconMovingAnim);
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                if (ignoreStatusBar()) {
                    yDownInScreen = event.getRawY();
                } else {
                    yDownInScreen = event.getRawY() - getStatusBarHeight();
                }
                //一旦按下Icon，就需要重新计时，即移除两个message
                mUiHandler.removeMessages(MSG_ICON_CHANGE_ICON_WINDOW_SHADOW);
                mUiHandler.removeMessages(MSG_ICON_CHANGE_ICON_WINDOW_HIDE);
                if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.GINGERBREAD_MR1) {
                    getIconView().setAlpha(1.0f);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (needUpdateViewPosition()) {
                    updateViewPosition();
                }
                break;
            case MotionEvent.ACTION_UP:
                clearViewAnimation();
                startShadowIcon();
                //记录最后一次停靠位置Y
                Config.writeConfig(TAG, new Integer(mParams.y));
                //左边停靠
                int screenWidth = DisplayUtil.getRealScreenSizeX(getContext());
                if ((xInScreen - xInView) > (int) (screenWidth / 2)) {
                    moveToEdge(false);
                } else {
                    moveToEdge(true);
                }

                if ((xInScreen - xInView) < 0) {
                    startHideIcon(0);
                    return true;
                } else {
                    startHideIcon(TIME_DELAY_HIDE_ICON);
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    protected boolean needUpdateViewPosition() {
        return Math.abs(xInScreen - xDownInScreen) > 12 || Math.abs(yInScreen - yDownInScreen) > 12;
    }

    @Override
    public void onClick(final View v) {
        //先把Icon的红点置为0，然后开启大悬浮
        doIconClickAnimation(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
        resetParams(mParams);
    }

    public int getIconLocationX() {
        if (null != mParams) {
            return mParams.x;
        } else {
            return getConfigLocationX();
        }
    }

    public int getIconLocationY() {
        if (null != mParams) {
            ZLog.d("y:" + mParams.y);
            return mParams.y;
        } else {
            return getConfigLocationY();
        }
    }

    protected int getConfigLocationX() {
        return 0;
    }

    protected int getConfigLocationY() {
        //上次该用户放置的位置
        //默认出现的位置
        //如果有额外的配置，则使用额外配置否则默认92dp
        int height = (int) DisplayUtil.dip2px(this.getContext(), 92);

        ZLog.d("height:" + height);
        return height;
    }

    /**
     * 更新悬浮窗的位置
     */
    protected void updateViewPosition() {
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
        int screenWidth = DisplayUtil.getRealScreenSizeX(getContext());
        if (mParams.x <= 0 || mParams.x >= (screenWidth - 90)) {
            if (isLogoAnimRunning) {
                clearViewAnimation();
            }
        }
        updateViewLayout();
    }

    //停止icon的旋转
    public void clearViewAnimation() {
        getIconView().clearAnimation();
        isLogoAnimRunning = false;
    }

    // 删除整个icon的隐藏等效果
    public void clearIconLayoutAnimation() {
        if (getIconLayout().getAnimation() != null) {
            getIconLayout().clearAnimation();
        }
    }

    /**
     * Icon靠边
     */
    public void moveToEdge(boolean isLeft) {
        int screenWidth = DisplayUtil.getRealScreenSizeX(getContext());
        if (isLeft) {
            mParams.x = 0;
        } else {
            mParams.x = (int) screenWidth;
        }
        ZLog.d(TAG + " screenWidth:" + screenWidth);
        updateViewLayout();
        if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.GINGERBREAD_MR1) {
            getIconView().setAlpha(1.0f);
        }
    }

    protected void updateViewLayout() {
        resetParams(mParams);
        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * 动画执行完之后开启大悬浮窗
     */
    public void doIconClickAnimation(Animation.AnimationListener listener) {
        RotateAnimation rotateAnimation = new RotateAnimation(359f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(0);
        rotateAnimation.setDuration(300l);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setAnimationListener(listener);
        getIconView().setAnimation(rotateAnimation);
        getIconView().startAnimation(rotateAnimation);

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

    public void onDestroy() {
        mUiHandler.removeMessages(MSG_ICON_CHANGE_ICON_WINDOW_SHADOW);
        mUiHandler.removeMessages(MSG_ICON_CHANGE_ICON_WINDOW_HIDE);
    }

    public void showIcon() {
        startShadowIcon();
        startHideIcon(IconView.TIME_DELAY_HIDE_ICON);
        setVisibility(View.VISIBLE);
    }

    protected void startShadowIcon() {
        mUiHandler.sendEmptyMessageDelayed(MSG_ICON_CHANGE_ICON_WINDOW_SHADOW, TIME_DELAY_SHADOW_ICON);
    }

    protected void shadowIcon() {
        Animation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        getIconView().startAnimation(alphaAnimation);
    }

    protected void startHideIcon(int time) {
        if (time > 0) {
            mUiHandler.sendEmptyMessageDelayed(MSG_ICON_CHANGE_ICON_WINDOW_HIDE, time);
        } else {
            mUiHandler.sendEmptyMessage(MSG_ICON_CHANGE_ICON_WINDOW_HIDE);
        }
    }

    protected void hideIcon() {
        if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.GINGERBREAD_MR1) {
            getIconView().setAlpha(0.5f);
        }

        int le = getIconLocationX();
        int screenWidth = DisplayUtil.getRealScreenSizeX(getContext());
        boolean isLeft = le < screenWidth / 2;
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                isLeft ? 0 - mSlidePercent : mSlidePercent, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                0);
        animation.setDuration(300);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (BuildUtils.INSTANCE.getSDK_INT() > VERSION_CODES.GINGERBREAD_MR1) {
                    getIconView().setAlpha(1.0f);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        getIconLayout().startAnimation(animation);
    }

    protected void resetParams(WindowManager.LayoutParams mParams) {

    }

    protected boolean iconLocationIsLeft() {
        int screenWidth = DisplayUtil.getRealScreenSizeX(getContext());
        boolean iconLocationIsLeft = mParams.x < screenWidth / 2;
        return iconLocationIsLeft;
    }
}