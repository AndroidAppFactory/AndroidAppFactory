package com.bihe0832.android.lib.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;


public class DownloadProgressDialog extends AlertDialog {

    private TextView mTitileView;
    private TextView mMessageView;
    private ProgressBar mProgress;
    private TextView mProgressPercent;
    private TextView mProgressNumber;
    private Button mNegativeButton;
    private View mButtonLine;
    private Button mPositiveButton;

    // 下载百分比进度
    private NumberFormat mProgressPercentFormat = NumberFormat.getPercentInstance();


    private CharSequence mTitleString;
    private CharSequence mMessageString;
    private int mAPKSize;
    private int mCurrentSize;

    private CharSequence mNegativeButtonString;
    private View.OnClickListener mNegativeButtonListener;

    private CharSequence mPositiveButtonString;
    private View.OnClickListener mPositiveButtonListener;

    private boolean mHasStarted;

    public DownloadProgressDialog(Context context) {
        // 默认采用的风格
        this(context, R.style.CommonProgressDialogStyle);
    }

    public DownloadProgressDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        this(context, R.style.CommonProgressDialogStyle);
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
    }

    public DownloadProgressDialog(Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        initFormat();
    }

    private void initFormat() {
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.common_progress_dialog, null);
        mTitileView = (TextView) view.findViewById(R.id.update_title);
        mMessageView = (TextView) view.findViewById(R.id.update_message);
        mProgress = (ProgressBar) view.findViewById(R.id.update_progress_bar);
        mProgressNumber = (TextView) view.findViewById(R.id.update_progress_number);
        mProgressPercent = (TextView) view.findViewById(R.id.update_progress_percent);
        mPositiveButton = (Button) view.findViewById(R.id.update_progress_cancle);
        mNegativeButton = (Button) view.findViewById(R.id.update_progress_positive);
        mButtonLine = view.findViewById(R.id.update_progress_column_line);
        setView(view);
        // 重新设置在onCreate之前传入的参数
        if (null != mTitleString) {
            mTitileView.setText(mTitleString);
        }

        if (null != mMessageString) {
            mMessageView.setText(mMessageString);
        }

        if (mAPKSize > 0) {
            setAPKSize(mAPKSize);
        }
        if (mCurrentSize > 0) {
            setCurrentSize(mCurrentSize);
        }

        if (null != mNegativeButtonString) {
            mNegativeButton.setText(mNegativeButtonString);
        }

        if (null != mNegativeButtonListener) {
            mNegativeButton.setOnClickListener(mNegativeButtonListener);
        }

        if (null != mPositiveButtonString) {
            mPositiveButton.setText(mPositiveButtonString);
        }

        if (null != mPositiveButtonListener) {
            mPositiveButton.setOnClickListener(mPositiveButtonListener);
        }

        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }

    // setProgress传入的参数以B为单位
    public void setCurrentSize(int value) {
        mCurrentSize = value;
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        }
    }

    // setMax传入的参数以B为单位
    public void setAPKSize(int max) {
        mAPKSize = max;
        if (mProgress != null) {
            mProgress.setMax(mAPKSize);
            onProgressChanged();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mTitileView != null) {
            mTitileView.setVisibility(View.VISIBLE);
            mTitileView.setText(title);
        } else {
            mTitleString = title;
        }
    }

    @Override
    public void setMessage(CharSequence message) {
        if (mMessageView != null) {
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(message);
        } else {
            mMessageString = message;
        }
    }

    public void setNegativeButton(CharSequence text, View.OnClickListener listener) {
        if (mNegativeButton != null) {
            if (listener == null) {
                mNegativeButton.setVisibility(View.GONE);
            } else {
                mNegativeButton.setText(text);
                mNegativeButton.setOnClickListener(listener);
                mNegativeButton.setVisibility(View.VISIBLE);
            }
        } else {
            mNegativeButtonString = text;
            mNegativeButtonListener = listener;
        }
    }

    public void setPositiveButton(String text, View.OnClickListener listener) {
        if (mPositiveButton != null) {
            if (listener == null || TextUtils.isEmpty(text)) {
                mPositiveButton.setVisibility(View.GONE);
            } else {
                mPositiveButton.setText(text);
                mPositiveButton.setOnClickListener(listener);
                mPositiveButton.setVisibility(View.VISIBLE);
            }
        } else {
            mPositiveButtonString = text;
            mPositiveButtonListener = listener;
        }
    }

    private void onProgressChanged() {
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (mProgress.getMax() < 1) {
            mProgressNumber.setVisibility(View.GONE);
            mProgressPercent.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        } else {
            mProgressNumber.setVisibility(View.VISIBLE);
            mProgressPercent.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            mProgressNumber.setText(
                    Formatter.formatFileSize(getContext(), mProgress.getProgress()) + "/"
                            + Formatter.formatFileSize(getContext(), mProgress.getMax()));

            if (mProgressPercentFormat != null) {
                double percent = mProgress.getProgress() * 1d / mProgress.getMax();
                SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                        0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mProgressPercent.setText(tmp);
            } else {
                mProgressPercent.setText("");
            }
            if (mPositiveButton.getText() == null || mNegativeButton.getText() != null) {
                mButtonLine.setVisibility(View.GONE);
            } else {
                mButtonLine.setVisibility(View.VISIBLE);
            }
        }
    }
}
