package com.bihe0832.android.lib.ui.dialog.impl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.ui.dialog.R;
import com.bihe0832.android.lib.ui.dialog.callback.OnDialogListener;
import com.bihe0832.android.lib.ui.view.ext.ViewExtKt;
import com.bihe0832.android.lib.utils.MathUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;


public class DownloadProgressDialog extends Dialog {

    private TextView mTitleView;
    private TextView mContentView;
    private ProgressBar mProgress;
    private TextView mLeftTextView;
    private TextView mRightTextView;
    private TextView mNegativeButton;
    private View mButtonLine;
    private TextView mPositiveButton;

    public DownloadProgressDialog(Context context) {
        super(context, R.style.AAFCommonProgressDialogStyle);
    }

    private String mTitleString;
    private String mContentString;
    private long mContentSize;
    private long mCurrentSize = 0;
    private long mCurrentSpeed = 0;
    private int mPercentScale = 2;

    private boolean shouldCanceledOutside = false;
    private boolean shouldCanceled = true;

    private int maxLine = -1;
    private static final int MAX_LINES_LANDSCAPE = 3;
    private static final int MAX_LINES_PORTRAIT = 8;

    private CharSequence mNegativeButtonString;
    private CharSequence mPositiveButtonString;

    /**
     * 设置确定取消按钮的回调
     */
    public OnDialogListener mClickBottomListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_common_progress_dialog);
        //初始化界面控件
        initView();
        //初始化界面数据
        refreshView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickBottomListener != null) {
                    mClickBottomListener.onPositiveClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickBottomListener != null) {
                    mClickBottomListener.onNegativeClick();
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        //如果用户自定了title和message
        setCanceledOnTouchOutside(shouldCanceledOutside);
        setCancelable(shouldCanceled);
        int screenWidth = DisplayUtil.getScreenWidth(getContext());
        int screenheight = DisplayUtil.getScreenHeight(getContext());
        if (mTitleView != null) {
            if (!TextUtils.isEmpty(mTitleString)) {
                mTitleView.setText(mTitleString);
                mTitleView.setVisibility(View.VISIBLE);
            } else {
                mTitleView.setVisibility(View.GONE);
            }
        }

        if (null != mContentView) {
            if (!TextUtils.isEmpty(mContentString)) {
                if (!TextUtils.isEmpty(mContentString)) {
                    CharSequence charSequence = TextFactoryUtils.getSpannedTextByHtml(mContentString);//支持html
                    mContentView.setText(charSequence);
                }
                mContentView.setVisibility(View.VISIBLE);
                mContentView.setMovementMethod(new ScrollingMovementMethod());
                if (screenWidth > screenheight) {
                    if (maxLine > 0) {
                        if (maxLine > MAX_LINES_LANDSCAPE) {
                            mContentView.setMaxLines(MAX_LINES_LANDSCAPE);
                        } else {
                            mContentView.setMaxLines(maxLine);
                        }
                    } else {
                        mContentView.setMaxLines(MAX_LINES_LANDSCAPE);
                    }
                } else {
                    if (maxLine > 0) {
                        if (maxLine > MAX_LINES_PORTRAIT) {
                            mContentView.setMaxLines(MAX_LINES_PORTRAIT);
                        } else {
                            mContentView.setMaxLines(maxLine);
                        }
                    } else {
                        mContentView.setMaxLines(MAX_LINES_PORTRAIT);
                    }
                }
            } else {
                mContentView.setVisibility(View.GONE);
            }
        }

        if (null != mLeftTextView) {
            if (mCurrentSize > 0 && mContentSize > 0) {
                String result = "<strong>" + MathUtils.getFormatPercentDesc(
                        MathUtils.getFormatPercent(mCurrentSize, mContentSize, mPercentScale), mPercentScale)
                        + "</strong>";
                if (mContentSize != mCurrentSize && mCurrentSpeed > 0) {
                    result = result + "  |  " + FileUtils.INSTANCE.getFileLength(mCurrentSpeed, 1) + "/s";
                }
                mLeftTextView.setText(
                        TextFactoryUtils.getSpannedTextByHtml(TextFactoryUtils.getTextHtmlAfterTransform(result)));
            }
        }

        if (null != mRightTextView) {
            String result = "";
            if (mContentSize > 0) {
                if (mCurrentSize < 0) {
                    mCurrentSize = 0;
                }
                result = FileUtils.INSTANCE.getFileLength(mCurrentSize, 1) + " / " + FileUtils.INSTANCE.getFileLength(
                        mContentSize);
            }
            mRightTextView.setText(result);
        }
        if (null != mProgress) {
            if (mContentSize < 1) {
                mProgress.setProgress(0);
            } else {

                mProgress.setProgress((int) (mCurrentSize * 100 / mContentSize));
            }
        }

        //如果设置按钮的文字
        if (mPositiveButton != null) {
            if (!TextUtils.isEmpty(mPositiveButtonString)) {
                mPositiveButton.setText(mPositiveButtonString);
            } else {
                mPositiveButton.setText(getContext().getString(R.string.dialog_button_ok));
            }
        }

        if (mNegativeButton != null) {
            if (!TextUtils.isEmpty(mNegativeButtonString)) {
                mNegativeButton.setText(mNegativeButtonString);
                mNegativeButton.setVisibility(View.VISIBLE);
                mButtonLine.setVisibility(View.VISIBLE);
            } else {
                mNegativeButton.setVisibility(View.GONE);
                mButtonLine.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void show() {
        Activity activity = ViewExtKt.getActivity(getContext());
        if (null != activity && !activity.isFinishing()) {
            if (!isShowing()) {
                super.show();
            }
            refreshView();
        } else {
            ZLog.e("CommonDialog", "activity is null or isFinishing");
        }
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mTitleView = (TextView) findViewById(R.id.update_title);
        mContentView = (TextView) findViewById(R.id.update_message);
        mProgress = (ProgressBar) findViewById(R.id.update_progress_bar);
        mProgress.setMax(100);
        mRightTextView = (TextView) findViewById(R.id.update_progress_number);
        mLeftTextView = (TextView) findViewById(R.id.update_progress_percent);
        mNegativeButton = (TextView) findViewById(R.id.update_progress_cancle);
        mPositiveButton = (TextView) findViewById(R.id.update_progress_positive);
        mButtonLine = findViewById(R.id.update_progress_column_line);
    }


    public DownloadProgressDialog setOnClickListener(OnDialogListener bottomListener) {
        mClickBottomListener = bottomListener;
        return this;
    }

    public DownloadProgressDialog setMessage(String message) {
        this.mContentString = message;
        return this;
    }


    public DownloadProgressDialog setTitle(String title) {
        this.mTitleString = title;
        return this;
    }


    public DownloadProgressDialog setPositive(String positive) {
        this.mPositiveButtonString = positive;
        return this;
    }

    public DownloadProgressDialog setNegative(String negative) {
        this.mNegativeButtonString = negative;
        return this;
    }

    public DownloadProgressDialog setContentMaxLine(int maxLine) {
        this.maxLine = maxLine;
        return this;
    }

    public DownloadProgressDialog setShouldCanceled(boolean flag) {
        shouldCanceled = flag;
        return this;
    }

    // setProgress传入的参数以B为单位
    public void setCurrentSize(long currentSize, long currentSpeed) {
        if (mContentSize > 0 && currentSize > mContentSize) {
            mCurrentSize = mContentSize;
        }else {
            mCurrentSize = currentSize;
        }
        mCurrentSpeed = currentSpeed;
        refreshView();
    }

    // setMax传入的参数以B为单位
    public void setContentSize(long max) {
        mContentSize = max;
        refreshView();
    }

    // setMax传入的参数以B为单位
    public void setPercentScale(int max) {
        mPercentScale = max;
        refreshView();
    }

}