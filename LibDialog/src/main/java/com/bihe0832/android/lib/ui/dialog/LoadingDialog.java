package com.bihe0832.android.lib.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.bihe0832.android.lib.thread.ThreadManager;


public class LoadingDialog extends Dialog {

    /**
     * 显示的文字
     */
    private TextView titleTv;
    private String title;
    private boolean shouldCanceledOutside = true;
    private boolean isFullScreen = false;

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
        setCanceledOnTouchOutside(shouldCanceledOutside);
        if (titleTv != null) {
            if(TextUtils.isEmpty(title)){
                title = "加载中……";
            }
            CharSequence charSequence = Html.fromHtml(title);//支持html
            titleTv.setText(charSequence);
        }

        if(isFullScreen){
            ViewGroup.LayoutParams lp = findViewById(R.id.loading_main_layout).getLayoutParams();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            findViewById(R.id.loading_main_layout).setLayoutParams(lp);
        }else {
            findViewById(R.id.loading_main_layout).setBackgroundResource(R.drawable.loading_progress_bg);
        }
    }

    private void showAction() {
        if(!isShowing()){
            super.show();
        }
        refreshView();
    }

    @Override
    public void show() {
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                showAction() ;
            }
        });
    }

    public void show(String msg) {
        title = msg;
        ThreadManager.getInstance().runOnUIThread(new Runnable() {
            @Override
            public void run() {
                show();
            }
        });

    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        titleTv = (TextView) findViewById(R.id.loading_label);
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
}