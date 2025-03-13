package com.bihe0832.android.base.debug.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author zixie code@bihe0832.com Created on 2025/3/13. Description: Description
 */
public class CuntomRecycleView extends RecyclerView {

    public CuntomRecycleView(@NonNull Context context) {
        super(context);
    }

    public CuntomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CuntomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }
}
