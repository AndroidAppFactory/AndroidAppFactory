package com.bihe0832.lib.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zixie
 */
public abstract class CardBaseAdapter extends BaseMultiItemQuickAdapter<CardBaseModule, BaseViewHolder> {

    private Context mContext;
    private ArrayList mHeaderIDList = new ArrayList();

    abstract int getResourceIdByCardType(int cardType);

    abstract CardBaseHolder createViewHolder(int cardType, View itemView, Context context);

    public CardBaseAdapter(Context context, List data) {
        super(data);
        mContext = context;
    }

    protected void addItemToAdapter(int cardType) {
        addItemToAdapter(cardType, false);
    }

    protected void addItemToAdapter(int cardType, boolean isHeader) {
        addItemToAdapter(cardType, getResourceIdByCardType(cardType),isHeader);
    }

    protected void addItemToAdapter(int cardType, int layoutResId, boolean isHeader) {
        addItemType(cardType, layoutResId);
        if(isHeader){
            mHeaderIDList.add(cardType);
        }
    }

    protected boolean isFixedViewType(int type) {
        return mHeaderIDList.contains(type);
    }

    @Override
    protected BaseViewHolder createBaseViewHolder(ViewGroup parent, int resID) {
        return super.createBaseViewHolder(parent, resID);
    }

    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int cardId) {
        if (BaseMultiItemQuickAdapter.TYPE_NOT_FOUND != getLayoutId(cardId)) {
            View itemView = getItemView(getLayoutId(cardId), parent);
            return createViewHolder(cardId,itemView,mContext);
        } else {
            return createBaseViewHolder(parent, getLayoutId(cardId));
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, CardBaseModule item) {
        try {
            if (helper instanceof CardBaseHolder) {
                ((CardBaseHolder) helper).initData(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
