package com.bihe0832.android.lib.ui.touchregion;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import java.util.ArrayList;


public class TouchDelegateGroup extends TouchDelegate {
    private static final Rect DEFAULT_RECT = new Rect();

    private ArrayList<TouchDelegate> mTouchDelegates;
    private TouchDelegate mCurrentTouchDelegate;

    public TouchDelegateGroup(View delegateView) {
        super(DEFAULT_RECT, delegateView);
    }

    public void addTouchDelegate(TouchDelegate touchDelegate) {
        if (mTouchDelegates == null) {
            mTouchDelegates = new ArrayList<TouchDelegate>();
        }
        mTouchDelegates.add(touchDelegate);
    }

    public void removeTouchDelegate(TouchDelegate touchDelegate) {
        if (mTouchDelegates != null) {
            mTouchDelegates.remove(touchDelegate);
            if (mTouchDelegates.isEmpty()) {
                mTouchDelegates = null;
            }
        }
    }

    public void clearTouchDelegates() {
        if (mTouchDelegates != null) {
            mTouchDelegates.clear();
        }
        mCurrentTouchDelegate = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TouchDelegate delegate = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchDelegates != null) {
                    for (TouchDelegate touchDelegate : mTouchDelegates) {
                        if (touchDelegate != null) {
                            if (touchDelegate.onTouchEvent(event)) {
                                mCurrentTouchDelegate = touchDelegate;
                                return true;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                delegate = mCurrentTouchDelegate;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                delegate = mCurrentTouchDelegate;
                mCurrentTouchDelegate = null;
                break;
            default:
                break;
        }
        return delegate == null ? false : delegate.onTouchEvent(event);
    }
}