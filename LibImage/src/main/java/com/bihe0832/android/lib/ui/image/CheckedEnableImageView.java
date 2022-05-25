/*
 * *
 *  * Created by zixie < code@bihe0832.com > on 2022/5/25 下午4:22
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/5/25 下午3:40
 *
 */

package com.bihe0832.android.lib.ui.image;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;

public class CheckedEnableImageView extends ImageView implements Checkable {

    private boolean mChecked;

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public CheckedEnableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }
}