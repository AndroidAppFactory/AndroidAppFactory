package com.bihe0832.android.lib.ui.dialog;

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
import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.utils.os.DisplayUtil;

import java.text.NumberFormat;


public class DownloadProgressDialog extends Dialog {

    private TextView mTitleView;
    private TextView mContentView;
    private ProgressBar mProgress;
    private TextView mProgressPercent;
    private TextView mProgressNumber;
    private TextView mNegativeButton;
    private View mButtonLine;
    private TextView mPositiveButton;

    public DownloadProgressDialog(Context context) {
        super(context, R.style.CommonProgressDialogStyle);
    }

    private String mTitleString;
    private String mContentString;
    private long mAPKSize;
    private long mCurrentSize = 0;

    private boolean shouldCanceledOutside = false;
    private boolean shouldCanceled = true;
    private NumberFormat mProgressPercentFormat = NumberFormat.getPercentInstance();

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

        if (null != mProgressNumber) {
            if (mAPKSize < 1) {
                mProgressNumber.setVisibility(View.GONE);
            } else {
                mProgressNumber.setVisibility(View.VISIBLE);
                mProgressNumber.setText(
                        FileUtils.INSTANCE.getFileLength(mCurrentSize) + "/"
                                + FileUtils.INSTANCE.getFileLength(mAPKSize));
            }
        }

        if (null != mProgressPercent) {
            if (mAPKSize < 1) {
                mProgressPercent.setVisibility(View.GONE);
            } else {
                mProgressPercent.setVisibility(View.VISIBLE);
                if (mProgressPercentFormat != null) {
                    double percent = mCurrentSize * 1d / mAPKSize;
                    SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                    tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mProgressPercent.setText(tmp);
                } else {
                    mProgressPercent.setText("");
                }
            }
        }
        if (null != mProgress) {
            mProgress.setProgress(0);
            if (mAPKSize < 1) {
                mProgress.setVisibility(View.INVISIBLE);
            } else {
                mProgress.setProgress((int) (mCurrentSize * 100 / mAPKSize));
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        //如果设置按钮的文字
        if (mPositiveButton != null) {
            if (!TextUtils.isEmpty(mPositiveButtonString)) {
                mPositiveButton.setText(mPositiveButtonString);
            } else {
                mPositiveButton.setText("确定");
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
        super.show();
        refreshView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mTitleView = (TextView) findViewById(R.id.update_title);
        mContentView = (TextView) findViewById(R.id.update_message);
        mProgress = (ProgressBar) findViewById(R.id.update_progress_bar);
        mProgress.setMax(100);
        mProgressNumber = (TextView) findViewById(R.id.update_progress_number);
        mProgressPercent = (TextView) findViewById(R.id.update_progress_percent);
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
    public void setCurrentSize(long value) {
        mCurrentSize = value;
        refreshView();
    }

    // setMax传入的参数以B为单位
    public void setAPKSize(long max) {
        mAPKSize = max;
        refreshView();
    }
}