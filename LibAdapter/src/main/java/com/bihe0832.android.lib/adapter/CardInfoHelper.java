package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.ui.common.Res;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;

import org.jetbrains.annotations.Nullable;

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

    public int getResIdByCardInfo(Context context, Class<? extends CardBaseModule> module) {
        CardInfo getAnnotation = module.getAnnotation(CardInfo.class);
        return getResIdByCardInfo(context, getAnnotation);
    }

    private int getResIdByCardInfo(Context context, CardInfo getAnnotation) {
        if (getAnnotation == null) {
            return BaseMultiItemQuickAdapter.TYPE_NOT_FOUND;
        }

        if (mApplicationContext == null) {
            if (context == null) {
                return BaseMultiItemQuickAdapter.TYPE_NOT_FOUND;
            } else {
                mApplicationContext = context.getApplicationContext();
            }
        }

        try {
            String resFileName = getAnnotation.resFileName().replace(".xml", "");
            int id = Res.layout(mApplicationContext.getResources(), resFileName, mApplicationContext.getPackageName());
            ZLog.d(TAG, "getResIdByCardInfo:" + resFileName + " " + id);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseMultiItemQuickAdapter.TYPE_NOT_FOUND;
    }

    public final void addCardItem(Context context, Class<? extends CardBaseModule> module) {
        CardInfo getAnnotation = module.getAnnotation(CardInfo.class);
        if (getAnnotation == null) {
            ZLog.e("getAnnotation is null");
        } else {
            addCardItem(context, getResIdByCardInfo(context, getAnnotation), getAnnotation.holderCalss());
        }
    }

    public final void addCardItem(Context context, int resID, Class<? extends CardBaseHolder> holderCalss) {
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
