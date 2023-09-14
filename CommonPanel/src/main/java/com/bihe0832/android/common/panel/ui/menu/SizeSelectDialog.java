package com.bihe0832.android.common.panel.ui.menu;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bihe0832.android.common.panel.R;
import com.bihe0832.android.lib.ui.custom.view.background.ViewWithBackground;
import com.bihe0832.android.lib.ui.dialog.CommonDialog;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

/**
 * @author zixie code@bihe0832.com Created on 7/20/21.
 */
public class SizeSelectDialog extends CommonDialog {


    public static final int TYPE_LINE = 1;

    public static final int TYPE_TEXT_SIZE = 2;

    private int currentType = TYPE_LINE;

    private int defaultSize = 1;

    private int minSize = 1;

    private int maxSize = 1;
    private SeekBar mSeekbar = null;
    private ViewWithBackground mLine = null;
    private TextView mText = null;


    private ConstraintLayout mConstraintLayout = null;


    public SizeSelectDialog(Context context, int type) {
        super(context);
        this.currentType = type;
    }

    public SizeSelectDialog(Context context, int themeResId, int type) {
        super(context, themeResId);
        this.currentType = type;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.com_bihe0832_common_panel_size_dialog_layout;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    protected void initView() {
        super.initView();
        mSeekbar = findViewById(R.id.seek_bar);
        mSeekbar.setProgress(defaultSize);
        if (mSeekbar != null) {
            mSeekbar.setProgress(defaultSize);
            mSeekbar.setMax(maxSize);
        }
        mConstraintLayout = findViewById(R.id.content_size);
        if (currentType == TYPE_LINE) {
            mLine = findViewById(R.id.line_size);
            mLine.setVisibility(View.VISIBLE);
        } else {
            mText = findViewById(R.id.text_size);
            mText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    refreshView();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void refreshView() {
        super.refreshView();

        if (mSeekbar != null && null != mConstraintLayout) {
            mConstraintLayout.setMinHeight(DisplayUtil.dip2px(getContext(), mSeekbar.getMax()));
        }
        if (currentType == TYPE_LINE) {
            ViewGroup.LayoutParams lineParam = null;
            if (null != mLine) {
                lineParam = mLine.getLayoutParams();
                if (null != lineParam) {
                    int height = DisplayUtil.dip2px(getContext(), mSeekbar.getProgress());
                    if (height == 0) {
                        lineParam.height = 1;
                    } else {
                        lineParam.height = height;
                    }
                    mLine.setLayoutParams(lineParam);
                }
            }
        }

        if (currentType == TYPE_TEXT_SIZE) {
            if (null != mText) {
                mText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mSeekbar.getProgress());
            }
        }
    }

    public void setSize(int defaultSize) {
        this.defaultSize = defaultSize;
        refreshView();
    }


    public void setMinAndMax(int maxSize) {
        this.maxSize = maxSize;
        refreshView();
    }

    public int getCurrent() {
        if (mSeekbar != null) {
            return mSeekbar.getProgress();
        }
        return 0;
    }

    public void reset() {
        mSeekbar.setProgress(defaultSize);
        refreshView();
    }
}
