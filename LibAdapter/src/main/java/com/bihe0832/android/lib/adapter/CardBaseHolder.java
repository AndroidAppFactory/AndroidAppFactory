package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;

/**
 * Created by zixie on 2017/12/7.
 *
 * 所有卡片的Holder都继承自该类并实现对应的view查找和内容填充
 */

public abstract class CardBaseHolder extends BaseViewHolder {

    private Context mContext;

    public CardBaseHolder(View view, Context context) {
        super(view);
        if(null == context){
            mContext = view.getContext();
        }else {
            mContext = context;
        }
        initView();
    }

    /**
     * 通过getView 找到所有元素在页面内的布局
     */
    public abstract void initView();

    /**
     * 结合数据填充布局
     */
    public abstract void initData(CardBaseModule item);

    public Context getContext(){
        return mContext;
    }

    public void onDestroy() {

    }
}
