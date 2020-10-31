package com.bihe0832.android.test.base;

/**
 * Created by hardyshi on 2017/7/20.
 */

public class TestItem {
    String mTitle;
    OnTestItemClickListener mListener;
    public TestItem(String title,OnTestItemClickListener listener){
        mTitle = title;
        mListener = listener;
    }

    public String getTitle(){
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }
}
