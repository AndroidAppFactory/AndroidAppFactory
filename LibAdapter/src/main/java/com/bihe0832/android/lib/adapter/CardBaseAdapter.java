package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.bihe0832.android.lib.adapter.item.BadDataTypeHolder;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author zixie
 * <p>
 * 目前框架仅支持同一个数据结构（即UI样式，BaseModule），不同场景下使用不同的展示形式（BaseHolder），暂不支持不同的数据结构，相同的展示形式
 * 在同一个 Adapter 中，一个数据结构（即UI样式，BaseModule）仅支持单一数据展示形式（BaseHolder），如果有相同的数据结构（即UI样式，BaseModule），不同的展示形式（BaseHolder），建议可以合并展现形式为一个
 */
public class CardBaseAdapter extends BaseMultiItemQuickAdapter<CardBaseModule, BaseViewHolder> {

    private Context mContext;
    private ArrayList mHeaderIDList = new ArrayList();
    private HashMap<Integer, Class<? extends CardBaseHolder>> mSpecialList = new HashMap<>();

    public CardBaseAdapter(Context context, List data) {
        super(data);
        mContext = context;
    }

    protected void addItemToAdapter(Class<? extends CardBaseModule> module) {
        addItemToAdapter(module, false);
    }

    protected void addItemToAdapter(Class<? extends CardBaseModule> module, boolean isHeader) {
        CardBaseInnerModule innerModuleInstance = CardInfoHelper.getInstance().getItemByClass(module);
        if (null != innerModuleInstance) {
            addItemToAdapter(innerModuleInstance, module, innerModuleInstance.getViewHolderClass(), false, isHeader);
        }
    }

    protected void addItemToAdapter(Class<? extends CardBaseModule> module, Class<? extends CardBaseHolder> holderClass, boolean isHeader) {
        addItemToAdapter(null, module, holderClass, true, isHeader);
    }

    private void addItemToAdapter(CardBaseInnerModule innerModuleParam, Class<? extends CardBaseModule> module, Class<? extends CardBaseHolder> holderClass, boolean addSpecial, boolean isHeader) {
        CardBaseInnerModule innerModuleInstance = innerModuleParam;
        if (null == innerModuleInstance) {
            innerModuleInstance = CardInfoHelper.getInstance().getItemByClass(module);
        }

        if (null != innerModuleInstance) {
            CardInfoHelper.getInstance().addCardItem(innerModuleInstance.getResID(), innerModuleInstance.getViewHolderClass());
        }
        if (addSpecial) {
            mSpecialList.put(innerModuleInstance.getResID(), holderClass);
        }

        if (isHeader) {
            mHeaderIDList.add(innerModuleInstance.getResID());
        }
    }

    protected boolean isFixedViewType(int type) {
        return mHeaderIDList.contains(type);
    }

    @Override
    protected BaseViewHolder createBaseViewHolder(ViewGroup parent, int resID) {
        try {
            return super.createBaseViewHolder(parent, resID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int cardId) {
        BaseViewHolder holder = null;
        if (BaseMultiItemQuickAdapter.TYPE_NOT_FOUND != cardId) {
            try {
                View itemView = getItemView(cardId, parent);
                if (mSpecialList.containsKey(cardId)) {
                    holder = CardInfoHelper.getInstance().createViewHolder(itemView, mSpecialList.get(cardId), mContext);
                } else {
                    holder = CardInfoHelper.getInstance().createViewHolder(cardId, itemView, mContext);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (holder == null) {
            holder = new BadDataTypeHolder(getItemView(getBadDataTypeHolderID(), parent), mContext);
        }
        return holder;
    }

    protected int getBadDataTypeHolderID() {
        return R.layout.com_bihe0832_base_card_bad_data_item;
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
