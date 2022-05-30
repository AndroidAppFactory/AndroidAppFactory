package com.bihe0832.android.lib.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.bihe0832.android.lib.text.TextFactoryUtils;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.dialog.view.ProgressIndicatorView;


public class LoadingDialog extends Dialog {

    public static final int LOADING_TYPE_CIRCLE = 1;
    public static final int LOADING_TYPE_DOTS = 2;

    /**
     * 显示的文字
     */
    private TextView titleTv;
    private String title;
    private boolean shouldCanceledOutside = true;
    private boolean isFullScreen = false;
    private int loadingType = LOADING_TYPE_DOTS;

    public LoadingDialog(Context context) {
        super(context, R.style.LoadingProgressDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_dialog_loading);
        //初始化界面控件
        initView();
        //初始化界面数据
        refreshView();
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        //如果用户自定了title和message
        if (loadingType == LOADING_TYPE_CIRCLE) {
            findViewById(R.id.circle_loading_bar).setVisibility(View.VISIBLE);
            findViewById(R.id.dots_loading_bar).setVisibility(View.GONE);
        } else {
            findViewById(R.id.circle_loading_bar).setVisibility(View.GONE);
            findViewById(R.id.dots_loading_bar).setVisibility(View.VISIBLE);
        }

        setCanceledOnTouchOutside(shouldCanceledOutside);
        if (titleTv != null) {
            if (TextUtils.isEmpty(title)) {
                title = "加载中……";
            }
            CharSequence charSequence = TextFactoryUtils.getSpannedTextByHtml(title);//支持html
            titleTv.setText(charSequence);
        }

        if (isFullScreen) {
            ViewGroup.LayoutParams lp = findViewById(R.id.loading_main_layout).getLayoutParams();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            findViewById(R.id.loading_main_layout).setLayoutParams(lp);
        } else {
            findViewById(R.id.loading_main_layout).setBackgroundResource(R.drawable.com_bihe0832_loading_progress_bg);
        }
    }

    private void showAction() {
        if (!isShowing()) {
            super.show();
        }
        refreshView();
    }

    public void showOnUIThread() {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                show();
            }
        });
    }

    @Override
    public void show() {
        showAction();
    }

    public void show(String msg) {
        title = msg;
        show();
    }

    public void showOnUIThread(String msg) {
        title = msg;
        showOnUIThread();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        titleTv = (TextView) findViewById(R.id.loading_label);
        ((ProgressIndicatorView)findViewById(R.id.dots_loading_bar)).setAnimationNum(3);
    }

    public LoadingDialog setHtmlTitle(String content) {
        this.title = content;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LoadingDialog setCanCanceled(boolean flag) {
        shouldCanceledOutside = flag;
        return this;
    }

    public LoadingDialog setIsFullScreen(boolean flag) {
        isFullScreen = flag;
        return this;
    }

    public void setLoadingType(int loadingType) {
        this.loadingType = loadingType;

    }
}