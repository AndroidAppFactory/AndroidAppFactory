package com.bihe0832.android.lib.router;

import android.app.Activity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2020/8/14.
 * Description: Description
 */
public class RouterMappingManager {

    private static final String STUB_PACKAGE_NAME = "com.bihe0832.android.lib.router.stub";
    private HashMap<String, Class<? extends Activity>> mappings = new HashMap<>();
    private static ArrayList<Class<? extends Activity>> activityIsMain = new ArrayList<>();
    public static void addMain(Class<? extends Activity> activity) {
        if(!activityIsMain.contains(activity)){
            activityIsMain.add(activity);
        }
    }

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
            System.out.println(method.invoke(null, new  Object[]{}));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void initMapping(String format) {
        if (!mappings.isEmpty() && mappings.containsKey(format)) {
            return;
        }
        try {
            Class<?> threadClazz = Class.forName(STUB_PACKAGE_NAME + ".RouterMapping_"+ format);
            Method method = threadClazz.getMethod("map");
            System.out.println(method.invoke(null, new  Object[]{}));
        }catch (Exception e){
            e.printStackTrace();
            mappings.put(format,null);
        }
    }

    public void addMapping(String format, Class<? extends Activity> activity) {
        mappings.put(format, activity);
    }

    public ArrayList<Class<? extends Activity>> getActivityIsMain() {
        return activityIsMain;
    }

    HashMap<String, Class<? extends Activity>> getMappings() {
        return mappings;
    }
}
