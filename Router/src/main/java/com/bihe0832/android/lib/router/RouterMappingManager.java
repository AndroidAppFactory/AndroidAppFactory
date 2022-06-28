package com.bihe0832.android.lib.router;

import android.app.Activity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zixie code@bihe0832.com
 * Created on 2020/8/14.
 * Description: Description
 */
public class RouterMappingManager {

    private static final String STUB_PACKAGE_NAME = "com.bihe0832.android.lib.router.stub";

    private static CopyOnWriteArrayList<Class<? extends Activity>> activityIsMain = new CopyOnWriteArrayList<>();

    public static void addMain(Class<? extends Activity> activity) {
        if (!activityIsMain.contains(activity)) {
            activityIsMain.add(activity);
        }
    }

    private ConcurrentHashMap<String, Class<? extends Activity>> routerMappingByKey = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Activity>, String> routerMappingByValue = new ConcurrentHashMap<>();

    private static volatile RouterMappingManager instance;

    public static RouterMappingManager getInstance() {
        if (instance == null) {
            synchronized (RouterMappingManager.class) {
                if (instance == null) {
                    instance = new RouterMappingManager();
                }
            }
        }
        return instance;
    }

    private RouterMappingManager() {
        initMain();
    }

    private void initMain() {
        try {
            Class<?> threadClazz = Class.forName(STUB_PACKAGE_NAME + ".RouterInit");
            Method method = threadClazz.getMethod("init");
            System.out.println(method.invoke(null, new Object[]{}));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initMapping(String host) {
        if (!routerMappingByKey.isEmpty() && routerMappingByKey.containsKey(host)) {
            return;
        }
        try {
            Class<?> threadClazz = Class.forName(STUB_PACKAGE_NAME + ".RouterMapping_" + host);
            Method method = threadClazz.getMethod("map");
            System.out.println(method.invoke(null, new Object[]{}));
        } catch (Exception e) {
            e.printStackTrace();
            routerMappingByKey.put(host, null);
        }
    }

    public void addMapping(String host, Class<? extends Activity> activity) {
        routerMappingByKey.put(host, activity);
        routerMappingByValue.put(activity, host);
    }

    public List<Class<? extends Activity>> getActivityIsMain() {
        return (List<Class<? extends Activity>>) activityIsMain.clone();
    }

    ConcurrentHashMap<String, Class<? extends Activity>> getRouterMapping() {
        return routerMappingByKey;
    }

    ConcurrentHashMap<Class<? extends Activity>, String> getRouterMappingKey() {
        return routerMappingByValue;
    }

    public String getRouterHost(Class<? extends Activity> activitClass) {
        if (routerMappingByValue.containsKey(activitClass)) {
            return routerMappingByValue.get(activitClass);
        } else {
            return "";
        }
    }
}
