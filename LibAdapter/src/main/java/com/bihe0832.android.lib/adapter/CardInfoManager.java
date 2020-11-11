package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;

import com.bihe0832.android.lib.log.ZLog;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/11/11.
 * Description: Description
 */
class CardInfoManager {
    private HashMap<Integer, Class<CardBaseHolder>> mCardList = new HashMap<>();

    private static volatile CardInfoManager instance;

    public static CardInfoManager getInstance() {
        if (instance == null) {
            synchronized (CardInfoManager.class) {
                if (instance == null) {
                    instance = new CardInfoManager();
                }
            }
        }
        return instance;
    }

    private CardInfoManager() {
    }


    public final void addCardItem(int id, Class holder) {
        ZLog.d("addCardItem:" + id + " " + holder.toString());
        if (holder != null && CardBaseHolder.class.isAssignableFrom(holder)) {
            mCardList.put(id, holder);
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
