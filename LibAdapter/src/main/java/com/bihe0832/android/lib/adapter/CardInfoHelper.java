package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;

import com.bihe0832.android.lib.log.ZLog;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/11/11.
 * Description: Description
 */
class CardInfoHelper {
    private static final String TAG = "CardInfoHelper";
    private HashMap<Integer, Class<CardBaseHolder>> mCardList = new HashMap<>();
    private Context mApplicationContext = null;
    private static volatile CardInfoHelper instance;

    public static CardInfoHelper getInstance() {
        if (instance == null) {
            synchronized (CardInfoHelper.class) {
                if (instance == null) {
                    instance = new CardInfoHelper();
                }
            }
        }
        return instance;
    }

    private CardInfoHelper() {

    }

    public void init(Context context) {
        mApplicationContext = context;
    }

    public int getResIdByCardInfo(Class<? extends CardBaseInnerModule> module) {
        CardBaseInnerModule moduleItem = getItemByClass(module);
        if (null == moduleItem) {
            return BaseMultiItemQuickAdapter.TYPE_NOT_FOUND;
        } else {
            return moduleItem.getResID();
        }
    }

    public final void addCardItem(Class<? extends CardBaseInnerModule> module) {
        CardBaseInnerModule moduleItem = getItemByClass(module);
        if (null != moduleItem) {
            addCardItem(moduleItem.getResID(), moduleItem.getViewHolderClass());
        }
    }

    private CardBaseInnerModule getItemByClass(Class<? extends CardBaseInnerModule> module) {
        try {

            Constructor ct = module.getConstructors()[0];
            ZLog.d(TAG, "Constructor = " + ct.toString());

            Class<?>[] params = ct.getParameterTypes();
            Class[] cArg = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                cArg[i] = null;
            }
            CardBaseInnerModule moduleItem = (CardBaseInnerModule) ct.newInstance(cArg);
            return moduleItem;
        } catch (Exception e) {
            ZLog.e(TAG, "class " + module + " should hava a no params Constructor!!!");
            e.printStackTrace();
        }
        return null;
    }


    public final void addCardItem(int resID, Class<? extends CardBaseHolder> holderCalss) {
        ZLog.d(TAG, "addCardItem:" + resID + " " + holderCalss.toString());
        if (holderCalss != null && CardBaseHolder.class.isAssignableFrom(holderCalss)) {
            mCardList.put(resID, (Class<CardBaseHolder>) holderCalss);
        }
    }

    @Nullable
    public final CardBaseHolder createViewHolder(int cardType, @Nullable View itemView, @Nullable Context context) {
        try {
            Class holderClass = mCardList.get(cardType);
            if (holderClass != null) {
                return (CardBaseHolder) holderClass.getConstructor(View.class, Context.class).newInstance(itemView, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
