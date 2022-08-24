package com.bihe0832.android.lib.ui.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;


/**
 * @author zixie code@bihe0832.com
 * Created on 2019-12-05.
 * Description: Description
 */
public class TextViewWithRedTips extends ConstraintLayout {

    private TextView mContentView;
    private TextView mRedNumView;
    private View mReddotView;

    private void initView(Context context) {
        View.inflate(context, R.layout.com_bihe0832_textview_red_tips, this);
        mContentView = findViewById(R.id.item_text);
        mRedNumView = findViewById(R.id.item_red_num);
        mReddotView = findViewById(R.id.item_red_dot);
    }

    public TextView getContentView() {
        return mContentView;
    }

    public TextView getRedNumView() {
        return mRedNumView;
    }

    public View getReddotView() {
        return mReddotView;
    }

    public TextViewWithRedTips(Context context) {
        super(context);
        initView(context);
    }

    public TextViewWithRedTips(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TextViewWithRedTips(Context context, AttributeSet attrs,
                               int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public void setText(String content){
        mContentView.setText(content);
    }

    public void setRednum(int num){
        if(num > 0){
            String showText = String.valueOf(num);
            if(num > 10000){
                showText = (num / 1000) + "k";
            }
            mRedNumView.setText(showText);
            mRedNumView.setVisibility(VISIBLE);
            mReddotView.setVisibility(GONE);
        }else {
            mRedNumView.setVisibility(GONE);
        }
    }

    public void showReddot(){
        mReddotView.setVisibility(VISIBLE);
    }

    public void hideReddot(){
        mReddotView.setVisibility(GONE);
    }
}