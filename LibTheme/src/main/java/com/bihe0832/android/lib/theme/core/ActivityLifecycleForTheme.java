package com.bihe0832.android.lib.theme.core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.core.view.LayoutInflaterCompat;

import com.bihe0832.android.lib.theme.ThemeManager;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2018/3/16 0016.
 */

public class ActivityLifecycleForTheme implements Application.ActivityLifecycleCallbacks {

    ConcurrentHashMap<Activity, ThemeLayoutInflaterFactory> mLayoutFactoryMap = new ConcurrentHashMap<>();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (ThemeManager.INSTANCE.isEnabled()) {
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            forceSetFactory2(activity, layoutInflater);
        }
    }

    private void forceSetFactory2(Activity activity, LayoutInflater inflater) {
        Class<LayoutInflaterCompat> compatClass = LayoutInflaterCompat.class;
        Class<LayoutInflater> inflaterClass = LayoutInflater.class;
        try {
            Field sCheckedField = compatClass.getDeclaredField("sCheckedField");
            sCheckedField.setAccessible(true);
            sCheckedField.setBoolean(inflater, false);
            Field mFactory = inflaterClass.getDeclaredField("mFactory");
            mFactory.setAccessible(true);
            Field mFactory2 = inflaterClass.getDeclaredField("mFactory2");
            mFactory2.setAccessible(true);
            ThemeLayoutInflaterFactory factory = new ThemeLayoutInflaterFactory();
            mFactory2.set(inflater, factory);
            mFactory.set(inflater, factory);
            ThemeManager.INSTANCE.addObserver(factory);
            mLayoutFactoryMap.put(activity, factory);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ThemeLayoutInflaterFactory skinLayoutFactory = mLayoutFactoryMap.remove(activity);
        ThemeManager.INSTANCE.deleteObserver(skinLayoutFactory);
    }
}
