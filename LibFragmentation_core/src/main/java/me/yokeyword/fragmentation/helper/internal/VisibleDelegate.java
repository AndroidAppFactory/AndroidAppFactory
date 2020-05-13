package me.yokeyword.fragmentation.helper.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentationHack;

import java.util.List;

import me.yokeyword.fragmentation.ISupportFragment;

/**
 * Created by YoKey on 17/4/4.
 */

public class VisibleDelegate {
    private static final String FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave";
    private static final String FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE = "fragmentation_compat_replace";

    // SupportVisible相关
    private boolean mIsSupportVisible;
    private boolean mNeedDispatch = true;
    private boolean mInvisibleWhenLeave;
    private boolean mIsFirstVisible = true;
    private boolean mFixStatePagerAdapter;
    private boolean mFirstCreateViewCompatReplace = true;

    private Handler mHandler;
    private Bundle mSaveInstanceState;

    private ISupportFragment mSupportF;
    private Fragment mFragment;

    public VisibleDelegate(ISupportFragment fragment) {
        this.mSupportF = fragment;
        this.mFragment = (Fragment) fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSaveInstanceState = savedInstanceState;
            if (!mFixStatePagerAdapter) { // setUserVisibleHint() may be called before onCreate()
                mInvisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE);
                mFirstCreateViewCompatReplace = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, mInvisibleWhenLeave);
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE, mFirstCreateViewCompatReplace);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (!mFirstCreateViewCompatReplace && mFragment.getTag() != null && mFragment.getTag().startsWith("android:switcher:")) {
            return;
        }

        if (mFirstCreateViewCompatReplace) {
            mFirstCreateViewCompatReplace = false;
        }

        if (!mInvisibleWhenLeave && !mFragment.isHidden() &&
                (mFragment.getUserVisibleHint() || mFixStatePagerAdapter)) {
            if ((mFragment.getParentFragment() != null && isFragmentVisible(mFragment.getParentFragment()))
                    || mFragment.getParentFragment() == null) {
                mNeedDispatch = false;
                dispatchSupportVisible(true);
            }
        }
    }

    public void onResume() {
        if (!mIsFirstVisible) {
            if (!mIsSupportVisible && !mInvisibleWhenLeave && isFragmentVisible(mFragment)) {
                mNeedDispatch = false;
                dispatchSupportVisible(true);
            }
        }
    }

    public void onPause() {
        if (mIsSupportVisible && isFragmentVisible(mFragment)) {
            mNeedDispatch = false;
            mInvisibleWhenLeave = false;
            dispatchSupportVisible(false);
        } else {
            mInvisibleWhenLeave = true;
        }
    }

    public void onHiddenChanged(boolean hidden) {
        if (hidden || mFragment.getView() == null) {
            safeDispatchUserVisibleHint(false);
        } else {
            dispatchSupportVisible(true);
        }
    }

    public void onDestroyView() {
        mIsFirstVisible = true;
        mFixStatePagerAdapter = false;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mFragment.isResumed() || (mFragment.isDetached() && isVisibleToUser)) {
            if (!mIsSupportVisible && isVisibleToUser) {
                safeDispatchUserVisibleHint(true);
            } else if (mIsSupportVisible && !isVisibleToUser) {
                dispatchSupportVisible(false);
            }
        } else if (isVisibleToUser) {
            mInvisibleWhenLeave = false;
            mFixStatePagerAdapter = true;
        }
    }

    private void safeDispatchUserVisibleHint(final boolean visible) {
        if (mIsFirstVisible) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    dispatchSupportVisible(visible);
                }
            });
        } else {
            dispatchSupportVisible(visible);
        }
    }

    private void dispatchSupportVisible(boolean visible) {
        mIsSupportVisible = visible;

        if (!mNeedDispatch) {
            mNeedDispatch = true;
        } else {
            FragmentManager fragmentManager = mFragment.getChildFragmentManager();
            if (fragmentManager != null) {
                List<Fragment> childFragments = FragmentationHack.getActiveFragments(fragmentManager);
                if (childFragments != null) {
                    for (Fragment child : childFragments) {
                        if (child instanceof ISupportFragment && !child.isHidden() && child.getUserVisibleHint()) {
                            ((ISupportFragment) child).getSupportDelegate().getVisibleDelegate().dispatchSupportVisible(visible);
                        }
                    }
                }
            }
        }

        if (visible) {
            mSupportF.onSupportVisible();

            if (mIsFirstVisible) {
                mIsFirstVisible = false;
                mSupportF.onLazyInitView(mSaveInstanceState);
            }
        } else {
            mSupportF.onSupportInvisible();
        }
    }

    private boolean isFragmentVisible(Fragment fragment) {
        return !fragment.isHidden() && fragment.getUserVisibleHint();
    }

    public boolean isSupportVisible() {
        return mIsSupportVisible;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }
}
