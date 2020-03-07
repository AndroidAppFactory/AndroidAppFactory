package com.bihe0832.android.lib.config;

import android.content.Context;

import org.json.JSONObject;

import java.util.Map;

/**
 * @author code@bihe0832.com
 */

public class Config {

    // 开关值为开启
    public static final String VALUE_SWITCH_ON = "true";
    // 开关值为关闭
    public static final String VALUE_SWITCH_OFF = "false";

    public static void init(Context ctx, String file, boolean isDebug) {
        ConfigManager.getInstance().init(ctx, file, isDebug);
    }

    public static void loadLoaclFile(Context ctx, String file, boolean isDebug) {
        ConfigManager.getInstance().loadFile(ctx, file, isDebug);
    }

    public static String readConfig(String key, String defValue) {
        return ConfigManager.getInstance().readConfig(key, defValue);
    }

    public static int readConfig(String key, int defValue) {
        return ConfigManager.getInstance().readConfig(key, defValue);
    }

    public static long readConfig(String key, long defValue) {
        return ConfigManager.getInstance().readConfig(key, defValue);
    }

    public static float readConfig(String key, float defValue) {
        return ConfigManager.getInstance().readConfig(key, defValue);
    }

    public static double readConfig(String key, double defValue) {
        return ConfigManager.getInstance().readConfig(key, defValue);
    }

    public static boolean isSwitchEnabled(String switchKey, boolean defValue) {
        return ConfigManager.getInstance().isSwitchEnabled(switchKey, defValue);
    }

    public static boolean writeConfig(String key, String defValue, boolean saveToLocal) {
        return ConfigManager.getInstance().writeConfig(key, defValue,saveToLocal);
    }

    public static boolean writeConfig(String key, boolean defValue, boolean saveToLocal) {
        return ConfigManager.getInstance().writeConfig(key, defValue,saveToLocal);
    }

    public static boolean writeConfig(String key, int defValue, boolean saveToLocal) {
        return writeConfig(key, String.valueOf(defValue),saveToLocal);
    }

    public static boolean writeConfig(String key, long defValue, boolean saveToLocal) {
        return writeConfig(key, String.valueOf(defValue),saveToLocal);
    }

    public static boolean writeConfig(String key, float defValue, boolean saveToLocal) {
        return writeConfig(key, String.valueOf(defValue),saveToLocal);
    }

    public static boolean writeConfig(String key, double defValue, boolean saveToLocal) {
        return writeConfig(key, String.valueOf(defValue),saveToLocal);
    }

    public static boolean writeConfig(String key, String defValue) {
        return writeConfig(key, defValue,true);
    }

    public static boolean writeConfig(String key, boolean defValue) {
        return writeConfig(key, defValue,true);
    }

    public static boolean writeConfig(String key, int defValue) {
        return writeConfig(key, String.valueOf(defValue),true);
    }

    public static boolean writeConfig(String key, long defValue) {
        return writeConfig(key, String.valueOf(defValue),true);
    }

    public static boolean writeConfig(String key, float defValue) {
        return writeConfig(key, String.valueOf(defValue),true);
    }

    public static boolean writeConfig(String key, double defValue) {
        return writeConfig(key, String.valueOf(defValue),true);
    }

    public static boolean writeConfigs(Map<String, String> configs, boolean saveToLocal) {
        return ConfigManager.getInstance().writeConfigs(configs,saveToLocal);
    }

    public static boolean writeConfigs(Map<String, String> configs) {
        return writeConfigs(configs,true);
    }

    public static boolean writeConfigs(JSONObject configs, boolean saveToLocal) {
        return ConfigManager.getInstance().writeConfigs(configs,saveToLocal);
    }

    public static boolean writeConfigs(JSONObject configs) {
        return writeConfigs(configs,true);
    }
}
