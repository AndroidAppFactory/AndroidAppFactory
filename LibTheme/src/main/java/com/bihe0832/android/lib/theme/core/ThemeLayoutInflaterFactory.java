package com.bihe0832.android.lib.theme.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.theme.ThemeManager;

import java.lang.reflect.Constructor;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class ThemeLayoutInflaterFactory implements LayoutInflater.Factory2, Observer {
    // 属性处理类
    ThemeAttributeTrans mThemeAttributeTrans;

    public ThemeLayoutInflaterFactory() {
        mThemeAttributeTrans = new ThemeAttributeTrans();
    }

    private static final ConcurrentHashMap<String, Constructor<? extends View>> sViewConstructorMap = new ConcurrentHashMap<String, Constructor<? extends View>>();
    private static final CopyOnWriteArrayList<String> sBadViewConstructorMap = new CopyOnWriteArrayList<>();

    private static final Class<?>[] sConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    private final String[] sViewPre = new String[]{"android.widget.", "android.view.", "androidx.", "android.webkit."};

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = createViewByTag(name, context, attrs);
        if (view == null) {
            view = createView(name, context, attrs);
        }
        if (view != null) {
            if (ThemeManager.INSTANCE.isDebug()) {
                ZLog.e(ThemeManager.TAG, String.format("检查[%s]:" + name, context.getClass().getName()));
            }
            mThemeAttributeTrans.load(view, attrs);
        }
        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return createView(name, context, attrs);
    }

    private View createView(String name, Context context, AttributeSet attrs) {
        Constructor<? extends View> constructor = findConstructor(context, name);
        try {
            if (constructor != null) {
                return constructor.newInstance(context, attrs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Constructor<? extends View> findConstructor(Context context, String name) {
        Constructor<? extends View> constructor = sViewConstructorMap.get(name);
        if (null == constructor && !sBadViewConstructorMap.contains(name)) {
            try {
                Class<? extends View> clazz = context.getClassLoader().loadClass(name).asSubclass(View.class);
                constructor = clazz.getConstructor(sConstructorSignature);
                sViewConstructorMap.put(name, constructor);
            } catch (Exception e) {
                sBadViewConstructorMap.add(name);
                e.printStackTrace();
            }
        }
        return constructor;
    }

    private View createViewByTag(String name, Context context, AttributeSet attrs) {
        if (-1 != name.indexOf('.')) {//包含自定义控件
            return null;
        }
        View view = null;
        for (int i = 0; i < sViewPre.length; i++) {
            view = createView(sViewPre[i] + name, context, attrs);
            if (view != null) {
                break;
            }
        }
        return view;
    }

    @Override
    public void update(Observable o, Object arg) {
        mThemeAttributeTrans.applyTheme();
    }

}
