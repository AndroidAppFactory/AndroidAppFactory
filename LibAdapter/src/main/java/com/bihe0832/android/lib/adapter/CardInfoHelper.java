package com.bihe0832.android.lib.adapter;

import android.content.Context;
import android.view.View;

import com.bihe0832.android.lib.log.ZLog;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/11/11.
 * Description: Description
 */
public class CardInfoHelper {
    private static final String TAG = "CardInfoHelper";
    private HashMap<Integer, Class<CardBaseHolder>> mCardList = new HashMap<>();
    private static volatile CardInfoHelper instance;
    private boolean mAutoAddItem = false;
    private boolean mIsDebug = false;

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

    protected CardBaseInnerModule getItemByClass(Class<? extends CardBaseInnerModule> module) {
        try {

            Constructor ct = module.getConstructors()[0];
            Class<?>[] params = ct.getParameterTypes();
            Object[] cArg = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                cArg[i] = getDefaultValue(params[i]);
            }
            CardBaseInnerModule moduleItem = (CardBaseInnerModule) ct.newInstance(cArg);
            return moduleItem;
        } catch (Exception e) {
            ZLog.e(TAG, "  \n !!!========================================  \n \n \n !!! LibAdapter: class " + module + " should hava a no params Constructor!!!!!!========================================");
            e.printStackTrace();
        }
        return null;
    }

    private Object getDefaultValue(Class clazz) {
        if (clazz.equals(boolean.class)) {
            return true;
        } else if (clazz.equals(byte.class)) {
            return Byte.MIN_VALUE;
        } else if (clazz.equals(short.class)) {
            return Short.MIN_VALUE;
        } else if (clazz.equals(int.class)) {
            return Integer.MIN_VALUE;
        } else if (clazz.equals(long.class)) {
            return Long.MIN_VALUE;
        } else if (clazz.equals(float.class)) {
            return Float.MIN_VALUE;
        } else if (clazz.equals(double.class)) {
            return Double.MIN_VALUE;
        } else {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void enableDebug(boolean isDebug) {
        mIsDebug = isDebug;
    }

    public boolean isDebug() {
        return mIsDebug;
    }

    public void setAutoAddItem(boolean autoAddItem) {
        mAutoAddItem = autoAddItem;
    }

    public boolean autoAddItem() {
        return mAutoAddItem;
    }

    public final void addCardItem(int resID, Class<? extends CardBaseHolder> holderCalss) {
        if (!mCardList.containsKey(resID) && holderCalss != null && CardBaseHolder.class.isAssignableFrom(holderCalss)) {
            ZLog.w(TAG, "added CardItem:" + resID + " " + holderCalss);
            mCardList.put(resID, (Class<CardBaseHolder>) holderCalss);
        }
    }

    public final CardBaseHolder createViewHolder(int cardType, View itemView, Context context) {
        try {
            if (mCardList.containsKey(cardType)) {
                return createViewHolder(itemView, mCardList.get(cardType), context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public final CardBaseHolder createViewHolder(View itemView, Class<? extends CardBaseHolder> holderClass, Context context) {
        try {
            if (holderClass != null) {
                return holderClass.getConstructor(View.class, Context.class).newInstance(itemView, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
