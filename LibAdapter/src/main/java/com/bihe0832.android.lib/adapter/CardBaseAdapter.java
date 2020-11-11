package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author zixie
 */
public abstract class CardBaseAdapter extends BaseMultiItemQuickAdapter<CardBaseModule, BaseViewHolder> {

    private Context mContext;
    private ArrayList mHeaderIDList = new ArrayList();

    private HashMap<String, Class<CardBaseModule>> test = new HashMap<>();

    public CardBaseAdapter(Context context, List data) {
        super(data);
        mContext = context;
    }

    protected void addItemToAdapter(Class<? extends CardBaseModule> module) {
        addItemToAdapter(module, false);
    }

    protected void addItemToAdapter(Class<? extends CardBaseModule> module, boolean isHeader) {
        if (module.isAnnotationPresent(CardInfo.class)) {
            CardInfo getAnnotation = module.getAnnotation(CardInfo.class);
            addItemType(getAnnotation.id(), getAnnotation.id());
            CardInfoManager.getInstance().addCardItem(getAnnotation.id(), getAnnotation.hoderCalss());
            if (isHeader) {
                mHeaderIDList.add(getAnnotation.id());
            }
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
            return CardInfoManager.getInstance().createViewHolder(cardId, itemView, mContext);
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
